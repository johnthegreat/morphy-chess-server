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

import java.text.SimpleDateFormat;

import morphy.game.ExaminedGame;
import morphy.game.Game;
import morphy.game.GameInterface;
import morphy.service.GameService;
import morphy.service.UserService;
import morphy.user.SocketChannelUserSession;
import morphy.user.UserSession;
import morphy.utils.john.TimeZoneUtils;

public class MovesCommand extends AbstractCommand {

	public MovesCommand() {
		super("moves");
	}
	
	/*
	 * Usage: moves [<empty>|n|username]
     */
	public void process(String arguments, UserSession userSession) {
		arguments = arguments.trim();
		
		GameService gs = GameService.getInstance();
		GameInterface g = null;
		
		UserSession s = null;
		if (arguments.equals("")) {
			s = userSession;
			
			SocketChannelUserSession sess = (SocketChannelUserSession)s;
			if (!sess.isExamining() && 
				!sess.isPlaying() && 
				sess.getGamesObserving().isEmpty()) {
				sess.send("You are neither playing, observing nor examining a game.");
			}
		} else if (arguments.matches("[0-9]+")) {
			int id = Integer.parseInt(arguments);
			g = gs.findGameById(id);
		} else {
			s = UserService.getInstance().getUserSession(arguments);
		}

		if (s != null && g == null) {
			g = gs.map.get(s);
			//if (g instanceof Game) g = (Game)gs.map.get(s);
			//if (g instanceof ExaminedGame) g = (ExaminedGame)gs.map.get(s);
		}
		if (g == null) { System.err.println("args = \"" + arguments + "\""); }
		
		StringBuilder b = new StringBuilder();
		b.append("\nMovelist for game " + g.getGameNumber() + ":\n\n");

		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d, HH:mm z yyyy");
		sdf.setTimeZone(TimeZoneUtils.getTimeZone(userSession.getUser()
				.getUserVars().getVariables().get("tzone").toUpperCase()));
		if (sdf.getTimeZone() == null) sdf.setTimeZone(java.util.TimeZone.getDefault());
		
		
		if (g instanceof Game) {
			b.append(((Game)g).getWhite().getUser().getUserName() + " (UNR) vs. " + 
					((Game)g).getBlack().getUser().getUserName() + " (UNR) --- " + sdf.format(g.getTimeGameStarted()) + "\n\r");
		} else if (g instanceof ExaminedGame) {
			ExaminedGame eg = (ExaminedGame)g;
			b.append(eg.getWhiteName() + " (" + eg.getWhiteRating() + ") vs. " + eg.getBlackName() + " (" + eg.getBlackRating() + ") --- " + sdf.format(eg.getTimeGameStarted()) + "\n\r");
		}
		
		b.append((g.isRated()?"Rated":"Unrated") + " " + g.getVariant().name() + " match, initial time: " + g.getTime() + " minutes, increment: " + g.getIncrement() + " seconds.\n\r\n\r");
		
		if (g instanceof Game) { 
			b.append(String.format("%4s  %-21s   %-21s\n\r","Move",((Game)g).getWhite().getUser().getUserName(),((Game)g).getBlack().getUser().getUserName())); 
		}
		b.append("----  ---------------------   ---------------------\n\r"); //21
		//b.append("  1.  e4      (0:00.000)      e5      (0:00.000)   \n\r");
		b.append("      {Still in progress} *\n\r");
		
		userSession.send(b.toString());

	}
}
