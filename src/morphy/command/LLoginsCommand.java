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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import morphy.Morphy;
import morphy.service.DBConnectionService;
import morphy.service.UserService;
import morphy.user.UserSession;

public class LLoginsCommand extends AbstractCommand {

	public LLoginsCommand() {
		super("llogins");
	}
	
	public void process(String arguments, UserSession userSession) {		
		/* Sun Aug  7, 12:00 MDT 2011: GuestGYKQ(U)         logout */
		
		boolean empty = StringUtils.isEmpty(arguments);
		boolean numeric = StringUtils.isNumeric(arguments);
		
		int limit = 10; //200
		arguments = arguments.trim();
		if (empty) {
			limit = 10;
		} else if (numeric && !empty) {
			limit = Integer.parseInt(arguments);
		} else if (!numeric && !empty) {
			userSession.send(getContext().getUsage());
			return;
		}
		
		boolean isAdmin = UserService.getInstance().isAdmin(userSession.getUser().getUserName());
		StringBuilder b = new StringBuilder();
		
		final java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEE MMM dd, HH:mm z yyyy");
		
		int count = 0;
		String query = "SELECT COUNT(*) FROM `logins`";
		ResultSet rs = DBConnectionService.getInstance().getDBConnection().executeQueryWithRS(query);
		try {
			if (rs.next()) {
				count = rs.getInt(1);
			}
		} catch(SQLException e) {
			Morphy.getInstance().onError(e);
		}
		
		if (limit > count) limit = count;
		
		query = "SELECT `id` FROM `logins` ORDER BY `id` DESC LIMIT " + limit;
		rs = DBConnectionService.getInstance().getDBConnection().executeQueryWithRS(query);
		int[] arr = new int[limit];
		try {
			int index = 0;
			while (rs.next()) {
				arr[index++] = rs.getInt(1);
			}
		} catch(SQLException e) {
			Morphy.getInstance().onError(e);
		}
		java.util.Arrays.sort(arr);
		
		Map<String,Boolean> registeredCache = new HashMap<String,Boolean>();
		
		query = "SELECT `id`,`username`,CONVERT_TZ(`timestamp`,'UTC','SYSTEM'),`type`" + (isAdmin?",`ipAddress`":"") + " FROM logins WHERE " + MessagesCommand.formatIdListForQuery("id", arr) + " ORDER BY id ASC";
		rs = DBConnectionService.getInstance().getDBConnection().executeQueryWithRS(query);
		try {
			while(rs.next()) {
				String line = "";
				String username = rs.getString(2);
				if (!registeredCache.containsKey(username.toLowerCase())) {
					boolean registered = UserService.getInstance().isRegistered(username);
					if (!registered) username += "(U)";
					registeredCache.put(username.toLowerCase(),registered);
				} else /* we have cached information about whether this user is registered */ {
					boolean registered = registeredCache.get(username.toLowerCase());
					if (!registered) username += "(U)";
				}
				
				if (!isAdmin) line = String.format("%26s: %-20s %s",sdf.format(rs.getTimestamp(3).getTime()),username,rs.getString(4));
				if (isAdmin) line = String.format("%26s: %-20s %7s from %s",sdf.format(rs.getTimestamp(3).getTime()),username,rs.getString(4),rs.getString(5));
				if (rs.next()) { line += "\n"; rs.previous(); }
				b.append(line);
			}
		} catch(SQLException e) {
			Morphy.getInstance().onError(e);
		}
		
		userSession.send(b.toString());
		return;
		
		/*UserSession uS = null;
		String[] array = UserService.getInstance().completeHandle(arguments);
		if (array.length == 0) {
			userSession.send(arguments + " is not logged in.");
			return;
		}
		if (array.length > 1) {
			StringBuilder b = new StringBuilder();
			b.append("-- Matches: " + array.length + " player(s) --\n");
			for(int i=0;i<array.length;i++) {
				b.append(array[i] + "  ");
			}
			userSession.send(b.toString());
			return;
		}
		uS = UserService.getInstance().getUserSession(array[0]);
		*/
	}

}
