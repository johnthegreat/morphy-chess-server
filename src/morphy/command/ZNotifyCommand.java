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

import morphy.service.UserService;
import morphy.user.PersonalList;
import morphy.user.UserSession;

public class ZNotifyCommand extends AbstractCommand {
	public ZNotifyCommand() {
		super("znotify");
	}

	public void process(String arguments, UserSession userSession) {
		String[] args = arguments.split(" ");
		if (args.length != 1) {
			userSession.send(getContext().getUsage());
			return;
		}
		
		StringBuilder b = new StringBuilder();
		List<String> l = userSession.getUser().getLists().get(PersonalList.notify);
		if (l.size() == 0 || areAllDisconnected(l)) {
			b.append("No one from your notify list is logged on.\n");
		} else {
			b.append("Present company on your notify list:\n   ");
			for(int i=0;i<l.size();i++) {
				String username = l.get(i);
				System.err.println(username);
				UserSession s = UserService.getInstance().getUserSession(username);
				if (s != null) { // in case player is offline
					b.append(username);
					if (s.getIdleTimeMillis() > 60000) {
						b.append("(idle:" + (s.getIdleTimeMillis()/60000) + "m)");
					}
				}
				if (i != l.size()-1)
					b.append(" ");
			}
			b.append("\n");
		}
		
		String names = getNames(userSession.getUser().getUserName());
		if (names.equals("")) {
			b.append("No one logged in has you on their notify list.");
		} else {
			b.append("The following players have you on their notify list:\n   ");
			b.append(names);
		}
		
		userSession.send(b.toString());
		
	}
	
	/** 
	 * Gets the usernames (and idle times) of people who have <tt>username</tt> on their notify list.
	 * Returns empty string if noone. 
	 */
	private String getNames(String username) {
		StringBuilder b = new StringBuilder();
		final UserSession[] arr = UserService.getInstance().getLoggedInUsers();
		java.util.Arrays.sort(arr);
		for(int i=0;i<arr.length;i++) {
			UserSession s = arr[i];
			List<String> l = s.getUser().getLists().get(PersonalList.notify);
			if (l.contains(username)) {
				b.append(s.getUser().getUserName());
				if (s.getIdleTimeMillis() > 60000) {
					b.append("((idle:" + (s.getIdleTimeMillis()/60000) + "m)");
				}
				
				if (i != l.size()-1)
					b.append(" ");
			}
		}
		return b.toString();
	}
	
	private boolean areAllDisconnected(List<String> list) {
		if (list == null) 
			return true;
		
		for(String username : list) {
			if (UserService.getInstance().isLoggedIn(username))
				return false;
		}
		return true;
	}
}
