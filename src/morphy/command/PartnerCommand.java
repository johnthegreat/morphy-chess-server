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

import morphy.game.request.PartnershipRequest;
import morphy.game.request.Request;
import morphy.service.PartnershipService;
import morphy.service.RequestService;
import morphy.service.UserService;
import morphy.user.Partnership;
import morphy.user.UserSession;

public class PartnerCommand extends AbstractCommand {
	public PartnerCommand() {
		super("partner");
	}

	public void process(String arguments, UserSession userSession) {		
		if (arguments.trim().equals("")) {
			PartnershipService ps = PartnershipService.getInstance();
			
			boolean hasPartner = ps.getPartnershipMap().containsKey(userSession);
			if (hasPartner) {
				userSession.send("You no longer have a bughouse partner.");
				Partnership p = ps.getPartnershipMap().get(userSession);
				ps.removePartnership(p);
				UserSession partner = null;
				if (p.a == userSession) { partner = p.b; } else { partner = p.a; }
				if (partner != null) { partner.send("Your partner has ended partnership.\n" +
						"You no longer have a bughouse partner."); }
				return;
			} else {
				userSession.send("You do not have a bughouse partner.");
				return;
			}
		}
		
		UserService us = UserService.getInstance();
		String username = arguments;
		String[] possibleMatches = us.completeHandle(username);
		if (possibleMatches.length == 0) {
			userSession.send(username+" is not logged in.");
		}
		if (possibleMatches.length >= 2) {
			StringBuilder b = new StringBuilder();
			b.append("-- Matches: " + possibleMatches.length + " player(s) --\n");
			for(int i=0;i<possibleMatches.length;i++) {
				b.append(possibleMatches[i] + "  ");
			}
			userSession.send(b.toString());
			return;
		}
		if (possibleMatches.length == 1) {
			username = possibleMatches[0];
			
			if (username.equalsIgnoreCase(userSession.getUser().getUserName())) {
				userSession.send("You can't be your own bughouse partner.");
				return;
			}
			
			UserSession sess = us.getUserSession(username);
			
			RequestService rs = RequestService.getInstance();
			List<Request> list = rs.findAllToRequestsByType(userSession,PartnershipRequest.class);
			if (list == null || list.size() == 0) {
				boolean isRecipientOpenForBug = sess.getUser().getUserVars().getVariables().get("bugopen").equals("1");
				if (!isRecipientOpenForBug) {
					userSession.send(sess.getUser().getUserName() + " is not open for bughouse.");
					return;
				}
				
				boolean isOpenForBug = userSession.getUser().getUserVars().getVariables().get("bugopen").equals("1");
				userSession.send((!isOpenForBug?"Setting you open for bughouse.\n":"") + "Making a partnership offer to " + sess.getUser().getUserName() + ".");
				if (!isOpenForBug) userSession.getUser().getUserVars().getVariables().put("bugopen","1");
				username = userSession.getUser().getUserName();
				sess.send(username + " offers to be your bughouse partner; type \"partner " + username + "\" to accept.");
				rs.addRequest(userSession, sess, new PartnershipRequest(userSession, sess));
			} else if (list.size() == 1) {
				list.get(0).acceptAction();
			}
			return;
		}
	}
}
