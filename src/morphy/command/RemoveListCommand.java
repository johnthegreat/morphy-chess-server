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

import java.util.ArrayList;
import java.util.List;

import morphy.channel.Channel;
import morphy.service.ChannelService;
import morphy.service.DBConnectionService;
import morphy.service.ServerListManagerService;
import morphy.service.UserService;
import morphy.user.PersonalList;
import morphy.user.User;
import morphy.user.UserLevel;
import morphy.user.UserSession;
import morphy.utils.john.ServerList;

public class RemoveListCommand extends AbstractCommand {
	public RemoveListCommand() {
		super("RemoveList");
	}

	public void process(String arguments, UserSession userSession) {
		String[] args = arguments.split(" ");
		if (args.length != 2) {
			userSession.send(getContext().getUsage());
			return;
		}
		ServerListManagerService serv = ServerListManagerService.getInstance();

		String listName = args[0].toLowerCase();
		String value = args[1];

		PersonalList list = null;
		ServerList serverList = null;
		try {
			list = PersonalList.valueOf(listName);
		} catch (Exception e) {
			serverList = serv.getList(listName);
			if (serverList == null) {
				userSession.send("\"" + listName
						+ "\" does not match any list name.");
				return;
			}
			if (userSession.getUser().getUserLevel().ordinal() < serverList.getPermissions().ordinal()) {
				userSession.send("\"" + listName + "\" is not an appropriate list name or you have insufficient rights.");
				return;
			}
		}

		List<String> myList = null;

		if (list != null) {
			myList = userSession.getUser().getLists().get(list);
			if (myList == null) {
				myList = new ArrayList<String>(User.MAX_LIST_SIZE);
				userSession.getUser().getLists().put(list, myList);
			}
		} else if (serverList != null) {
			myList = serv.getElements().get(serverList);
		}

		if (value.equals("*")) {
			myList.clear();
			userSession.send("All players have been removed from "
					+ ((list == null) ? "the" : "your") + " " + listName
					+ " list.");
			return;
		}

		if (myList.contains(value)) {
			if (list == PersonalList.channel) {
				ChannelService cS = ChannelService.getInstance();
				try {
					boolean isBase16 = value.startsWith("0x");
					if (isBase16)
						value = value.substring(2);
					int intVal = Integer.parseInt(value, isBase16 ? 16 : 10);
					if (intVal < Channel.MINIMUM || intVal > Channel.MAXIMUM)
						throw new NumberFormatException();
					Channel c = cS.getChannel(intVal);
					if (c == null) {
						userSession
								.send("That channel should, but does not, exist.");
						return;
					} else {
						c.removeListener(userSession);
					}

				} catch (NumberFormatException e) {
					userSession
							.send("The channel to remove must be a number between "
									+ Channel.MINIMUM
									+ " and "
									+ Channel.MAXIMUM + ".");
					return;
				}
			}

			if (serverList != null && userSession.getUser().getUserLevel().ordinal() >= serverList.getPermissions().ordinal()) {
				myList.remove(value);
				UserSession user = UserService.getInstance().getUserSession(value);
				if (listName.equals("admin")) {
					user.getUser().setUserLevel(UserLevel.Player); 
				}
				if (listName.equals("admin") || listName.equals("sr") || listName.equals("tm") || 
					listName.equals("td") || listName.equals("computer")) {
					if (user != null) {
						user.send("You have been removed from the " + listName + " list by " + userSession.getUser().getUserName() + ".");
					}
				}
				
			} else if (list != null) {
				myList.remove(value);
			}
			
			userSession.send("[" + value + "] removed from " + ((list == null) ? "the" : "your") + " " + listName
					+ " list.");
			
			int dbid = userSession.getUser().getDBID();
			boolean isGuest = dbid == 0;
			if (!isGuest) {
				DBConnectionService dbcs = DBConnectionService.getInstance();
				String query = "DELETE FROM personallist_entry WHERE personallist_id = " + userSession.getUser().getPersonalListDBIDs().get(list) + " && value = '" + value + "';";
				dbcs.getDBConnection().executeQuery(query);
			}
		} else {
			userSession.send("[" + value + "] is not " + ((list == null) ? "on the" : "in your") + " " + listName
					+ " list.");
		}
	}
}
