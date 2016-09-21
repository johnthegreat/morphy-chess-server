/*
 *   Morphy Open Source Chess Server
 *   Copyright (C) 2008-2011  http://code.google.com/p/morphy-chess-server/
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package morphy.command;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import morphy.service.DBConnectionService;
import morphy.service.UserService;
import morphy.user.UserSession;

public class ClearmessagesCommand extends AbstractCommand {

	public ClearmessagesCommand() {
		super("clearmessages");
	}
	
	public void process(String arguments, UserSession userSession) {		
		/*  clearmessages *
			     will delete all of your messages.
			clearmessages 2
			     will delete your message #2.
			clearmessages DAV
			     will delete all of your messages from DAV.
			clearmessages 4-7
			     will delete your messages 4 through 7.
		 */
		/*	clearmessage 38-40
			Messages 38-40 cleared.
		*/  
		/*	clearmessages 37-40
			You have no messages 37-40.
		 */
		/*	You have 36 messages (1 unread).
			Use "messages u" to view unread messages and "clearmessages *" to clear all.
		*/
		
		
		arguments = arguments.trim();
		if (arguments.equals("")) {
			userSession.send(getContext().getUsage());
			return;
		} else {
			if (!UserService.getInstance().isRegistered(userSession.getUser().getUserName())) {
				userSession.send("Only registered players can use the clearmessages command.");
				return;
			}
			
			int numMessages = 0;
			String query = "SELECT COUNT(*) FROM `messages` WHERE `to_user_id` = '" + userSession.getUser().getDBID() + "';";
			ResultSet rs = DBConnectionService.getInstance().getDBConnection().executeQueryWithRS(query);
			try {
				if (rs.next()) {
					numMessages = rs.getInt(1);
				}
			} catch(SQLException e) {
				e.printStackTrace(System.err);
			}
			
			if (numMessages == 0) {
				userSession.send("You have no messages.");
				return;
			}
				
			if (arguments.equals("*")) {
				// delete all messages
				
				query = "DELETE FROM `messages` WHERE `to_user_id` = '" + userSession.getUser().getDBID() + "'";
				boolean executed = DBConnectionService.getInstance().getDBConnection().executeQuery(query);
				if (executed) {
					userSession.send("Messages cleared.");
					return;
				}
			}
			
			if (StringUtils.isNumeric(arguments)) {
				// delete this message
				
				arguments += "-" + arguments;
				//return;
			}
			
			if (StringUtils.isAlpha(arguments)) {
				// delete all messages from this user
				
				int id = UserService.getInstance().getDBID(arguments);
				if (id == 0) { /* something failed */ 
					userSession.send("There is no player matching the name " + arguments + ".");
					return;
				} else {
					/*	clearmessages outrunyou
						You have no messages from OUTRUNYOU.
					*/
					
					String username = UserService.getInstance().correctCapsUsername(arguments);
					
					query = "SELECT `id` FROM `messages` WHERE `from_user_id` = '" + id + "' ORDER BY `id` ASC";
					rs = DBConnectionService.getInstance().getDBConnection().executeQueryWithRS(query);
					try {
						List<Integer> ids = new ArrayList<Integer>();
						while(rs.next()) {
							ids.add(new Integer(rs.getInt(1)));
						}
						if (ids.size() == 0) {
							userSession.send("You have no messages from " + username + ".");
							return;
						} else {
							query = "DELETE FROM `messages` WHERE " + MessagesCommand.formatIdListForQuery("id",ids);
							boolean executed = DBConnectionService.getInstance().getDBConnection().executeQuery(query);
							if (executed) {
								userSession.send("Messages from " + username + " cleared.");
								return;
							}
						}
					} catch(SQLException e) {
						e.printStackTrace(System.err);
					}
				}
				return;
			}
			
			if (arguments.matches("[0-9]+\\-[0-9]+")) {
				// delete this range of messages
				List<Integer> list = MessagesCommand.expandRange(arguments);
				java.util.Collections.sort(list);
				query = "SELECT m.`id`,u1.`username`,`message`,`timestamp`,`read` " + 
				"FROM `messages` m INNER JOIN `users` u1 ON (u1.`id` = m.`from_user_id`) " +
				"WHERE m.to_user_id = '" + userSession.getUser().getDBID() + "'" +
				"ORDER BY m.`id` ASC";
				rs = DBConnectionService.getInstance().getDBConnection().executeQueryWithRS(query);
				try {
					// the "ids" variable contains the actual message ids as stored in the database
					// and NOT the psuedo message number as the user thinks.
					List<Integer> ids = new ArrayList<Integer>();
					List<Integer> rownums = new ArrayList<Integer>();
					int rownum = 0;
					while(rs.next()) {
						rownum++;
						if (list.contains(rownum)) {
							ids.add(rs.getInt(1));
							rownums.add(rownum);
						}
					}
					if (ids.size() > 0) {
						query = "DELETE FROM `messages` WHERE " + MessagesCommand.formatIdListForQuery("id",ids);
						boolean executed = DBConnectionService.getInstance().getDBConnection().executeQuery(query);
						if (executed) {
							userSession.send((rownums.size()==1?"Message":"Messages") + " " + rownums.toString() + " cleared.");
							return;
						}
					} else {
						userSession.send("You have no message" + (rownums.size()>1?"s":"") + " " + rownums.toString() + ".");
						return;
					}
				} catch(SQLException e) {
					e.printStackTrace(System.err);
				}
				
				return;
			}
			
			// if we've reached this point, nothing has been deleted, so invalid arguments.
			userSession.send(getContext().getUsage());
			return;
		}
	}
}
