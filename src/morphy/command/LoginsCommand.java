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

import org.apache.commons.lang.StringUtils;

import morphy.Morphy;
import morphy.service.DBConnectionService;
import morphy.service.UserService;
import morphy.user.UserSession;

public class LoginsCommand extends AbstractCommand {

	public LoginsCommand() {
		super("logins");
	}
	
	public void process(String arguments, UserSession userSession) {
		/*if (arguments.equals("")) {
			process(userSession.getUser().getUserName(),userSession);
			return;
		}*/
		/* 	log googlephone
			googlephone has not logged on.
		 */
		/* log YUBBUB
			Sun Aug  7, 13:00 MDT 2011: YUBBUB(U)            login 
			log YUBBUBTWO
			There is no player matching the name yubbubtwo.
		 */
		
		arguments = arguments.trim();
		
		String username = arguments;
		
		if (username.equals("")) {
			username = userSession.getUser().getUserName();
		}
		
		if (username.equals("*")) {
			new LLoginsCommand().process("", userSession);
			return;
		}
				
		int strlen = username.length();
		if (strlen < 2) {
			userSession.send("You need to specify at least two characters of the name.");
			return;
		}
		if (!arguments.contains("*") && !StringUtils.isAlpha(username)) {
			userSession.send(username + " is not a valid handle.");
			return;
		}
		if (arguments.contains("*")) username = username.replace("*","%");
		
		boolean isAdmin = UserService.getInstance().isAdmin(userSession.getUser().getUserName());
		StringBuilder b = new StringBuilder();
		final java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEE MMM dd, HH:mm z yyyy");
		
				
		
		int limit = 20;
		int count = 0;
		
		String query = "SELECT COUNT(*) FROM `logins` WHERE `username` LIKE '" + username + "'";
		ResultSet rs = DBConnectionService.getInstance().getDBConnection().executeQueryWithRS(query);
		try {
			if (rs.next()) {
				count = rs.getInt(1);
			}
		} catch(SQLException e) {
			Morphy.getInstance().onError(e);
		}
		
		if (limit > count) limit = count;
		
		query = "SELECT `id` FROM (SELECT `id` FROM `logins` WHERE `username` LIKE '" + username + "' ORDER BY `id` DESC) t LIMIT " + limit;
		rs = DBConnectionService.getInstance().getDBConnection().executeQueryWithRS(query);
		int[] arr = new int[limit];
		try {
			int index = 0;
			while (rs.next()) {
				arr[index++] = rs.getInt(1);
			}
			if (index == 0) {
				// this user has never connected it seems.
				if (username.contains("%")) {
					// this is a feature that is NOT part of FICS.
					userSession.send("No usernames matching the pattern " + username.replace("%","*") + " have ever logged in.");
					return;
				} else {
					userSession.send(username + " has not logged on.");
					return;
				}
			}
		} catch(SQLException e) {
			Morphy.getInstance().onError(e);
		}
		java.util.Arrays.sort(arr);
		StringBuilder queryBuilder = new StringBuilder(125);
		queryBuilder.append("SELECT `id`,`username`,CONVERT_TZ(`timestamp`,'UTC','SYSTEM'),`type`" + (isAdmin?",`ipAddress`":"") + " " +
				"FROM `logins` WHERE `id` IN ("); // WHERE `username` LIKE '" + username + "'
		for(int i=0;i<arr.length;i++) {
			queryBuilder.append(arr[i]);
			if (i != arr.length-1) queryBuilder.append(",");
		}
		queryBuilder.append(") ORDER BY `id` ASC");
		query = queryBuilder.toString();
		rs = DBConnectionService.getInstance().getDBConnection().executeQueryWithRS(query);
		try {
			while(rs.next()) {
				String line = "";
				if (!isAdmin) line = String.format("%26s: %-20s %s",sdf.format(rs.getTimestamp(3).getTime()),rs.getString(2),rs.getString(4));
				if (isAdmin) line = String.format("%26s: %-20s %7s from %s",sdf.format(rs.getTimestamp(3).getTime()),rs.getString(2),rs.getString(4),rs.getString(5));
				if (rs.next()) { line += "\n"; rs.previous(); }
				b.append(line);
			}
		} catch(SQLException e) {
			Morphy.getInstance().onError(e);
		}
		
		userSession.send(b.toString());
	}

}
