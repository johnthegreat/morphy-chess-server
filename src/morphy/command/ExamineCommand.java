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

import morphy.service.GameService;
import morphy.user.SocketChannelUserSession;
import morphy.user.UserSession;

public class ExamineCommand extends AbstractCommand {

	public ExamineCommand() {
		super("examine");
	}
	
	public void process(String arguments, UserSession userSession) {
		
		SocketChannelUserSession sess = (SocketChannelUserSession)userSession;
		if (sess.isPlaying()) {
			userSession.send("You are playing a game.");
			return;
		}
		if (sess.isExamining()) {
			userSession.send("You are already examining a game.");
			return;
		}
		
		if (!arguments.equals("")) {
			if (arguments.matches("\\w{3,17}")) {
				userSession.send("There is no stored game " + arguments + " vs. " + userSession.getUser().getUserName());
				return;
			}
		}
		
		GameService.getInstance().createExaminedGame(userSession);
	}
}
