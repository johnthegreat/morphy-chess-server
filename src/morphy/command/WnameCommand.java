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

import morphy.game.ExaminedGame;
import morphy.game.GameInterface;
import morphy.service.GameService;
import morphy.user.SocketChannelUserSession;
import morphy.user.UserSession;

public class WnameCommand extends AbstractCommand {
	
	public WnameCommand() {
		super("wname");
	}
	
	public void process(String arguments, UserSession userSession) {
		arguments = arguments.trim();
		GameService gs = GameService.getInstance();
		
		GameInterface g = gs.map.get(userSession);
		
		if (g == null || !((SocketChannelUserSession)userSession).isExamining()) {
			userSession.send("You are not examining or setting up a game.");
			return;
		}
		
		if (arguments.length() > 17) {
			userSession.send("The maximum length of a name is 17 characters.");
			return;
		}
		
		if (arguments.equals("")) {
			// nothing is sent to user on blank username it seems
			return;
		}
		

		ExaminedGame gg = (ExaminedGame) g;
		gg.setWhiteName(arguments);
		gg.processMoveUpdate(true);
		
//		final String line = "Game " + gg.getGameNumber() + ": " + userSession.getUser().getUserName() + " sets white's name to " + "" + ".";
//		
//		UserSession[] examiners = gg.getExaminers();
//		for(int i=0;i<examiners.length;i++) {
//			examiners[i].send(line);
//		}
//
//		UserSession[] observers = gg.getObservers();
//		for(int i=0;i<observers.length;i++) {
//			observers[i].send(line);
//		}
	}
}
