/*
 *   Morphy Open Source Chess Server
 *   Copyright (C) 2016 http://code.google.com/p/morphy-chess-server/
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

import morphy.game.Seek;
import morphy.service.SeekService;
import morphy.service.ServerListManagerService;
import morphy.service.UserService;
import morphy.user.UserSession;

public class SoughtCommand extends AbstractCommand {
	public SoughtCommand() {
		super("sought");
	}

	public void process(String arguments, UserSession userSession) {
		// sought (only seeks matching your formula)
		// sought all (all seeks)
		
		final SeekService seekService = SeekService.getInstance();
		
		arguments = arguments.toLowerCase();
		
		// NOTE: the rating column width is different if provshow=1 than if provshow=0
		// NOTE: server tags are not shown, EXCEPT for computer (C).
		
		Seek[] allSeeks = seekService.getAllSeeks();
		
		String messageToSend = composeSoughtString(seekService, allSeeks);
		userSession.send(messageToSend);
		
//		if (arguments.equals("all")) {
//			Seek[] allSeeks = seekService.getAllSeeks();
//		}
		
	}
	
	private String composeSoughtString(SeekService seekService, Seek[] seeks) {
		final UserService userService = UserService.getInstance();
		
		StringBuilder messageToSendBuilder = new StringBuilder();
		
		for (Seek seek : seeks) {
			final String seekUsername = seek.getUserSession().getUser().getUserName();
			final String seekRatingLimit = "0-9999"; // TODO
			final String seekFlags = (seek.isUseManual() ? "m" : "") + (seek.isUseFormula() ? "f" : "");
			
			//  29 2636  Knightsmasher(C)    1   0 rated   lightning              0-9999 f
			//  83 ++++  robbodrone          1   1 unrated odds/queen-and-move [black]     0-9999 mf
			String seekLine = String.format("%3d %s %20s %3d %3d %7s %22s %9s %s\n",
					seek.getSeekIndex(), "++++", seekUsername + (userService.isComputer(seekUsername) ? "(C)" : ""),
					seek.getSeekParams().getTime(), seek.getSeekParams().getIncrement(), seek.getSeekParams().isRated() ? "rated" : "unrated",
					seek.getSeekParams().getVariant().name(), seekRatingLimit, seekFlags);
			
			messageToSendBuilder.append(seekLine);
		}
		
		messageToSendBuilder.append(String.format("%d ads displayed.", seeks.length));
		
		return messageToSendBuilder.toString();
	}
}
