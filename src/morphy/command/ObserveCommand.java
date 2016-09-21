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
import morphy.game.Game;
import morphy.game.GameInterface;
import morphy.service.GameService;
import morphy.service.UserService;
import morphy.user.SocketChannelUserSession;
import morphy.user.UserSession;

public class ObserveCommand extends AbstractCommand {
	private static enum FoundBy { GameNumber,Username };
	
	public ObserveCommand() {
		super("observe");
	}
	
	public void process(String arguments, UserSession userSession) {
		if (arguments.equals("")) {
			process(userSession.getUser().getUserName(),userSession);
			return;
		}
		
		GameService gs = GameService.getInstance();
		UserSession uS = null;
		
		FoundBy foundBy = null;
		GameInterface g = null;
		if (arguments.matches("[0-9]+")) {
			g = gs.findGameById(Integer.parseInt(arguments));
			foundBy = FoundBy.GameNumber;
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
			
			uS = UserService.getInstance().getUserSession(array[0]);
			g = gs.map.get(uS);
			foundBy = FoundBy.Username;
		}
		
		if (g == null) {
			userSession.send("There is no such game.");
			return;
		}
		
		if (((SocketChannelUserSession)userSession).getGamesObserving().contains(g.getGameNumber())) {
			//You are already observing game 515.
			//You are already observing caspiwins's game.
			
			if (foundBy == FoundBy.GameNumber) {
				userSession.send("You are already observing game " + g.getGameNumber() + ".");
				return;
			} else if (foundBy == FoundBy.Username && uS != null) {
				userSession.send("You are already observing " + uS.getUser().getUserName() + "'s game.");
				return;
			}
		}
		
		String whiteName = "",blackName = "";	
		if (g instanceof Game) {
			Game gg = (Game)g;
			gg.addObserver(userSession);
			userSession.send(gg.processMoveUpdate(userSession));
			whiteName = gg.getWhite().getUser().getUserName();
			blackName = gg.getBlack().getUser().getUserName();
		}
		if (g instanceof ExaminedGame) {
			ExaminedGame gg = (ExaminedGame)g;
			gg.addObserver(userSession);
			gg.processMoveUpdate(userSession);
			whiteName = gg.getWhiteName();
			blackName = gg.getBlackName();
		}
		userSession.send("You are now observing game " + g.getGameNumber() + ".\nGame " + g.getGameNumber() + ": " + whiteName + " (----) " + blackName + " (----) rated blitz " + g.getTime() + " " + g.getIncrement() + "\n\n" + g.processMoveUpdate(userSession));
		return;
	}

}
