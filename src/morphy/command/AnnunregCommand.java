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

import morphy.service.UserService;
import morphy.user.UserSession;

public class AnnunregCommand extends AbstractCommand {
	public AnnunregCommand() {
		super("annunreg");
	}

	public void process(String arguments, UserSession userSession) {
		UserSession[] all = UserService.getInstance().getLoggedInUsers();
		
		if (arguments.equals("")) {
			userSession.send(getContext().getUsage());
			return;
		}
		
		int sentTo = 0;
		for(UserSession sess : all) {
			if (!sess.getUser().isRegistered() || 
				sess.getUser().getUserName().equals(userSession.getUser().getUserName())) {
					sess.send("  ** UNREG ANNOUNCEMENT ** from " + userSession.getUser().getUserName() + ": " + arguments + "\n");
					sentTo++;
			}
		}
		userSession.send("(Announcement sent to " + sentTo + " unregistered players)");
	}
}
