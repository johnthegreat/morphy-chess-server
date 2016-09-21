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

import morphy.channel.Channel;
import morphy.service.ChannelService;
import morphy.service.HighlightService;
import morphy.service.UserService;
import morphy.user.PersonalList;
import morphy.user.SocketChannelUserSession;
import morphy.user.UserSession;
import morphy.user.UserVars;
import morphy.utils.MorphyStringUtils;

public class TellCommand extends AbstractCommand {
	public TellCommand() {
		super("tell");
	}

	public void process(String arguments, UserSession userSession) {
		int spaceIndex = arguments.indexOf(' ');
		if (spaceIndex == -1) {
			userSession.send(getContext().getUsage());
		} else {
			String userName = arguments.substring(0, spaceIndex);
			String message = arguments.substring(spaceIndex + 1, arguments
					.length());

			if (userName.matches("[0-9]+") || userName.matches("0x[0-9a-fA-F]+")) {
				
				ChannelService channelService = ChannelService.getInstance();
				int number = 0x0;
				if (userName.startsWith("0x"))
					{ number = Integer.parseInt(userName.substring(2),16); }
				else
					{ number = Integer.parseInt(userName); }
				Channel c = channelService.getChannel(number);
				if (number < Channel.MINIMUM || number > Channel.MAXIMUM) {
					userSession.send("The range of channels is " + Channel.MINIMUM + " to " + Channel.MAXIMUM +".");
				} else if (c == null) {
					userSession.send("That channel should, but does not, exist.");
				}
				else {
					if (!c.hasAccess(userSession.getUser())) {
						// Only service representatives and admins may send tells to channel 5.
						/* I want to make this as flexible, hard coding as little as possible.
						 * This default will currently be incorrect for some channels 
						 * and will need to be changed later.
						 */
						String msg = "admins"; // authorized users
						userSession.send("Only " + msg + " may send tells to channel " + number + ".");
						return;
					}

					int sentTo = channelService.tell(c, message, userSession);
					String tosend = "(told " + sentTo + " players in channel "
							+ c.getNumber() + " \"" + c.getName() + "\")";
					if (!c.getListeners().contains(userSession)) {
						tosend += " (You're not listening.)";
					}
							
					userSession.send(tosend);
					// The ONLY difference between xtell and tell is removing this line...
					((morphy.user.SocketChannelUserSession)userSession).setLastChannelToldTo(c);
				}
			} else {
				String[] matches = UserService.getInstance().completeHandle(userName);
				if (matches.length > 1) {
					userSession.send("Ambiguous handle \"" + userName + "\". Matches: " + MorphyStringUtils.toDelimitedString(matches," "));
					return;
				} else if (matches.length == 1) {
					userName = matches[0];
				}
				
				/*if (userName.contains(",")) { 
					String[] matches = userName.split(",");
					
				}*/
				
				UserSession personToTell = UserService.getInstance()
						.getUserSession(userName);
				if (personToTell == null) {
					userSession.send("" + userName.toLowerCase() + " is not logged in.");
					return;
				} else {
					if (personToTell.getUser().isOnList(PersonalList.censor,
							userSession.getUser().getUserName())) {
						userSession.send("Player \""
								+ personToTell.getUser().getUserName()
								+ "\" is censoring you.");
						return;
					}
					
					UserVars uv = personToTell.getUser().getUserVars();
					
					if (!userSession.getUser().isRegistered()
							&& uv.getVariables().get("tell").equals("0")) {
						userSession
								.send("Player \""
										+ personToTell.getUser().getUserName()
										+ "\" isn't listening to unregistered user's tells.");
						return;
					}
					
					boolean highlight = !uv.getVariables().get("highlight").equals("0");
					HighlightService hl = HighlightService.getSingletonInstance();
					String line = (highlight?hl.before(userSession):"") +
						UserService.getInstance().getTags(userSession.getUser().getUserName()) +
						(highlight?hl.after():"") + " tells you: " + message;
					personToTell.send(line);
					
					String s = "(told "
							+ personToTell.getUser().getUserName() + "";
					
					boolean isPlaying = ((SocketChannelUserSession)personToTell).isPlaying();
					boolean isExamining = ((SocketChannelUserSession)personToTell).isExamining();
					
					if (isPlaying) {
						s += ", who is playing";
					} else if (isExamining) {
						s += ", who is examining a game";
					} else {
						String busyString = uv.getVariables().get("busy");
						int minutes = (int)(personToTell.getIdleTimeMillis()/60000);
						
						if (!busyString.equals("")) {
							s += ", who " + busyString + " ";
							
							int seconds = (int)(personToTell.getIdleTimeMillis()/1000);
							if (minutes > 0) { s += "(idle: " + minutes + " mins)"; }
							else { s += " (idle: " + seconds + " secs)"; }
						} else if (minutes >= 5) {
							s += ", who has been idle for " + minutes + " mins";
						}
						
					}
					
					userSession.send(s + ")");
					
					// The ONLY difference between xtell and tell is removing this line...
					((morphy.user.SocketChannelUserSession)userSession).setLastPersonToldTo(personToTell);
				}
			}
		}
	}
}
