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

import morphy.game.ExaminedGame;
import morphy.game.GameInterface;
import morphy.service.GameService;
import morphy.user.SocketChannelUserSession;
import morphy.user.UserSession;

public class BclockCommand extends AbstractCommand {
	
	public BclockCommand() {
		super("bclock");
	}
	
	public void process(String arguments, UserSession userSession) {
		arguments = arguments.trim();
		GameService gs = GameService.getInstance();
		
		GameInterface g = gs.map.get(userSession);
		
		if (g == null || !((SocketChannelUserSession)userSession).isExamining()) {
			userSession.send("You are not examining or setting up a game.");
			return;
		}
		
		if (g instanceof ExaminedGame) {
			
			int hours = 0,minutes = 0,seconds = 0;
			
			final String errmess = "You must specify the time as (-)mm:ss or (-)hh:mm:ss.";
			
			String[] args = arguments.split(":");
			//System.err.println(java.util.Arrays.toString(args));
			if (args.length <= 1 || args.length > 3 || arguments.startsWith(":") || arguments.endsWith(":")) {
				userSession.send(errmess);
				return;
			}
			
			int positive = 1;
			
			if (args.length == 2) {
				if (args[0].equals("") || args[1].equals("")) {
					userSession.send(errmess);
					return;
				}
				
				if (args[0].substring(0,1).equals("-")) { positive = -1; args[0] = args[0].substring(1); }
				minutes = Integer.parseInt(args[0]);
				seconds = Integer.parseInt(args[1]);
				
				if (minutes > 59 || seconds > 59) {
					userSession.send(errmess);
					return;
				}
			}
			if (args.length == 3) {
				if (args[0].equals("") || args[1].equals("") || args[2].equals("")) {
					userSession.send(errmess);
					return;
				}
				
				if (args[0].substring(0,1).equals("-")) { positive = -1; args[0] = args[0].substring(1); }
				hours = Integer.parseInt(args[0]);
				minutes = Integer.parseInt(args[1]);
				seconds = Integer.parseInt(args[2]);
				
				if (minutes > 59 || seconds > 59) {
					userSession.send(errmess);
					return;
				}
				
				if (hours > 9) { 
					userSession.send("You must specify a smaller time.");
					return;
				}
			}

			ExaminedGame gg = (ExaminedGame) g;
			gg.setBlackClock(((hours*60*60) + (minutes*60) + (seconds)) * 1000 * positive);
			gg.processMoveUpdate(true);
			
			StringBuilder b = new StringBuilder();
			b.append((hours!=0&&hours<10?"0"+hours:hours)+":");
			b.append((minutes<10?"0"+minutes:minutes)+":");
			b.append((seconds<10?"0"+seconds:seconds));
			final String line = "Game " + gg.getGameNumber() + ": " + userSession.getUser().getUserName() + " sets black's clock to " + b.toString() + ".";
			
			
			
			UserSession[] examiners = gg.getExaminers();
			for(int i=0;i<examiners.length;i++) {
				examiners[i].send(line);
			}

			UserSession[] observers = gg.getObservers();
			for(int i=0;i<observers.length;i++) {
				observers[i].send(line);
			}
		}
	}

}
