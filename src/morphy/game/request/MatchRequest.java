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

import morphy.game.MatchParams;
import morphy.service.GameService;
import morphy.service.RequestService;
import morphy.user.UserSession;

public class MatchRequest implements Request {
	
	private UserSession from;
	private UserSession to;
	private MatchParams params;
	private int requestNumber;
	public MatchRequest(UserSession from,UserSession to,MatchParams params) {
		this.from = from;
		this.to = to;
		this.params = params;
	}
	
	public boolean areMatchParamsSame(MatchParams mp) {
		return params.getTime() == mp.getTime() && 
			params.getIncrement() == mp.getIncrement() && 
			params.isRated() == mp.isRated() &&
			params.getColorRequested() == mp.getColorRequested() &&
			params.getVariant() == mp.getVariant();
	}

	public void acceptAction() {
		GameService gs = GameService.getInstance();
		gs.createGame(from, to, params);
		
		RequestService rs = RequestService.getInstance();
		rs.removeRequestFrom(from,this);
		rs.removeRequestTo(to,this);
	}

	public void declineAction() {
		to.send("You decline the match offer from " + from.getUser().getUserName() + ".");
		from.send(to.getUser().getUserName() + " declines the match offer.");
		
		RequestService rs = RequestService.getInstance();
		rs.removeRequestFrom(from,this);
		rs.removeRequestTo(to,this);
	}

	public void setParams(MatchParams params) {
		this.params = params;
	}

	public MatchParams getParams() {
		return params;
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

	public void setFrom(UserSession from) {
		this.from = from;
	}

	public UserSession getTo() {
		return to;
	}

	public void setTo(UserSession to) {
		this.to = to;
	}

}
