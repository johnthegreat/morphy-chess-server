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
import morphy.game.Game;
import morphy.game.GameInterface;
import morphy.service.GameService;
import morphy.service.UserService;
import morphy.user.UserSession;

public class AllObserversCommand extends AbstractCommand {
	public AllObserversCommand() {
		super("allobservers");
	}

	public void process(String arguments, UserSession userSession) {
		arguments = arguments.trim();
		
		GameService gs = GameService.getInstance();
		GameInterface g = null;
		if (arguments.matches("[0-9]+")) {
			int gameNumber = 0;
			try {
				// NumberFormatException is thrown when 
				// the argument is out of range for Integer
				gameNumber = Integer.parseInt(arguments);
			} catch(NumberFormatException e) {
				gameNumber = 0;
			} finally {
				
			}
			g = gs.findGameById(gameNumber);
			if (g == null) {
				userSession.send("There is no such game.");
				return;
			}
		} else if (arguments.matches("\\w{3,17}")) {
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
			g = gs.map.get(array[0]);
		}
		
		if (g == null) { userSession.send(getContext().getUsage()); return; }
		
		StringBuilder b = new StringBuilder();
		UserSession[] arr = null;
		if (g instanceof Game) { 
			Game gg = (Game)g;
			arr = gg.getObservers();
			
			
			b.append("Observing " + String.format("%3d",gg.getGameNumber()) + " [" + gg.getWhite().getUser().getUserName() + " vs. " + gg.getBlack().getUser().getUserName() + "]: "); 
			for(UserSession s : arr) {
				b.append(s.getUser().getUserName() + " ");
			}
		}
		
		if (g instanceof ExaminedGame) {
			ExaminedGame gg = (ExaminedGame)g;
			arr = gg.getObservers();
			
			
			b.append("Examining " + String.format("%3d",gg.getGameNumber()) + " (scratch): "); 
			
			UserSession[] examiners = gg.getExaminers();
			for(UserSession s : examiners) {
				b.append("#" + s.getUser().getUserName() + " ");
			}
			
			for(UserSession s : arr) {
				b.append(s.getUser().getUserName() + " ");
			}
		}
		
		b.append("(" + arr.length + " " + (arr.length==1?"user":"users") + ")\n\n  1 game displayed (of " + gs.getCurrentNumberOfGames() + " in progress).");
		userSession.send(b.toString());
		return;
	}
}
