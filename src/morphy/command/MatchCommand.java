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
package morphy.command;

import java.util.List;

import morphy.game.MatchParams;
import morphy.game.Variant;
import morphy.game.request.MatchRequest;
import morphy.game.request.Request;
import morphy.service.RequestService;
import morphy.service.UserService;
import morphy.user.PersonalList;
import morphy.user.SocketChannelUserSession;
import morphy.user.UserSession;
import morphy.user.UserVars;
import morphy.utils.MorphyStringTokenizer;
import morphy.utils.MorphyStringUtils;

public class MatchCommand extends AbstractCommand {

	public MatchCommand() {
		super("match");
	}
	
	/*
	 * Usage: match user [rated|unrated] [Time] [Inc] [White|Black]
     *            [{board_categoryboard}|w#|variant]
     */
	public void process(String arguments, UserSession userSession) {
		int pos = arguments.indexOf(" ");
		if (arguments.equals("")) {
			userSession.send(getContext().getUsage());
			//process(userSession.getUser().getUserName(),userSession);
			return;
		}
		
		
		UserService userService = UserService.getInstance();
		
		String user = arguments.substring(0,((pos == -1) ? arguments.length() : pos));
		if (user.length() == 1) {
			userSession.send("You need to specify at least two characters of the name.");
			return;
		}
		
		String[] matches = UserService.getInstance().completeHandle(user);
		if (matches.length > 1) {
			userSession.send("Ambiguous handle \"" + user + "\". Matches: " + MorphyStringUtils.toDelimitedString(matches," "));
			return;
		}
		
		if (matches.length == 1)
			user = matches[0];
		
		if (!UserService.getInstance().isValidUsername(user)) {
			userSession.send("There is no player matching the name " + user + ".");
			return;
		}
		
		if (user.equals(userSession.getUser().getUserName())) {
			userSession.send("You cannot match yourself.");
			return;
		}
		
		if (userSession.getUser().isOnList(PersonalList.censor,user)) {
			userSession.send("You are censoring " + user + ".");
			return;
		}
		
		if (userSession.getUser().isOnList(PersonalList.noplay,user)) {
			userSession.send("You have " + user + " on your noplay list.");
			return;
		}
		
		SocketChannelUserSession sess = (SocketChannelUserSession)userService.getUserSession(user);
		
		if (sess.getUser().isOnList(PersonalList.censor,userSession.getUser().getUserName())) {
			userSession.send(user + " is censoring you.");
			return;
		}
		
		if (sess.getUser().isOnList(PersonalList.noplay,userSession.getUser().getUserName())) {
			userSession.send("You are on " + user + "'s noplay list.");
			return;
		}
		
		if (sess.getUser().getUserVars().getVariables().get("open").equals("0")) {
			userSession.send(user + " is not open to match requests.");
			return;
		}
		
		if (sess.isPlaying()) {
			userSession.send(user + " is playing a game.");
			return;
		}
		
		if (sess.isExamining()) {
			userSession.send(user + " is examining a game.");
			return;
		}
		
		if (pos == -1) {
			
			
		}
		
		// defaults
		MatchParams p = new MatchParams();
		boolean timeSet = false,incrementSet = false,ratedSet = false,variantSet = false,colorSet = false;
		
//		userSession.send("Game is untimed - setting to unrated.");
//		userSession.send("Couldn't interpret the game parameters.");
//		userSession.send("No such board: unrate 0");
		
		/* i hate doing this, but for now i will presume it is in format: 3 0 r zh black */
		if (pos != -1) {
			MorphyStringTokenizer toks = new MorphyStringTokenizer(arguments," ");
			String tok = toks.nextToken();
			if (tok.matches("[0-9]+")) {
				// time
				p.setTime(Integer.parseInt(tok));
				timeSet = true;
				tok = toks.nextToken();
			}
			
			
			if (tok.matches("[0-9]+")) {
				// increment
				p.setIncrement(Integer.parseInt(tok));
				incrementSet = true;
				tok = toks.nextToken();
			}
			
			if (tok.equalsIgnoreCase("u") || tok.equalsIgnoreCase("unrated")) { 
				p.setRated(false); 
				ratedSet = true;
			}
			if (tok.equalsIgnoreCase("r") || tok.equalsIgnoreCase("rated")) {
				if (!userSession.getUser().isRegistered()) {
					userSession.send("You are unregistered - setting to unrated.");
					p.setRated(false);
				} else if (!sess.getUser().isRegistered()) {
					userSession.send(sess.getUser().getUserName() + " is unregistered - setting to unrated.");
					p.setRated(false);
				} else {
					p.setRated(true);
				}
				ratedSet = true;
			}
			
			tok = toks.nextToken();
			
			if (tok.equalsIgnoreCase("w") || tok.equalsIgnoreCase("white")) {
				p.setColorRequested(MatchParams.ColorRequested.White);
				colorSet = true;
			} else if (tok.equalsIgnoreCase("b") || tok.equalsIgnoreCase("black")) {
				p.setColorRequested(MatchParams.ColorRequested.Black);
				colorSet = true;
			} else {
				p.setColorRequested(MatchParams.ColorRequested.Neither);
				colorSet = true;
			}
		} 
		
		// there is no match string defined, so let's build out the match params from the user's defaults (parameters)
		if (!timeSet) p.setTime(Integer.parseInt(userSession.getUser().getUserVars().getVariables().get("time")));
		if (!incrementSet) p.setIncrement(Integer.parseInt(userSession.getUser().getUserVars().getVariables().get("inc")));
		if (!ratedSet) p.setRated(userSession.getUser().getUserVars().getVariables().get("rated").equals("1")?true:false);
		if (!variantSet) p.setVariant(Variant.blitz);
		if (!colorSet) p.setColorRequested(MatchParams.ColorRequested.Neither);
		
		StringBuilder str = new StringBuilder(200);
		
		if (p.isRated()) {
			if (!userSession.getUser().isRegistered()) {
				str.append("You are unregistered - setting to unrated.\n");
				p.setRated(false);
			} else if (!sess.getUser().isRegistered()) {
				str.append(sess.getUser().getUserName() + " is unregistered - setting to unrated.\n");
				p.setRated(false);
			}
		}
		
		// todo
		/*if (sess.getUser().getFormula() != null) {
			if (!sess.getUser().getFormula().matches(p)) {
				userSession.send("Match request does not fit formula for " + user + ": ");
				sess.send("Ignoring (formula): ");
				return;
			}
		}*/
		
		boolean update = false;
		
		RequestService rs = RequestService.getInstance();
		Request oldInstance = null;
		
		List<Request> list = rs.findAllFromRequestsByType(userSession,MatchRequest.class);
		if (list != null) {
			for(Request r : list) {
				if (r.getTo().equals(sess)) {
					if (((MatchRequest)r).areMatchParamsSame(p)) {
						str.append("You are already offering an identical match to " + sess.getUser().getUserName() + ".");
						userSession.send(str.toString());
						return;
					}
					
					// there is already a match challenge to this challenge
					// player, we should update instead of overriding.
					update = true;
					oldInstance = r;
					break;
				}
			}
		}		
	
		//str = new StringBuilder(200);
		//str.append("GuestBVKZ updates the match request.\n\n");
		/*
		 * 	match guestkqjz 3 0 r
		 *	GuestKQJZ is unregistered - setting to unrated.
		 *	You are already offering an identical match to GuestKQJZ.
		 */		
		
		
		
		UserVars uv = sess.getUser().getUserVars();
		StringBuilder toStr = new StringBuilder(200);
		toStr.append("Challenge: " + userSession.getUser().getUserName() + " (----) " + (p.getColorRequested()!=MatchParams.ColorRequested.Neither?"[" + p.getColorRequested().name() + "]":"") + " " + sess.getUser().getUserName() + " (----) " + (p.isRated()?"rated":"unrated") + " " + p.getVariant() + " " + p.getTime() + " " + p.getIncrement() + ".");
		toStr.append("\nYou can \"accept\" or \"decline\", or propose different parameters." + (uv.getVariables().get("bell").equals("1")?((char)7):""));
		sess.send(toStr.toString());
		
		uv = userSession.getUser().getUserVars();
		//str = new StringBuilder(200);
		if (uv.getVariables().get("open").equals("0")) {
			str.append("You are now open to receive match requests.\n");
			uv.getVariables().put("open","1");
		}
		if (!update) str.append("Issuing: " + userSession.getUser().getUserName() + " (----) " + sess.getUser().getUserName() + " (----) " + (p.getColorRequested()!=MatchParams.ColorRequested.Neither?"[" + p.getColorRequested().name() + "]":"") + " " + (p.isRated()?"rated":"unrated") + " " + p.getVariant() + " " + p.getTime() + " " + p.getIncrement() + ".");
		if (update) {
			str.append("Updating offer already made to \"" + sess.getUser().getUserName() + "\".\n\n");
			// TODO watchout for bug where color is Neither
			str.append("Updating match request to: " + ((p.getColorRequested()==MatchParams.ColorRequested.White)?userSession.getUser().getUserName():sess.getUser().getUserName()) + " (----) " + ((p.getColorRequested()==MatchParams.ColorRequested.Black)?userSession.getUser().getUserName():sess.getUser().getUserName()) + "(----) " + (p.isRated()?"rated":"unrated") + " " + p.getVariant() + " " + p.getTime() + " " + p.getIncrement() + ".");
		}
		userSession.send(str.toString());
		
		MatchRequest req = new MatchRequest(userSession,sess,p);
		rs.removeRequestTo(sess, oldInstance);
		rs.addRequest(userSession, sess, req);
	}
}
