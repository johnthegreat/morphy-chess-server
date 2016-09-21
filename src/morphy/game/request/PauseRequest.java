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
package morphy.game.request;

import morphy.game.Game;
import morphy.game.GameInterface;
import morphy.service.GameService;
import morphy.service.RequestService;
import morphy.user.UserSession;

public class PauseRequest implements Request {

	private UserSession from;
	private UserSession to;
	private int requestNumber;
	
	public PauseRequest(UserSession from,UserSession to) {
		this.from = from;
		this.to = to;
	}
	
	public void acceptAction() {
		GameService gs = GameService.getInstance();
		GameInterface g = gs.map.get(from);
		if (g instanceof Game) {
			Game gg = ((Game)g); 
			gg.setClockTicking(false);
			gg.processMoveUpdate(false);
		}
		String toUsername = to.getUser().getUserName();
		from.send(toUsername + " accepts the pause request.");
		from.send(g.processMoveUpdate(from) + "\nGame " + g.getGameNumber() + ": Game clock paused.");
		String fromUsername = from.getUser().getUserName();
		to.send("You accept the pause request from " + fromUsername + ".\n\n"+g.processMoveUpdate(to) + "\nGame " + g.getGameNumber() + ": Game clock paused.");
		
		RequestService rs = RequestService.getInstance();
		rs.removeRequestFrom(from,this);
		rs.removeRequestTo(to,this);
	}

	public void declineAction() {
		RequestService rs = RequestService.getInstance();
		rs.removeRequestFrom(from,this);
		rs.removeRequestTo(to,this);
		
		String toUsername = to.getUser().getUserName();
		from.send(toUsername + " declines the pause request.");
		String fromUsername = from.getUser().getUserName();
		to.send("You decline the pause request from " + fromUsername + ".");
	}

	public void setRequestNumber(int i) {
		requestNumber = i;
	}

	public int getRequestNumber() {
		return requestNumber;
	}

	public UserSession getFrom() {
		return from;
	}

	public UserSession getTo() {
		return to;
	}

}
