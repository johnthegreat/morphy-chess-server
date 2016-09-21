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
package morphy.game.request;

import morphy.service.PartnershipService;
import morphy.service.RequestService;
import morphy.user.UserSession;

public class PartnershipRequest implements Request {

	private UserSession from;
	private UserSession to;
	private int requestNumber;
	
	public PartnershipRequest(UserSession from,UserSession to) {
		this.from = from;
		this.to = to;
	}
	
	public void acceptAction() {
		PartnershipService ps = PartnershipService.getInstance();
		ps.addPartnership(from, to);
		
		String toUsername = to.getUser().getUserName();
		from.send(toUsername + " agrees to be your partner.");
		from.send("You will now be following " + toUsername + "'s partner's games.");
		String fromUsername = from.getUser().getUserName();
		to.send("You agree to be " + fromUsername + "'s partner.");
		to.send("You will now be following " + fromUsername + "'s partner's games.");
		
		RequestService rs = RequestService.getInstance();
		rs.removeRequestFrom(from,this);
		rs.removeRequestTo(to,this);
	}

	public void declineAction() {
		RequestService rs = RequestService.getInstance();
		rs.removeRequestFrom(from,this);
		rs.removeRequestTo(to,this);
		
		from.send(to.getUser().getUserName() + " declines the partnership request.");
		to.send("You decline the partnership request from " + from.getUser().getUserName() + ".");
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
