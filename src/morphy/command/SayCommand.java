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

import morphy.game.Game;
import morphy.game.GameInterface;
import morphy.service.GameService;
import morphy.service.UserService;
import morphy.user.PersonalList;
import morphy.user.SocketChannelUserSession;
import morphy.user.UserSession;
import morphy.user.UserVars;

public class SayCommand extends AbstractCommand {
	public SayCommand() {
		super("say");
	}

	public void process(String arguments, UserSession userSession) {
		int spaceIndex = arguments.indexOf(' ');
		if (spaceIndex == -1) {
			userSession.send(getContext().getUsage());
		} else { }
			GameService instance = GameService.getInstance();
			GameInterface gi = instance.map.get(userSession);
			if (gi == null) { }
			Game g = (Game)gi;
			// This does NOT support bughouse yet!
			UserSession opp = userSession.equals(g.getWhite())?g.getBlack():g.getWhite();
			String userName = opp.getUser().getUserName();
			UserSession personToTell = UserService.getInstance()
					.getUserSession(userName);
			if (personToTell == null) {
				userSession.send("I don't know who to say that to.");
				return;
			} else {
				if (personToTell.getUser().isOnList(PersonalList.censor,
						userSession.getUser().getUserName())) {
					userSession.send("Player \""
							+ personToTell.getUser().getUserName()
							+ "\" is censoring you.");
					return;
				}
				
				UserVars uv = personToTell.getUser().getUserVars();
				
				boolean highlight = uv.getVariables().get("highlight").equals("1");

				boolean isPlaying = ((SocketChannelUserSession)personToTell).isPlaying();
				
				int gameNumber = 0;
				if (isPlaying) {
					gameNumber = g.getGameNumber();
				}
				
				personToTell.send((highlight?(((char)27)+"[7m"):"") +
						UserService.getInstance().getTags(userSession.getUser().getUserName()) +
						(highlight?(((char)27)+"[0m"):"") + (gameNumber>0?"[" + gameNumber + "]":"") + " says: " + arguments);
				String s = "(told "
						+ personToTell.getUser().getUserName() + "";
				
				boolean isExamining = ((SocketChannelUserSession)personToTell).isExamining();
				
				if (isPlaying) {
					s += ", who is playing";
				} else if (isExamining) {
					s += ", who is examining a game";
				} else {
					String busyString = uv.getVariables().get("busy");
					int minutes = (int)(personToTell.getIdleTimeMillis()/60000);
					
					if (!busyString.equals("")) {
						s += ", who " + busyString + " ";
						
						int seconds = (int)(personToTell.getIdleTimeMillis()/1000);
						if (minutes > 0) { s += "(idle: " + minutes + " mins)"; }
						else { s += " (idle: " + seconds + " secs)"; }
					} else if (minutes >= 5) {
						s += ", who has been idle for " + minutes + " mins";
					}
				}
				
				userSession.send(s + ")");
		}
	}
}
