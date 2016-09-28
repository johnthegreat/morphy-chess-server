/*
 *   Morphy Open Source Chess Server
 *   Copyright (C) 2008-2010  http://code.google.com/p/morphy-chess-server/
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

import morphy.Morphy;
import morphy.service.DatabaseConnectionService;
import morphy.user.UserSession;
import morphy.utils.john.DatabaseConnection;

public class HandlesCommand extends AbstractCommand {

	public HandlesCommand() {
		super("handles");
	}

	public void process(String arguments, UserSession userSession) {
//		int spaceIndex = arguments.indexOf(' ');
//		if (spaceIndex == -1) {
//			userSession.send(getContext().getUsage());
//			return;
//		}
		
		if (arguments.length() < 2) {
			userSession.send("You need to specify at least two characters of the name.");
			return;
		}
		
		if (arguments.length() > 17) {
			userSession.send(arguments + " is not a valid handle.");
			return;
		}
		
		DatabaseConnectionService dbcs = DatabaseConnectionService.getInstance();
		
		DatabaseConnection conn = dbcs.getDBConnection();
		String query = "SELECT `username` FROM `users` WHERE `username` LIKE '" + arguments + "%' LIMIT 100";
		ResultSet rs = conn.executeQueryWithRS(query);
		List<String> arr = new ArrayList<String>();
		int numrows = 0;
		try {
			while(rs.next()) {
				arr.add(rs.getString(1));
				numrows++;
			}
		} catch(SQLException e) { Morphy.getInstance().onError(e); }
		
		StringBuilder b = new StringBuilder();
		b.append("-- Matches: " + numrows + " player(s) --\n");
		for(int i=0;i<arr.size();i++) {
			String s = arr.get(i);
			b.append(String.format("%-19s",s));
			if (i % 5 == 0) { b.append("\n"); }
		}
		userSession.send(b.toString());
	}
}
