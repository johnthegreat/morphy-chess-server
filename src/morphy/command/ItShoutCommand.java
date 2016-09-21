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

import morphy.service.UserService;
import morphy.user.PersonalList;
import morphy.user.UserSession;

public class ItShoutCommand extends AbstractCommand {
	public ItShoutCommand() {
		super("itshout");
	}

	public void process(String arguments, UserSession userSession) {
		if (arguments.equals("")) {
			userSession.send(getContext().getUsage());
			return;
		}

		if (!userSession.getUser().isRegistered()) {
			userSession.send("Only registered players can use the it command.");
			return;
		}

		UserSession[] sessions = UserService.getInstance().getLoggedInUsers();
		int sentTo = 0;

		if (arguments.startsWith("(")) {
			userSession.send("You may not start your shout with a bracket.");
			return;
		}

		final String message = "--> "
				+ UserService.getInstance().getTags(
						userSession.getUser().getUserName()) + " " + arguments;
		for (UserSession session : sessions) {
			if (session.getUser().getUserVars().getVariables().get("shout")
					.equals("1")) {
				if (session == userSession) {
					continue;
				} else {
					if (userSession.getUser().isOnList(PersonalList.censor,
							session.getUser().getUserName())) {
						continue;
					}
					session.send(message);
					sentTo++;
				}
			}
		}

		final String shoutedMessage = "(shouted to " + sentTo + " players)";
		if (userSession.getUser().getUserVars().getVariables().get("echo")
				.equals("1"))
			userSession.send(message + "\n" + shoutedMessage);
		else
			userSession.send(shoutedMessage);
	}
}
