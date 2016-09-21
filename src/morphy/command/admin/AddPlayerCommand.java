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

import morphy.command.AbstractCommand;
import morphy.service.DBConnectionService;
import morphy.service.UserService;
import morphy.user.UserSession;
import morphy.utils.john.DBConnection;

public class AddPlayerCommand extends AbstractCommand {
	public AddPlayerCommand() {
		super("admin/addplayer");
	}

	public void process(String arguments, UserSession userSession) {
		final UserService serv = UserService.getInstance();
		
		final String[] args = arguments.split(" ");
		if (args.length < 3) {
			userSession.send(getContext().getUsage());
			return;
		}
		
		// playername emailaddress realname
		String playername = args[0];
		String emailaddress = args[1];
		String realname = "";
		for(int i=2;i<args.length;i++) {
			realname += " " + args[i];
		}
		
		if (playername.length() < 3) {
			userSession.send("Player name is too short");
			return;
		} else if (playername.length() > 17) {
			userSession.send("Player name is too long");
			return;
		}
		
		if (serv.isRegistered(playername)) {
			userSession.send("A player by the name " + playername + " is already registered.");
			return;
		}
		
		if (!emailaddress.contains("@")) { /* this verification is not FICS standard. */ }
		
		String password = serv.generatePassword(6);
		
		// do add comment, insert to db, etc here.
		
		DBConnection dbconn = DBConnectionService.getInstance().getDBConnection();
		String query = "INSERT INTO `users` VALUES(NULL,'" + playername + "','" + password + "',NULL,CURRENT_TIMESTAMP,'Player',0,'" + emailaddress + "')";
		dbconn.executeQuery(query);
		
		UserSession s = serv.getUserSession(playername);
		if (s != null) s.getUser().setRegistered(true);
		
		userSession.send("Added: >" + playername + "< >" + realname + "< >" + emailaddress + "<\n" +
						 "Password " + password);
	}
}
