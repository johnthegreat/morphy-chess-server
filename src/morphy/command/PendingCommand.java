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

public class PendingCommand extends AbstractCommand {

	public PendingCommand() {
		super("pending");
	}
	
	// 36: You are offering johnthegreatguest a challenge: GuestVNNP (----) johnthegreatguest (----) unrated crazyhouse 2 0.\n\n
	//  8: johnthegreat is offering a challenge: johnthegreat (2151) GuestVNNP (----) unrated crazyhouse 2 0.\n\n

	public void process(String arguments, UserSession userSession) {
		if (!arguments.equals("")) {
			process(userSession.getUser().getUserName(),userSession);
			return;
		}
		
		RequestService service = RequestService.getInstance();
		List<Request> from = service.getRequestsFrom(userSession);
		List<Request> to = service.getRequestsTo(userSession);
		
		StringBuilder b = new StringBuilder();
		//b.append("There are no offers pending to other players.\n\nThere are no offers pending from other players.\n");
		
		
		if (from == null || from.size() == 0) {
			b.append("There are no offers pending to other players.\n\n");
		} else {
			b.append("Offers to other players:\n\n");
			for(Request r : from) {
				b.append(" " + String.format("%2d",r.getRequestNumber()) + ": ");
				if (r instanceof MatchRequest) {
					MatchRequest mr = (MatchRequest)r;
					b.append("You are offering " + mr.getTo().getUser().getUserName() + " a challenge: " + mr.getFrom().getUser().getUserName() + " (----) " + mr.getTo().getUser().getUserName() + " (----) " + (mr.getParams().isRated()?"rated":"unrated") + " " + mr.getParams().getVariant().name() + " " + mr.getParams().getTime() + " " + mr.getParams().getIncrement() + ".\n");
				} else if (r instanceof AbortRequest) {
					AbortRequest ar = (AbortRequest)r;
					b.append("You are offering " + ar.getTo().getUser().getUserName() + " to abort the game.\n");
				} else if (r instanceof PartnershipRequest) {
					PartnershipRequest pr = (PartnershipRequest)r;
					b.append("You are offering " + pr.getTo().getUser().getUserName() + " to be bughouse partners.\n");
				}
			}
			b.append("\nIf you wish to withdraw any of these offers type \"withdraw number\".\n\n");
		}
		
		if (to == null || to.size() == 0) {
			b.append("There are no offers pending from other players.");
		} else {
			b.append("Offers from other players:\n\n");
			for(Request r : to) {
				b.append(" " + String.format("%2d",r.getRequestNumber()) + ": ");
				if (r instanceof MatchRequest) {
					MatchRequest mr = (MatchRequest)r;
					b.append(mr.getFrom().getUser().getUserName() + " is offering a challenge: " + mr.getFrom().getUser().getUserName() + " (----) " + mr.getTo().getUser().getUserName() + " (----) " + (mr.getParams().isRated()?"rated":"unrated") + " " + mr.getParams().getVariant().name() + " " + mr.getParams().getTime() + " " + mr.getParams().getIncrement() + ".\n");
				} else if (r instanceof AbortRequest) {
					AbortRequest ar = (AbortRequest)r;
					b.append(ar.getFrom().getUser().getUserName() + " is offering to abort the game.\n");
				} else if (r instanceof PartnershipRequest) {
					PartnershipRequest pr = (PartnershipRequest)r;
					b.append(pr.getFrom().getUser().getUserName() + " is offering to be bughouse partners.\n");
				}
			}
			b.append("\nIf you wish to accept any of these offers type \"accept number\".\nIf you wish to decline any of these offers type \"decline number\".");
		}
		
		userSession.send(b.toString());
	}
}
