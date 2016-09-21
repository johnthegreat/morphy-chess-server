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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import morphy.game.ExaminedGame;
import morphy.game.Game;
import morphy.service.GameService;
import morphy.service.UserService;
import morphy.user.SocketChannelUserSession;
import morphy.user.UserInfoList;
import morphy.user.UserLevel;
import morphy.user.UserSession;
import morphy.utils.MorphyStringUtils;

public class FingerCommand extends AbstractCommand {

	public FingerCommand() {
		super("finger");
	}
	
	public void process(String arguments, UserSession userSession) {
		int pos = arguments.indexOf(" ");
		if (arguments.equals("")) {
			process(userSession.getUser().getUserName(),userSession);
			return;
		}
		
		UserService userService = UserService.getInstance();
		
		String user = arguments.substring(0,((pos == -1) ? arguments.length() : pos));
		
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
			
		boolean showRatings = true;
		boolean showNotes = true;
		
		if (pos != -1) {
		String flags = arguments.substring(pos);
			//finger [user] [/[b][s][l][w][B][S]] [r][n]
			if (flags.contains("r")) { showNotes = false; } // don't show notes
			if (flags.contains("n")) { showRatings = false; } // don't show ratings
		}
		
		StringBuilder str = new StringBuilder(200);
		SocketChannelUserSession query = (SocketChannelUserSession)userService.getUserSession(user);
		String busyString = "";
		if (query.isConnected()) {
			// avoid NullPointerException
			busyString = query.getUser().getUserVars().getVariables().get("busy");
		}
		
		str.append("Finger of " + userService.getTags(query.getUser().getUserName()) + ":\n");
		if (!busyString.equals(""))
			str.append("(" + query.getUser().getUserName() + " " + busyString + ")\n");
		str.append("\n");
		
		long loggedInMillis = System.currentTimeMillis() - query.getLoginTime();
		long idleTimeMillis = query.getIdleTimeMillis();
		str.append("On for: "
				+ MorphyStringUtils.formatTime(loggedInMillis)
				+ "\tIdle: "
				+ ((idleTimeMillis <= 999) ? "0 secs" : MorphyStringUtils.formatTime(idleTimeMillis)));
		GameService gs = GameService.getInstance();
		if (query.isPlaying()) {	
			Game g = (Game)gs.map.get(query);
			if (g != null) {
				str.append("\n(playing game " + g.getGameNumber() + ": " + g.getWhite().getUser().getUserName() + " vs. " + g.getBlack().getUser().getUserName() + ")\n");
				//str.append("\n(partner is playing game 331: BlindJKiller vs. XeRcHeSs)\n");
				List<Integer> list = query.getGamesObserving();
				if (list.size() > 0) {
					str.append("\n(" + query.getUser().getUserName() + " is observing game(s)");
					for(int i=0;i<list.size();i++) {
						str.append(" " + i + "");
						if (i == list.size()-2) str.append(" and");
						if (i != list.size()-2) str.append(",");
					}
				}
			}
			
		} else if (query.isExamining()) {
			ExaminedGame g = (ExaminedGame)gs.map.get(query);
			str.append("\n(examining game " + g.getGameNumber() + ": " + g.getWhiteName() + " vs. " + g.getBlackName() + ")\n");
		}
		str.append("\n\n");
		
		if (showRatings) {
			str.append(String.format("%15s %7s %7s %7s %7s %7s %7s","rating","RD","win","loss","draw","total","best") + "\n");
			
			// variants, ratings
		}
		
		
		
		
		if (userSession.getUser().getUserName().equals(query.getUser().getUserName()) || 
			userService.isAdmin(userSession.getUser().getUserName())) {
			
			if(query.getUser().isRegistered()) {
				SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd, HH:mm z yyyy");
				
				str.append("\n\nEmail      : " + query.getUser().getEmail() + "\n\n");
				str.append("Total time online: xxxx\n" +
					"% of life online:  xx.x  (since " + sdf.format(query.getUser().getRegisteredSince()) + ")");
				} else {
				str.append("Total time online: " + MorphyStringUtils.formatTime(loggedInMillis) + "\n");
			}
		}

		str.append("\n\n");
		UserLevel lvl = query.getUser().getUserLevel();
		if (lvl == UserLevel.Admin || lvl == UserLevel.SuperAdmin || lvl == UserLevel.HeadAdmin) {
			str.append("Admin Level: ");
			if (lvl == UserLevel.Admin) str.append("Administrator");
			if (lvl == UserLevel.SuperAdmin) str.append("Senior Administrator");
			if (lvl == UserLevel.HeadAdmin) str.append("Head Administrator");
			str.append("\n\n");
		}
		str.append("Timeseal 1 : Off\n\n");
		
		if (showNotes) {
			List<String> notes = query.getUser().getUserInfoLists().get(UserInfoList.notes);
			if (notes == null) {
				notes = new ArrayList<String>(UserInfoList.MAX_NOTES);
				userSession.getUser().getUserInfoLists().put(UserInfoList.notes,notes);
			}
			for(int i=0;i< (notes.size()) ;i++) {
				String note = notes.get(i);
				if (!note.equals(""))
					str.append(format(i+1) + ": " + note + "\n");
			}
		}
		
		userSession.send(str.toString());
	}

	private String format(int x) {
		if (x < 10) 
		 return " " + x; 
		else 
		 return "" + x;
	}
}
