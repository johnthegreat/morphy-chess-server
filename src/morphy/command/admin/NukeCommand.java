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
import morphy.service.UserService;
import morphy.user.UserSession;

public class NukeCommand extends AbstractCommand {
	public NukeCommand() {
		super("admin/nuke");
	}

	public void process(String arguments, UserSession userSession) {		
		arguments = arguments.trim();
			
		if (arguments.equals("") || arguments.indexOf(" ") != -1) {
			userSession.send(getContext().getUsage());
			return;
		}
		
		
		String[] matches = UserService.getInstance().completeHandle(arguments);
		if (matches.length > 1) {
			StringBuilder toprint = new StringBuilder("Ambiguous name " + 
					arguments + ":\n-- Matches: " + matches.length + " player(s) --\n");
			for(int i=0;i<matches.length;i++) { 
				toprint.append(matches[i]);
				if (i != matches.length-1) { 
					toprint.append(" "); 
				} 
			}
			userSession.send(toprint.toString());
			return;
		} else if (matches.length == 1) { 
			arguments = matches[0];
		}
		
//		if (!UserService.getInstance().isLoggedIn(arguments)) {
//			userSession.send(arguments + " is not logged in.");
//			return;
//		}
		
		UserSession s = UserService.getInstance().getUserSession(arguments);
		if (s != null) {
			String str = "Nuking: " + arguments.toLowerCase() + "\n";
			if (s.getUser().isRegistered())
				str += "Please leave a comment explaining why " + arguments + " was nuked.";
			userSession.send(str);
			s.send("**** You have been kicked out by " + userSession.getUser().getUserName() + "! ****\n\n\n\n");
			s.disconnect();
		} else {
			userSession.send(arguments.toLowerCase() + " isn't logged in.");
		}
	}
}
