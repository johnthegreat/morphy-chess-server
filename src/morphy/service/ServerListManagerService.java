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
package morphy.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import morphy.user.UserLevel;
import morphy.utils.john.ServerList;
import morphy.utils.john.ServerList.ListType;

public class ServerListManagerService implements Service {
	protected static Log LOG = LogFactory.getLog(ServerListManagerService.class);
	public static enum FlushToDatabase { INSERT,UPDATE; };
	
	private static final ServerListManagerService singletonInstance = 
		new ServerListManagerService();
	
	public static ServerListManagerService getInstance() {
		return singletonInstance;
	}

	private List<ServerList> lists;
	private Map<ServerList,List<String>> elements;
	
	
	public Map<ServerList, List<String>> getElements() {
		return elements;
	}
	
	public List<ServerList> getLists() {
		return lists;
	}

	private ServerListManagerService() {
		HashMap<ServerList,List<String>> map = loadFromDatabase();
		if (map == null || map.isEmpty()) {
			// load defaults
			initialize();
		} else {
			ServerList[] listArr = map.keySet().toArray(new ServerList[map.keySet().size()]);
			lists = new ArrayList<ServerList>(listArr.length);
			for(int i=0;i<listArr.length;i++) {
				ServerList list = listArr[i];
				lists.add(list);
			}
			elements = map;
		}
		
		if (LOG.isInfoEnabled()) {
			LOG.info("Initialized ServerListManagerService.");
		}
	}
	
	private void initialize() {
		lists = new ArrayList<ServerList>();
		
		lists.add(new ServerList("admin",UserLevel.HeadAdmin,ListType.Username,"(*)",true));
		lists.add(new ServerList("removedcom",UserLevel.SuperAdmin,ListType.String,"",false));
		lists.add(new ServerList("filter",UserLevel.SuperAdmin,ListType.IPAddress,"",false));
		lists.add(new ServerList("ban",UserLevel.Admin,ListType.Username,"",false));
		lists.add(new ServerList("noteban",UserLevel.Admin,ListType.Username,"",false));
		lists.add(new ServerList("abuser",UserLevel.Admin,ListType.Username,"",false));
		lists.add(new ServerList("muzzle",UserLevel.Admin,ListType.Username,"",false));
		
		lists.add(new ServerList("FM",UserLevel.Admin,ListType.Username,"(FM)",true));
		lists.add(new ServerList("IM",UserLevel.Admin,ListType.Username,"(IM)",true));
		lists.add(new ServerList("GM",UserLevel.Admin,ListType.Username,"(GM)",true));
		lists.add(new ServerList("WFM",UserLevel.Admin,ListType.Username,"(WFM)",true));
		lists.add(new ServerList("WIM",UserLevel.Admin,ListType.Username,"(WIM)",true));
		lists.add(new ServerList("WGM",UserLevel.Admin,ListType.Username,"(WGM)",true));
		lists.add(new ServerList("Blind",UserLevel.Admin,ListType.Username,"(B)",true));
		lists.add(new ServerList("Team",UserLevel.Admin,ListType.Username,"(T)",true));
		lists.add(new ServerList("Computer",UserLevel.Admin,ListType.Username,"(C)",true));
		lists.add(new ServerList("TM",UserLevel.Admin,ListType.Username,"(TM)",true));
		lists.add(new ServerList("CA",UserLevel.Admin,ListType.Username,"(CA)",true));
		lists.add(new ServerList("SR",UserLevel.Admin,ListType.Username,"(SR)",true));
		lists.add(new ServerList("Demo",UserLevel.Admin,ListType.Username,"(D)",false));
		lists.add(new ServerList("TD",UserLevel.SuperAdmin,ListType.Username,"(TD)",true));
		
		elements = new HashMap<ServerList,List<String>>();
		
		for(ServerList l : lists) {
			elements.put(l,new ArrayList<String>());
		}
	}
	
	private void flushToDatabase(FlushToDatabase what) {
		if (what == null) {
			//
		} else if (what == FlushToDatabase.INSERT) {
			//
			StringBuilder queryBuilder = new StringBuilder();
			queryBuilder.append("INSERT INTO `list` (`id`,`name`) VALUES ");
			ServerList[] lists = getLists().toArray(new ServerList[getLists().size()]);
			for(int i=0;i<lists.length;i++) {
				ServerList list = lists[i];
				queryBuilder.append("(NULL,'" + list.getName() + "')");
			}
		} else if (what == FlushToDatabase.UPDATE) {
			//
		} else {
			// 
		}
	}
	
	private HashMap<ServerList,List<String>> loadFromDatabase() {
		HashMap<ServerList,List<String>> map = new HashMap<ServerList,List<String>>();
		
		return map;
	}
	
	public boolean isOnList(ServerList list,String username) {
		return listContainsIgnoreCase(elements.get(list),username);
	}
	
	/**
	 * Returns if param 'username' is on ANY of param 'lists'.
	 * @param lists
	 * @param username
	 * @return
	 */
	public boolean isOnAnyList(ServerList[] lists,String username) {
		boolean is = false;
		for(ServerList list : lists) {
			is = listContainsIgnoreCase(elements.get(list),username);
			if (is) return true;
		}
		return is;
	}
	
	public ServerList getList(String name) {
		for(ServerList list : lists) {
			if (list.getName().equalsIgnoreCase(name))
				return list;
		}
		return null;
	}
	
	private boolean listContainsIgnoreCase(List<String> list,String element) {
		for(String s : list) {
			if (s.equalsIgnoreCase(element))
				return true;
		}
		return false;
	}
	
	public void dispose() {
		flushToDatabase(null);
		
		if (lists != null) {
			lists.clear();
		}
		if (elements != null) {
			elements.clear();
		}
	}

}
