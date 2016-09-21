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

import morphy.game.ExaminedGame;
import morphy.game.GameInterface;
import morphy.service.GameService;
import morphy.user.SocketChannelUserSession;
import morphy.user.UserSession;

public class BratingCommand extends AbstractCommand {
	
	public BratingCommand() {
		super("brating");
	}
	
	public void process(String arguments, UserSession userSession) {
		arguments = arguments.trim();
		GameService gs = GameService.getInstance();
		
		GameInterface g = gs.map.get(userSession);
		
		if (g == null || !((SocketChannelUserSession)userSession).isExamining()) {
			userSession.send("You are not examining or setting up a game.");
			return;
		}
		

		int rating = 0;
		if (arguments.matches("[0-9]+")) {
			rating = Integer.parseInt(arguments);
			if (rating > 3500) { rating = 3500; }
			if (rating < 100) { rating = 100; }
		} else {
			userSession.send(getContext().getUsage());
			return;
		}
		

		ExaminedGame gg = (ExaminedGame) g;
		gg.setWhiteRating(rating);
		gg.processMoveUpdate(true);
		
		final String line = "Game " + gg.getGameNumber() + ": " + userSession.getUser().getUserName() + " sets black's rating to " + rating + ".";
		
		UserSession[] examiners = gg.getExaminers();
		for(int i=0;i<examiners.length;i++) {
			examiners[i].send(line);
		}

		UserSession[] observers = gg.getObservers();
		for(int i=0;i<observers.length;i++) {
			observers[i].send(line);
		}
	}
}
