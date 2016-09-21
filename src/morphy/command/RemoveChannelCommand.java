/*
 *   Morphy Open Source Chess Server
 *   Copyright (C) 2008,2009  http://code.google.com/p/morphy-chess-server/
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

import morphy.channel.Channel;
import morphy.service.ChannelService;
import morphy.user.PersonalList;
import morphy.user.UserSession;

@Deprecated
/**
 * Deprecated in favor of the removelist command.
 */
public class RemoveChannelCommand extends AbstractCommand {

	public RemoveChannelCommand() {
		super("RemoveChannel");
	}

	public void process(String arguments, UserSession userSession) {
		String[] args = arguments.split(" ");
		try {
			int chNum = Integer.parseInt(args[args.length - 1]);
			ChannelService cS = ChannelService.getInstance();
			Channel c = cS.getChannel(chNum);
			if (c != null) {
				if (!userSession.getUser().getLists().get(PersonalList.channel)
						.contains("" + chNum)) {
					userSession.send("[" + chNum
							+ "] is not in your channel list.");
					return;
				}
				c.removeListener(userSession);
				userSession.getUser().getLists().get(PersonalList.channel)
						.remove("" + chNum);
				userSession.send("[" + c.getNumber()
						+ "] removed from your channel list.");
			}
		} catch (NumberFormatException e) {
			userSession.send("The channel to remove must be a number between "
					+ Channel.MINIMUM + " and " + Channel.MAXIMUM + ".");
		}
	}

}
