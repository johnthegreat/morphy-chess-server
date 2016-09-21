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

import org.apache.commons.lang.StringUtils;

import morphy.service.UserService;
import morphy.user.SocketChannelUserSession;
import morphy.user.UserSession;

public class BugWhoCommand extends AbstractCommand {

	public BugWhoCommand() {
		super("bugwho");
	}
	
	public void process(String arguments, UserSession userSession) {	
		
		boolean showGames = false;
		boolean showPartnerships = false;
		boolean showUnpartnered = false;
		
		if (arguments.equals("")) {
			arguments = "gpu";
		}
		
		if (arguments.contains("g")) {
			showGames = true;
		}
		if (arguments.contains("p")) {
			showPartnerships = true;
		}
		if (arguments.contains("u")) {
			showUnpartnered = true;
		}
		
		StringBuilder b = new StringBuilder();
		
		if (showGames) {
			b.append("Bughouse games in progress\n");
			b.append(String.format("%3d",160) + " 1770 knighttour  1680 BishopBlud [pBu  2   0]   0:58 -  0:14 (35-23) W: 27\n");
			b.append(String.format("%3d",179) + " 1486 EagleMorphy ++++ DogWithSky [pBu  2   0]   1:09 -  0:22 (43-55) B: 20\n");
			b.append("\n"+String.format("%2d",1) + " game displayed.\n\n");
		}
		
		if (showPartnerships) {
			UserService us = UserService.getInstance();
			UserSession u = us.getUserSession("johnthegreat");
			b.append("Partnerships not playing bughouse\n");
			b.append(String.format("%4s","9999") + " " + getChar(u) + StringUtils.rightPad(us.getTags(u.getUser().getUserName()), 17));
			b.append(" / 2789:ChIcKeNcRoSsRoAd(FM)(CA)");
			
			b.append("\n\n" + String.format("%2d",1) + " partnerships displayed.\n\n");
		}

		if (showUnpartnered) {
			b.append("Unpartnered players with bugopen on\n\n");
			b.append("2789:ChIcKeNcRoSsRoAd(FM)(CA)  1369^bachio");
			b.append("\n\n 71 players displayed (of 1702). (*) indicates system administrator.");
		}
		
		
		
		userSession.send(b.toString());
	}
	
	private String getChar(UserSession u) {
		if (u == null) return null;
		String pChar = " ";
		SocketChannelUserSession s = (SocketChannelUserSession)u;
		if (s.isPlaying()) { pChar = "^"; } else
		if (s.isExamining()) { pChar = "#"; } else
		if (s.getUser().getUserVars().getVariables().get("open").equals("0")) { pChar = ":"; } else
		if (s.getIdleTimeMillis() > 300000 || 
			!s.getUser().getUserVars().getVariables().get("busy").equals("")) { pChar = "."; } else
		if (s.getUser().getUserVars().getVariables().get("tourney").equals("1")) { pChar = "&"; }
		return pChar;
	}
}
