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

import java.util.List;

import morphy.game.request.AbortRequest;
import morphy.game.request.MatchRequest;
import morphy.game.request.PartnershipRequest;
import morphy.game.request.Request;
import morphy.service.RequestService;
import morphy.user.UserSession;

public class WithdrawCommand extends AbstractCommand {
	public WithdrawCommand() {
		super("withdraw");
	}

	// withdraw [<empty> | n]
	public void process(String arguments, UserSession userSession) {
		RequestService rs = RequestService.getInstance();
		List<Request> list = rs.getRequestsFrom(userSession);
		
		if (arguments.matches("[0-9]+")) {
			Request r = rs.getRequestFrom(userSession,Integer.parseInt(arguments));
			if (!r.getFrom().equals(userSession)) {
				userSession.send("There is no offer " + arguments + " to withdraw.\nType \"pending\" to see the list of offers.");
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
			userSession.send("There is more than one pending offer.\nType \"pending\" to see the list of offers.\nType \"withdraw number\" to withdraw an offer.");
			return;
		}
		
		if (num == 0) {
			userSession.send("There are no offers to withdraw.");
			return;
		}
		
		if (num == 1) {
			Request r = list.get(0);
			
			String requestOrOffer = "offer";
			String offerType = "";
			if (r instanceof AbortRequest) { offerType = "abort"; requestOrOffer = "request"; }
			if (r instanceof MatchRequest) { offerType = "match"; requestOrOffer = "offer"; }
			if (r instanceof PartnershipRequest) { offerType = "partnership"; requestOrOffer = "request"; }
			
			r.getFrom().send("You withdraw the " + offerType + " " + requestOrOffer + " to " + r.getTo().getUser().getUserName() + ".");
			r.getTo().send(r.getFrom().getUser().getUserName() + " withdraws the " + offerType + " " + requestOrOffer + ".");
			
			rs.removeRequestFrom(r.getFrom(),r);
			rs.removeRequestTo(r.getTo(),r);
		}
	}
}
