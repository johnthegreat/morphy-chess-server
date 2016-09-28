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

import org.apache.commons.lang.StringUtils;

import morphy.Morphy;
import morphy.channel.Channel;
import morphy.service.ChannelService;
import morphy.service.DatabaseConnectionService;
import morphy.service.ServerListManagerService;
import morphy.service.UserService;
import morphy.user.PersonalList;
import morphy.user.User;
import morphy.user.UserLevel;
import morphy.user.UserSession;
import morphy.utils.john.ServerList;
import morphy.utils.john.ServerList.ListType;

public class AddListCommand extends AbstractCommand {
	public AddListCommand() {
		super("AddList");
	}

	public void process(String arguments, UserSession userSession) {
		
		String[] args = arguments.split(" ");
		if (args.length != 2) {
			userSession.send(getContext().getUsage());
			return;
		}
		String listName = args[0].toLowerCase();
		String value = args[1];
		
		UserService us = UserService.getInstance();
		if (us.isAdmin(userSession.getUser().getUserName())) {
			ServerListManagerService serv = ServerListManagerService.getInstance();
			ServerList s = serv.getList(listName);
			if (s != null) {
				if (userSession.getUser().getUserLevel().ordinal() >= s.getPermissions().ordinal()) {
					if (s.getType().equals(ListType.Integer) && !StringUtils.isNumeric(value)) {
						userSession.send("Bad value provided for that list (Integer required)");
						return;
					} else if (s.getType().equals(ListType.Username) && !us.isValidUsername(value)) {
						userSession.send("Bad value provided for that list (Username required)");
						return;
					} else if (s.getType().equals(ListType.IPAddress) && !value.matches("\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}")) { 
						userSession.send("Bad value provided for that list (IPAddress required)");
						return;
					} else if (s.getType().equals(ListType.String) && value.contains(" ")) {
						userSession.send("Bad value provided for that list (String required)");
						return;
					}
					
					
					if (listName.equals("admin") && !us.isRegistered(value)) {
						userSession.send("Guests cannot be added to that list.");
						return;
					}
					
					if (!serv.isOnList(s,value)) {
						serv.getElements().get(s).add(value);
						userSession.send("[" + value + "] added to the " + listName + " list.");
						UserSession user = us.getUserSession(value);
						if (listName.equals("admin")) { 
							user.getUser().setUserLevel(UserLevel.Admin); 
						}
						if (listName.equals("admin") || listName.equals("sr") || listName.equals("tm") || 
							listName.equals("td") || listName.equals("computer")) {
							if (user != null) {
								user.send("You have been added to the " + listName + " list by " + userSession.getUser().getUserName() + ".");
							}
						}
						return;
					} else {
						userSession.send("[" + value + "] is already on the " + listName + " list.");
						return;
					}
				} else {
					userSession.send("\"" + listName + "\" is not an appropriate list name or you have insufficient rights.");
					return;
				}
			}
		}
		
		listName = listName.toLowerCase();
		PersonalList list = null;
		try {
			list = PersonalList.valueOf(listName);
		} catch (Exception e) {
			userSession.send("\"" + listName
					+ "\" does not match any list name.");
			return;
		}
		
		
		List<String> myList = userSession.getUser().getLists().get(list);
		if (myList == null) {
			myList = new ArrayList<String>(User.MAX_LIST_SIZE);
			userSession.getUser().getLists().put(list, myList);
		}
		if (!myList.contains(value)) {
			if (list == PersonalList.channel) {
				ChannelService cS = ChannelService.getInstance();
				try {
					int intVal = Integer.parseInt(value);
					if (intVal < Channel.MINIMUM || intVal > Channel.MAXIMUM)
						throw new NumberFormatException();
					Channel c = cS.getChannel(intVal);
					if (c != null)
						c.addListener(userSession);
					else
						userSession.send("That channel should, but does not, exist.");
				} catch (NumberFormatException e) {
					userSession
							.send("The channel to add must be a number between "
									+ Channel.MINIMUM + " and " + Channel.MAXIMUM + ".");
					return;
				}
			}

			if (list != PersonalList.channel) {
				String[] matches = us.completeHandle(value);
				//System.err.println(value + " " + java.util.Arrays.toString(matches));
				if (matches.length > 0) {
					if (matches.length == 1) {
						value = matches[0];
					}
				}
			}
			
			myList.add(value);
			userSession.send("[" + value + "] added to your " + listName + " list.");
			
			int dbid = userSession.getUser().getDBID();
			boolean isGuest = dbid == 0;
			if (!isGuest) {
				DatabaseConnectionService dbcs = DatabaseConnectionService.getInstance();
				dbcs.getDBConnection().executeQueryWithRS("INSERT IGNORE INTO personallist VALUES(NULL," + dbid + ",'" + listName + "');");
				Integer listid = userSession.getUser().getPersonalListDBIDs().get(list);
				if (listid == null) {
					java.sql.ResultSet rs = dbcs.getDBConnection().executeQueryWithRS("SELECT `id`,`name` FROM personallist WHERE user_id = '" + dbid + "' && name = '" + listName + "';");
					try {
						if (rs.next()) {
							userSession.getUser().getPersonalListDBIDs().put(PersonalList.valueOf(rs.getString(2)),rs.getInt(1));
							listid = rs.getInt(1);
						}
					} catch(java.sql.SQLException e) { Morphy.getInstance().onError(e); }
				}
				String query = "INSERT INTO personallist_entry VALUES(NULL," + listid + ",'" + value + "');";
				dbcs.getDBConnection().executeQuery(query);
			}
		} else {
			userSession.send("[" + value + "] is already on your " + listName
					+ " list.");
		}
	}

}
