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
import morphy.game.request.PauseRequest;
import morphy.game.request.Request;
import morphy.service.GameService;
import morphy.service.RequestService;
import morphy.user.UserSession;

public class PauseCommand extends AbstractCommand {
	public PauseCommand() {
		super("pause");
	}

	public void process(String arguments, UserSession userSession) {
		// The clock is not ticking yet.
		// The clock is already paused, use "unpause" to resume.
		// You are not playing or examining a game
		
		RequestService rs = RequestService.getInstance();
		List<Request> list = rs.getRequestsTo(userSession);
		
		GameService gs = GameService.getInstance();
		GameInterface g = gs.map.get(userSession);
		if (g == null) {
			userSession.send("You are not playing or examining a game.");
			return;
		} else {
			if (g instanceof Game) {
				Game gg = (Game)g;
				int numMoves = g.getBoard().getGame().getPosition().getPlyNumber();
				//boolean isBughouse = gg.getVariant() == Variant.bughouse || gg.getVariant() == Variant.frbughouse;
				if (!gg.isClockTicking() && numMoves < 2) {
					userSession.send("The clock is not ticking yet.");
					return;
				}
				if (!gg.isClockTicking()) {
					userSession.send("The clock is already paused, use \"unpause\" to resume.");
					return;
				} else {
					UserSession to = userSession==gg.getWhite()?gg.getBlack():gg.getWhite();
					PauseRequest pr = new PauseRequest(userSession, to);
					rs.addRequest(userSession, to, pr);
					userSession.send("Pause request sent.");
					to.send(userSession.getUser().getUserName() + " requests to pause the game.");
					return;
				}
			} else if (g instanceof ExaminedGame) {
				ExaminedGame eg = (ExaminedGame)g;
				eg.setClockTicking(false);
				g.processMoveUpdate(true);
			}
		}
		
		if (g != null && g instanceof Game) {
			if (arguments.matches("[0-9]+")) {
				Request r = rs.getRequestTo(userSession, Integer.parseInt(arguments));
				if (!r.getTo().equals(userSession)) {
					userSession.send(String.format("There is no offer %s to accept.\nType \"pending\" to see the list of offers.",arguments));
					return;
				}
				if (r != null) {
					// we found it, we no longer need the list
					list = new java.util.ArrayList<Request>(1);
					list.add(r);
				}
			}
			
			int num = list == null ? 0 : list.size();
			
			if (num >= 2) {
				userSession.send("There is more than one pending offer.\nType \"pending\" to see the list of offers.\nType \"decline n\" to decline an offer.");
				return;
			}
			
			if (num == 0) {
				userSession.send("There are no offers to decline.");
				return;
			}
			
			if (num == 1) {
				//
				list.get(0).acceptAction();
			}
		}
	}
}
