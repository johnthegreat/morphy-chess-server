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

import morphy.game.ExaminedGame;
import morphy.game.Game;
import morphy.game.GameInterface;
import morphy.game.style.StyleInterface;
import morphy.service.GameService;
import morphy.service.UserService;
import morphy.user.SocketChannelUserSession;
import morphy.user.UserSession;

public class RefreshCommand extends AbstractCommand {
	
	public RefreshCommand() {
		super("refresh");
	}
	
	public void process(String arguments, UserSession userSession) {
		arguments = arguments.trim();
		GameService gs = GameService.getInstance();
		
		int gameNumber = 0;
		
		if (arguments.equals("")) {
			SocketChannelUserSession sess = (SocketChannelUserSession)userSession;
			if (!sess.isExamining() && 
				!sess.isPlaying() && 
				sess.getGamesObserving().isEmpty()) {
				sess.send("You are neither playing, observing nor examining a game.");
				return;
			} else {
				GameInterface gameInterface = gs.map.get(sess);
				if (gameInterface != null) {
					gameNumber = gameInterface.getGameNumber();
				}
			}
		} else if (arguments.matches("\\w{1,17}")) {
			String username = arguments;
			if (username.length() < 2) {
				userSession.send("You need to specify at least two characters of the name.");
				return;
			}
			
			String[] matches = UserService.getInstance().completeHandle(username);
			if (matches.length > 1) {
				StringBuilder toprint = new StringBuilder("Ambiguous name " + 
						username + ":\n-- Matches: " + matches.length + " player(s) --\n");
				for(int i=0;i<matches.length;i++) { 
					toprint.append(matches[i]);
					if (i != matches.length-1) { 
						toprint.append(" "); 
					} 
				}
				userSession.send(toprint.toString());
				return;
			} else if (matches.length == 1) { 
				username = matches[0];
			}
			
			UserService userService = UserService.getInstance();
			if (!userService.isLoggedIn(username)) {
				userSession.send(username.toLowerCase() + " is not logged in.");
				return;
			} else {
				SocketChannelUserSession sess = (SocketChannelUserSession)userService.getUserSession(username);
				if (!sess.isExamining() && !sess.isPlaying()) {
					if (userService.isRegistered(username)) {
						username = userService.correctCapsUsername(username);
					}
					userSession.send(username + " is not playing a game.");
					return;
				}
				GameInterface g = gs.map.get(sess);
				if (g != null) {
					gameNumber = g.getGameNumber();
				} else {
					// this is a bug...
				}
			}	
		} else if (arguments.matches("^\\d+$")) {
			try {
				gameNumber = Integer.parseInt(arguments);
			} catch(NumberFormatException e) {
				userSession.send("There is no such game.");
				return;
			} finally {
				
			}
		} else {
			userSession.send("'" + arguments + "' is not a valid handle.");
		}
		
		if (gameNumber > 0) {
			GameInterface gameInterface = gs.findGameById(gameNumber);
			StyleInterface style = userSession.getUser().getUserVars().getStyle();
			String whiteName = null;
			String blackName = null;
			if (gameInterface instanceof ExaminedGame) {
				ExaminedGame gg = (ExaminedGame)gameInterface;
				whiteName = gg.getWhiteName();
				blackName = gg.getBlackName();
			} else if (gameInterface instanceof Game) {
				Game gg = (Game)gameInterface;
				whiteName = gg.getWhite().getUser().getUserName();
				blackName = gg.getBlack().getUser().getUserName();
			}
			String gameLine = "Game " + gameInterface.getGameNumber() + ": " + whiteName + " (0) " + blackName + " (0) " + (gameInterface.isRated()?"rated":"unrated") + " " + gameInterface.getTime() + " " + gameInterface.getIncrement() + "\n\n";
			String line = style.print(userSession, gameInterface);
			if (!arguments.equals("")) {
				line = gameLine + "\n\n" + line;
			}
			userSession.send(line);
		} else {
			userSession.send(gameNumber + " is not a valid game number.");
		}

		return;
	}

}
