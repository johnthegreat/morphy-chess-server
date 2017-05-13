/*
 *   Morphy Open Source Chess Server
 *   Copyright (C) 2008-2011, 2017  http://code.google.com/p/morphy-chess-server/
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
import morphy.service.GameService;
import morphy.service.RequestService;
import morphy.user.SocketChannelUserSession;
import morphy.user.UserSession;

public class AbortRequest implements Request {

	private UserSession from;
	private UserSession to;
	private int requestNumber;
	private String extraInfo = "#";
	
	public AbortRequest(UserSession from,UserSession to) {
		this.from = from;
		this.to = to;
	}
	
	public String getRequestIdentifier() {
		return "abort";
	}
	
	public void acceptAction() {
		GameService gs = GameService.getInstance();
		Game g = (Game)gs.map.get(from);
		g.setResult("*");
		
		String message = "";
		g.setReason("Game aborted by mutual agreement");
		final String line = "\n{Game " + g.getGameNumber() + " (" + g.getWhite().getUser().getUserName() + " vs. " + g.getBlack().getUser().getUserName() + ") " + g.getReason() + "} " + g.getResult() + "";
		
		if (!g.getReason().equals("Game aborted on move 1")) { message = "You accept the abort request from " + from.getUser().getUserName() + ".\n"; }
		to.send(message+line);
		message = to.getUser().getUserName() + " accepts the abort request.";
		from.send(message+line);
		
		gs.map.put(from,null);
		gs.map.put(to,null);
		
		((SocketChannelUserSession)from).setPlaying(false);
		((SocketChannelUserSession)to).setPlaying(false);
		
		RequestService rs = RequestService.getInstance();
		rs.removeRequest(this);
	}

	public void declineAction() {
		from.send(String.format("%s declines the abort request.", to.getUser().getUserName()));
		to.send(String.format("You decline the abort request from %s.", from.getUser().getUserName()));
		
		RequestService rs = RequestService.getInstance();
		rs.removeRequest(this);
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
	
	public String getExtraInfo() {
		return extraInfo;
	}
	
	public void setExtraInfo(String extraInfo) {
		this.extraInfo = extraInfo;
	}
}
