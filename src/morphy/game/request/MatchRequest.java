/*
 *   Morphy Open Source Chess Server
 *   Copyright (C) 2008-2010, 2016-2017  http://code.google.com/p/morphy-chess-server/
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

import morphy.game.params.GameParams;
import morphy.game.params.MatchParams;
import morphy.service.GameService;
import morphy.service.RequestService;
import morphy.user.UserSession;

public class MatchRequest implements Request {
	
	private UserSession from;
	private UserSession to;
	private GameParams params;
	private int requestNumber;
	private String extraInfo;
	public MatchRequest(UserSession from,UserSession to,GameParams params) {
		this.from = from;
		this.to = to;
		this.params = params;
	}
	
	public String getRequestIdentifier() {
		return "match";
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
		
		//
		// TODO: take into consideration the requested color, or the auto-assigned color by the server.
		//
		
		StringBuilder messageToSendWhite = new StringBuilder();
		messageToSendWhite.append(String.format("%s accepts the match offer.\n\n", from.getUser().getUserName()));
		
		// in this case, to-user is black...
		StringBuilder messageToSendBlack = new StringBuilder();
		messageToSendBlack.append(String.format("You accept the match offer from %s.\n\n", to.getUser().getUserName()));
		
		gs.createGame(from, to, params, messageToSendWhite, messageToSendBlack);
		
		RequestService rs = RequestService.getInstance();
		rs.removeRequest(this);
	}

	public void declineAction() {
		to.send(String.format("You decline the match offer from %s.",from.getUser().getUserName()));
		from.send(String.format("%s declines the match offer.",to.getUser().getUserName()));
		
		RequestService rs = RequestService.getInstance();
		rs.removeRequest(this);
	}

	public void setParams(MatchParams params) {
		this.params = params;
	}

	public GameParams getParams() {
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
	
	public String getExtraInfo() {
		return extraInfo;
	}
	
	public void setExtraInfo(String extraInfo) {
		this.extraInfo = extraInfo;
	}
}
