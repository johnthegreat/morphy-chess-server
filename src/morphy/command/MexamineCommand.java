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

import java.util.ArrayList;

import morphy.game.ExaminedGame;
import morphy.game.GameInterface;
import morphy.service.GameService;
import morphy.service.UserService;
import morphy.user.SocketChannelUserSession;
import morphy.user.UserSession;

public class MexamineCommand extends AbstractCommand {

	public MexamineCommand() {
		super("mexamine");
	}
	
	public void process(String arguments, UserSession userSession) {
		arguments = arguments.trim();
		
		if (arguments.equals("")) {
			userSession.send(getContext().getUsage());
			return;
		}
		
		GameService gs = GameService.getInstance();
		UserService us = UserService.getInstance();
		
		SocketChannelUserSession sess = (SocketChannelUserSession)userSession;
		
		GameInterface g = gs.map.get(userSession);
		if (g == null || !sess.isExamining()) {
			userSession.send("You are not examining or setting up a game.");
			return;
		}
		
				
		ExaminedGame eg = (ExaminedGame)g;
		UserSession[] arr = eg.getObservers();
		
		ArrayList<String> list = new ArrayList<String>();
		for(UserSession s : arr) {
			String username = s.getUser().getUserName();
			if (username.toLowerCase().startsWith(arguments.toLowerCase())) {
				list.add(username);
			}
		}
		if (list.size() >= 2) {
			StringBuilder b = new StringBuilder();
			b.append("-- Matches: " + list.size() + " player(s) --\n");
			for(int i=0;i<list.size();i++) {
				b.append(list.get(i) + "  ");
			}
			userSession.send(b.toString());
			return;
		}
		
		if (list.size() == 0) {
			// this user is logged in but is not watching the game
			// TODO we might need to add auto-complete handle code here.
			if (us.isLoggedIn(arguments)) {
				userSession.send(arguments + "is not observing the game you are examining.");
			} else {
				userSession.send(arguments + " is not logged in.");
			}
			return;
		} 
		
		if (list.size() == 1) {
			// this user is watching this game
			// we can presume that this user is logged in because code in other places should handle removing this player from the examined game.
			
			String username = list.get(0);
			
			SocketChannelUserSession mysess = (SocketChannelUserSession)us.getUserSession(username);
			mysess.getGamesObserving().remove(new Integer(g.getGameNumber()));
			mysess.setExamining(true);
			eg.getObserversAsList().remove(mysess);
			eg.addExaminingUser(mysess);
			
			gs.map.put(mysess,eg);
			
			sess.send(mysess.getUser().getUserName() + " is now an examiner of game " + g.getGameNumber() + ".");
			mysess.send("Removing game " + g.getGameNumber() + " from observation list.\n\n" + sess.getUser().getUserName() + " has made you an examiner of game " + g.getGameNumber() + ".\n\n" + eg.processMoveUpdate(sess));
		}
		
	}
}
