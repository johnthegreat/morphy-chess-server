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

import morphy.game.ExaminedGame;
import morphy.game.Game;
import morphy.game.GameInterface;
import morphy.service.GameService;
import morphy.user.UserSession;

public class GamesCommand extends AbstractCommand {

	public GamesCommand() {
		super("games");
	}
	
	public void process(String arguments, UserSession userSession) {
//		int pos = arguments.indexOf(" ");
//		if (arguments.equals("")) {
//			process(userSession.getUser().getUserName(),userSession);
//			return;
//		}

//		 2 (Exam. 2531 GMKanep     2109 PerttuAntt) [ su120   0] W: 25
//		30 2677 GMFressinet 2705 GMBacrot   [ su120   0] 1:31:00 -1:32:00 (39-39) B:  7
		
		
		GameService gs = GameService.getInstance();
		List<GameInterface> list = gs.getGames();
		if (list.size() == 0) {
			userSession.send("There are no games in progress.");
			return;
		}
		java.util.Collections.sort(list);
		
		StringBuilder b = new StringBuilder();
		for(int i=0;i<list.size();i++) {
			GameInterface g = list.get(i);
			if (g instanceof Game) {
				String whiteUsername = chompUsername(((Game)g).getWhite().getUser().getUserName(),11);
				String blackUsername = chompUsername(((Game)g).getBlack().getUser().getUserName(),11);
				b.append(String.format("%3d ---- %-11s ---- %-11s [ %3d %3d] x:xx:xx x:xx:xx (%2d-%2d)\n",g.getGameNumber(),whiteUsername,blackUsername,g.getTime(),g.getIncrement(),g.getWhiteBoardStrength(),g.getBlackBoardStrength()));
			}
			if (g instanceof ExaminedGame) {
				ExaminedGame gg = (ExaminedGame)g;
				String whiteUsername = chompUsername(gg.getWhiteName(),11);
				String blackUsername = chompUsername(gg.getBlackName(),11);
				b.append(String.format("%3d (Exam. %4d %-11s %4d %-11s) [ uu%3d %3d] \n",gg.getGameNumber(),0,whiteUsername,0,blackUsername,gg.getTime(),gg.getIncrement()));
			}
		}
		
		userSession.send(b.toString());
	}
	
	public static String chompUsername(String username,int len) {
		if (username.length() > len) return username.substring(0,len); else return username;
	}

}
