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
package morphy.command.admin;

import java.sql.ResultSet;
import java.sql.Statement;

import morphy.Morphy;
import morphy.command.AbstractCommand;
import morphy.service.DBConnectionService;
import morphy.service.UserService;
import morphy.user.UserSession;
import morphy.utils.john.DBConnection;

public class AddCommentCommand extends AbstractCommand {
	public AddCommentCommand() {
		super("admin/addcomment");
	}

	public void process(String arguments, UserSession userSession) {		
		arguments = arguments.trim();
		
		int spacePos = arguments.indexOf(" ");
		if (arguments.equals("") || spacePos == -1) {
			userSession.send(getContext().getUsage());
			return;
		}
		
		String username = arguments.substring(0,spacePos).trim();
		arguments = arguments.substring(spacePos).trim();
		
		String[] matches = UserService.getInstance().completeHandle(username);
		if (matches.length > 1) {
			StringBuilder toprint = new StringBuilder("Ambiguous name " + 
					username + ":\n-- Matches: " + matches.length + " player(s) --\n");
			for(int i=0;i<matches.length;i++) { 
				toprint.append(matches[i]);
				if (i != matches.length-1) {
					toprint.append(" "); 
				} 
			}
			userSession.send(toprint.toString());
			return;
		} else if (matches.length == 1) { 
			username = matches[0];
		}
		
		// This limit of 300 is a database field constraint. This will need to be changed if the database changes as well. 
		if (arguments.length() > 300) {
			userSession.send("Maximum comment length is 300 characters. Please post a shorter comment.");
			return;
		}
		
		UserSession s = UserService.getInstance().getUserSession(username);
		if (s != null) {
			// add comment (arguments)
			int userId = s.getUser().getDBID();
			int commentFileId = 0;
			String query = "SELECT `id` FROM `commentfile` WHERE `user_id` = '" + userId + "'";
			DBConnectionService dbService = DBConnectionService.getInstance();
			DBConnection conn = dbService.getDBConnection();
			ResultSet rs = conn.executeQueryWithRS(query);
			try {
				if (rs != null) {
					if (rs.next()) {
						commentFileId = rs.getInt(1);
					}
				}
				if (commentFileId == 0) {
					query = "INSERT INTO `commentfile` (`id`,`user_id`) VALUES(NULL,'" + userId + "'); ";
					Statement statement = conn.getConnection().createStatement();
					statement.execute(query,Statement.RETURN_GENERATED_KEYS);
					rs = statement.getGeneratedKeys();
					if (rs.next()) {
						commentFileId = rs.getInt(1);
					}
				}
				
				if (commentFileId != 0) {
					query = "INSERT INTO `comment` (`id`,`commentfile_id`,`who_user_id`,`comment`,`timestamp`) " +
							"VALUES (NULL,'" + commentFileId + "','" + userSession.getUser().getDBID() + "','" + arguments + "',NOW())";
					boolean executed = conn.executeQuery(query);
					if (executed) {
						userSession.send("");
						return;
					}
				}
			} catch(Exception e) {
				userSession.send("Error adding comment!");
				Morphy.getInstance().onError(e);
				return;
			}
		} else {
			userSession.send(username.toLowerCase() + " isn't logged in.");
			return;
		}
	}
}
