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

import morphy.service.ServerListManagerService;
import morphy.service.UserService;
import morphy.user.UserSession;
import morphy.utils.john.ServerList;

public class QtellCommand extends AbstractCommand {

	public QtellCommand() {
		super("qtell");
	}
	
	public boolean willProcess(UserSession userSession) {
		return true;
	}

	public void process(String arguments, UserSession userSession) {
		ServerListManagerService service = ServerListManagerService.getInstance();
		
		int whiteSpacePos = arguments.indexOf(" ");
		if (whiteSpacePos == -1) {
			userSession.send(getContext().getUsage());
			return;
		}
		
		if (!service.isOnAnyList(new ServerList[] { service.getList("TD"),service.getList("admin") },
				userSession.getUser().getUserName())) {
					userSession.send("Only TD programs and admins are allowed to use this command.");
					return;
		}
		
		String userName = arguments.substring(0, whiteSpacePos);
		String message = arguments.substring(whiteSpacePos + 1, arguments.length());
	
		UserSession sendTo = UserService.getInstance().getUserSession(userName);
		if (sendTo != null) {
			sendTo.send(":" + message);
			userSession.send("*qtell " + userName + " 0*");
		} else {
			userSession.send("*qtell " + userName + " 1*");
		}
	
		
	}

}
