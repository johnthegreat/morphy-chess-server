/*
 *   Morphy Open Source Chess Server
 *   Copyright (C) 2008-2010, 2017  http://code.google.com/p/morphy-chess-server/
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

import java.util.List;

import morphy.game.ExaminedGame;
import morphy.game.Game;
import morphy.game.GameInterface;
import morphy.game.request.AbortRequest;
import morphy.game.request.Request;
import morphy.service.GameService;
import morphy.service.RequestService;
import morphy.user.SocketChannelUserSession;
import morphy.user.UserSession;

public class AbortCommand extends AbstractCommand {
	public AbortCommand() {
		super("abort");
	}

	public void process(String arguments, UserSession userSession) {
		GameService gs = GameService.getInstance();
		
		GameInterface gi = gs.map.get(userSession);
		if (gi == null || (gi instanceof ExaminedGame)) {
			userSession.send("You are not playing a game.");
			return;
		}
		Game g = (Game)gi;
		
		RequestService rs = RequestService.getInstance();
		List<Request> list = rs.findAllToRequestsByType(userSession,AbortRequest.class);
		if (list == null || list.size() == 0) {
			int moveNumber = g.getBoard().getGame().getPosition().getPlyNumber() / 2;
			if (moveNumber == 0) {
				g.setReason("Game aborted on move 1");
				g.setResult("*");
				GameService.getInstance().endGame(g);
				
				final String line = "The game has been aborted on move one.\n\n{Game " + g.getGameNumber() + " (" + g.getWhite().getUser().getUserName() + " vs. " + g.getBlack().getUser().getUserName() + ") " + g.getReason() + "} " + g.getResult() + "";
				g.getWhite().send(line);
				g.getBlack().send(line);
				gs.map.put(g.getWhite(),null);
				gs.map.put(g.getBlack(),null);
				
				((SocketChannelUserSession)g.getWhite()).setPlaying(false);
				((SocketChannelUserSession)g.getBlack()).setPlaying(false);
			} else {
				UserSession to = userSession.equals(g.getWhite())?g.getBlack():g.getWhite();
				Request req = new AbortRequest(userSession,to);
				rs.addRequest(userSession,to,req);
				userSession.send("Abort request sent.");
				to.send(userSession.getUser().getUserName() + " would like to abort the game; type \"abort\" to accept.");
			}
		} else if (list.size() == 1) {
			list.get(0).acceptAction();
		}
		return;
	}
}
