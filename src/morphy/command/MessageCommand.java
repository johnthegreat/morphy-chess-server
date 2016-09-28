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
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import morphy.service.DatabaseConnectionService;
import morphy.service.UserService;
import morphy.user.SocketChannelUserSession;
import morphy.user.UserSession;
import morphy.utils.john.TimeZoneUtils;

public class MessageCommand extends AbstractCommand {

	public MessageCommand() {
		super("message");
	}
	
	public void process(String arguments, UserSession userSession) {		
		/*  message johnthegreat hi
			The following message was sent and emailed to johnthegreat:
			johnthegreat at Fri Aug  5, 21:04 MDT 2011: hi
			
			The following message was received
			johnthegreat at Fri Aug  5, 21:04 MDT 2011: hi
		 */
		
		arguments = arguments.trim();
		if (arguments.equals("")) {
			new MessagesCommand().process(arguments, userSession);
			return;
		} else {
			Pattern p = Pattern.compile("(\\w+)\\s(.*)");
			Matcher m = p.matcher(arguments);
			if (m.matches()) {
				if (!UserService.getInstance().isRegistered(userSession.getUser().getUserName())) {
					userSession.send("Only registered players can use the messages command.");
					return;
				}
				
				String toUsername = m.group(1);
				String message = m.group(2);
				
				UserSession toUserSession = UserService.getInstance().getUserSession(toUsername);
				boolean isToUserOnline = ((SocketChannelUserSession)toUserSession).isConnected();
				toUsername = toUserSession.getUser().getUserName(); // do capitalization adjustments
				
				if (!UserService.getInstance().isRegistered(toUsername)) {
					userSession.send("Only registered players can have messages.");
					return;
				}
				
				if (message.length() > 2000) {
					userSession.send("Message is too long.");
					return;
				}
				
				Map<String,String> fromUserVars = userSession.getUser().getUserVars().getVariables();
				Map<String,String> toUserVars = userSession.getUser().getUserVars().getVariables();
				boolean emailMess = toUserSession.getUser().getUserVars().getVariables().get("mailmess")=="1"?true:false;
				
				String tzone = fromUserVars.get("tzone");
				// NOTE: The format for this date is different than the "date" command. 
				// The "date" command renders (eg) "05", while on FICS this renders (eg) " 5". 
				final java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEE MMM d, HH:mm z yyyy");
				// need to get user's timezone here from 'tzone' variable.
				sdf.setTimeZone(TimeZoneUtils.getTimeZone(tzone));
				if (sdf.getTimeZone() == null)
					sdf.setTimeZone(java.util.TimeZone.getDefault());
				
				// check to make sure their mail box isn't full
				// this doesn't apply to administrators
				if (!UserService.getInstance().isAdmin(userSession.getUser().getUserName()) &&
					!UserService.getInstance().isAdmin(toUsername)) {
					String query = "SELECT COUNT(*) FROM `messages` WHERE `to_user_id` = '" + toUserSession.getUser().getDBID() + "';";
					ResultSet rs = DatabaseConnectionService.getInstance().getDBConnection().executeQueryWithRS(query);
					try {
						if (rs.next()) {
							int numMessages = rs.getInt(1);
							if (numMessages >= 40) {
								// if mailmess is on but their message box is full, it is NOT added to their messages, but IS emailed.
								userSession.send(toUsername + "'s message box is full.");
								/*  johnthegreat's message box is full.

									A message cannot be received as your message box is full.
									The following message was emailed to johnthegreat:
									johnthegreat at Sat Aug  6, 20:08 MDT 2011: test
									
									The following message was emailed:
									johnthegreat at Sat Aug  6, 20:08 MDT 2011: test
								*/
								if (emailMess) {
									StringBuilder b = new StringBuilder();
									b.append("The following message was emailed to " + toUsername + ":");
									b.append(userSession.getUser().getUserName() + " at " + sdf.format(new java.util.Date()) + ": " + message);
									userSession.send(b.toString());
									
									if (isToUserOnline) {
										String subject = getSubject(userSession.getUser().getUserName());
										emailMessage(subject,message);
										
										tzone = toUserVars.get("tzone");
										TimeZone tz = TimeZoneUtils.getTimeZone(tzone);
										if (tz == null) tz = TimeZone.getDefault();
										sdf.setTimeZone(tz);
										
										b = new StringBuilder();
										b.append("A message cannot be received as your message box is full.\n");
										b.append("The following message was emailed:\n");
										b.append(userSession.getUser().getUserName() + " at " + sdf.format(new java.util.Date()) + ": " + message);
										toUserSession.send(b.toString());
									}
								}
								return;
							}
						}
					} catch(java.sql.SQLException e) { e.printStackTrace(System.err); }
				}
				
				StringBuilder b = new StringBuilder();
				b.append("The following message was sent" + (emailMess?" and emailed":"") + " to " + toUsername + ":\n");
				
				b.append(userSession.getUser().getUserName() + " at " + sdf.format(new java.util.Date()) + ": " + message);
				userSession.send(b.toString());

				if (isToUserOnline) {
					tzone = toUserVars.get("tzone");
					TimeZone tz = TimeZoneUtils.getTimeZone(tzone);
					if (tz == null) tz = TimeZone.getDefault();
					sdf.setTimeZone(tz);
					
					b = new StringBuilder("The following message was received\n");
					b.append(userSession.getUser().getUserName() + " at " + sdf.format(new java.util.Date()) + ": " + message);
					toUserSession.send(b.toString());
				}
				
				message = message.replace("'","''").replace("\\","\\\\"); // db security... make sure to decode on the other side!
				String query = "INSERT INTO `messages` (`id`,`from_user_id`,`to_user_id`,`message`,`timestamp`,`read`) " + 
				"VALUES(NULL," + userSession.getUser().getDBID() + "," + toUserSession.getUser().getDBID() + ",'" + message + "',UTC_TIMESTAMP(),'0');";
				DatabaseConnectionService.getInstance().getDBConnection().executeQuery(query);
				
				if (emailMess) {
					// send e-mail to the recipient user here
					String subject = getSubject(userSession.getUser().getUserName());
					emailMessage(subject,message);
				}
			}
		}
	}
	
	private String getSubject(String fromUsername) {
		return  "FICS message from " + fromUsername + " (Don't reply by email)";
	}
	
	/** currently no implementation */
	private void emailMessage(String subject,String body) {
		
	}
}
