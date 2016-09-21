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
import java.util.Collections;
import java.util.List;

import morphy.service.ServerListManagerService;
import morphy.user.PersonalList;
import morphy.user.User;
import morphy.user.UserSession;
import morphy.utils.MorphyStringUtils;
import morphy.utils.john.ServerList;

public class ShowListCommand extends AbstractCommand {
	public ShowListCommand() {
		super("ShowList");
	}

	public void process(String arguments, UserSession userSession) {
		if (arguments.indexOf(" ") != -1)
			arguments = arguments.substring(0, arguments.indexOf(" "));
		arguments = arguments.trim();
		
		String listName = arguments.toLowerCase();
		ServerListManagerService serv = ServerListManagerService.getInstance();
		if (listName.equals("")) {
			StringBuilder str = new StringBuilder();
			str.append("Lists:\n\n");

			List<ServerList> list = serv.getLists();
			for (int i = 0; i < list.size(); i++) {
				ServerList l = list.get(i);
				if (l.isPublic() && !l.getName().equals("admin")) {
					str.append(String.format("%-20s is %s", l.getName()
							.toLowerCase(), "PUBLIC"));
					if (i != list.size() - 1)
						str.append("\n");
				}
			}
			str.append("\n");

			PersonalList[] arr = PersonalList.values();
			for (int i = 0; i < arr.length; i++) {
				str.append(String.format("%-20s is %s", arr[i].name(),
						"PERSONAL"));
				if (i != arr.length - 1)
					str.append("\n");
			}
			userSession.send(str.toString());
			return;
		}

		
		
		PersonalList list = null;
		ServerList serverList = null;
		
		PersonalList[] arr = getLists(listName);
		if (arr.length >= 2) {
			userSession.send("Ambiguous list - matches: " + MorphyStringUtils.toDelimitedString(arr, ", ") + ".");
			return;
		}
		if (arr.length == 1) {
			list = arr[0];
		}
		
		if (arr.length == 0) {
			// try parsing for serverList
			//System.out.println("\"" + listName + "\"");
			ServerList[] sl_arr = getLists(serv, listName);
			if (sl_arr.length >= 2) {
			
				return;
			}
			if (sl_arr.length == 1) {
				serverList = sl_arr[0];
			}
			if (sl_arr.length == 0) {
				userSession.send("\"" + listName
					+ "\" does not match any list name.");
				return;
			}
 		}
		
//		try {
//			list = PersonalList.valueOf(listName);
//		} catch (Exception e) {
//			serverList = serv.getList(listName);
//			if (serverList == null) {
//				userSession.send("\"" + listName
//						+ "\" does not match any list name.");
//				return;
//			}
//		}
		
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
		Collections.sort(myList);
		showList(userSession, listName, myList
				.toArray(new String[myList.size()]));
	}
	
	private PersonalList[] getLists(String partial) {
		PersonalList[] values = PersonalList.values();
		List<PersonalList> retList = new ArrayList<PersonalList>(values.length);
		for(PersonalList l : values) {
			if (l.name().toLowerCase().startsWith(partial.toLowerCase())) {
				retList.add(l);
			}
		}
		return retList.toArray(new PersonalList[retList.size()]);
	}
	
	private ServerList[] getLists(ServerListManagerService instance,String partial) {
		ServerList[] values = instance.getLists().toArray(new ServerList[instance.getLists().size()]);
		List<ServerList> retList = new ArrayList<ServerList>(values.length);
		for(ServerList l : values) {
			if (l.getName().toLowerCase().startsWith(partial.toLowerCase())) {
				retList.add(l);
			}
		}
		return retList.toArray(new ServerList[retList.size()]);
	}

	private void showList(UserSession userSession, String listName,
			String[] elements) {
		String what = "names";
		if (listName.equals("filter")) {
			what = "ips";
		} else if (listName.equals("channel")) {
			what = "channels";
		} else if (listName.equals("removedcom")) {
			what = "commands";
		}

		StringBuilder str = new StringBuilder(60);
		str.append("-- " + listName + " list: " + elements.length + " " + what
				+ " --\n");
		for (int i = 0; i < elements.length; i++) {
			str.append(elements[i]);
			if (i != elements.length - 1)
				str.append(" ");
		}
		userSession.send(str.toString());
	}

}
