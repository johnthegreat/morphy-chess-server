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

import morphy.service.PartnershipService;
import morphy.service.RequestService;
import morphy.user.UserSession;

public class PartnershipRequest implements Request {

	private UserSession from;
	private UserSession to;
	private int requestNumber;
	private String extraInfo = "#";
	
	public PartnershipRequest(UserSession from,UserSession to) {
		this.from = from;
		this.to = to;
	}
	
	public String getRequestIdentifier() {
		return "partner";
	}
	
	public void acceptAction() {
		PartnershipService ps = PartnershipService.getInstance();
		ps.addPartnership(from, to);
		
		String toUsername = to.getUser().getUserName();
		from.send(String.format("%s agrees to be your partner.",toUsername));
		from.send(String.format("You will now be following %s's partner's games.",toUsername));
		
		String fromUsername = from.getUser().getUserName();
		to.send(String.format("You agree to be %s's partner.",fromUsername));
		to.send(String.format("You will now be following %s's partner's games.",fromUsername));
		
		RequestService rs = RequestService.getInstance();
		rs.removeRequest(this);
	}

	public void declineAction() {
		from.send(String.format("%s declines the partnership request.",to.getUser().getUserName()));
		to.send(String.format("You decline the partnership request from %s.",from.getUser().getUserName()));
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
