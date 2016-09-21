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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import morphy.channel.Channel;
import morphy.user.PersonalList;
import morphy.user.UserLevel;
import morphy.user.UserSession;
import morphy.utils.john.DBConnection;
import morphy.utils.john.ServerList;

public class ChannelService implements Service {
	protected static Log LOG = LogFactory.getLog(ChannelService.class);
	public static final int MAX_NUM_CHANNELS = 30;
	
	private static final ChannelService singletonInstance = new ChannelService();
	private ServerListManagerService listManager = ServerListManagerService
			.getInstance();

	public static ChannelService getInstance() {
		return singletonInstance;
	}

	private List<Channel> channels = new ArrayList<Channel>(1);

	private ChannelService() {
		addChannel(new Channel(0, "Admins", "", UserLevel.Admin, new ServerList[] { listManager.getList("admin") }));
		addChannel(new Channel(1, "Help", "The help channel.",
				UserLevel.Player, null));
		addChannel(new Channel(5, "Service Representitives", "SRs",
				UserLevel.Player, new ServerList[] { listManager.getList("SR"),
						listManager.getList("admin") }));
		addChannel(new Channel(255,"","",UserLevel.Player,null));
		loadChannelsFromDatabase();
		// getChannel(1).addListener();
	}

	public void addChannel(Channel c) {
		c.setName(c.getName().replace(" ","_"));
		getChannels().add(c);
	}

	public void dispose() {
		dumpChannelsToDatabase();
		
		if (LOG.isInfoEnabled())
			LOG.info("ChannelService disposed.");
	}
	
	private void loadChannelsFromDatabase() {
		channels.clear();
		DBConnectionService s = DBConnectionService.getInstance();
		DBConnection conn = s.getDBConnection();
		java.sql.ResultSet r = conn.executeQueryWithRS("SELECT chnum,chname,chdescription,level,canJoinLists FROM channels ORDER BY chnum ASC");
		try {
			while(r.next()) {
				int number = r.getInt(1);
				String name = r.getString(2);
				String description = r.getString(3);
				UserLevel level = UserLevel.valueOf(r.getString(4));
				String canJoinLists = r.getString(5);
				ServerList[] canJoin;
				if (canJoinLists != null) {
					String[] split = canJoinLists.split(",");
					canJoin = new ServerList[split.length];
					for(int i=0;i<split.length;i++) {
						canJoin[i] = listManager.getList(split[i]);
					}
				} else {
					canJoin = null;
				}
				Channel c = new Channel(number, name, description, level, canJoin);
				addChannel(c);
			}
		} catch(java.sql.SQLException e) { morphy.Morphy.getInstance().onError("Error loading channels from database",e); }
		
	}
	
	private void dumpChannelsToDatabase() {
		DBConnectionService s = DBConnectionService.getInstance();
		DBConnection conn = s.getDBConnection();
		for(Channel c : channels) {
			String txt = "";
			ServerList[] lists = c.getCanJoinLists();
			if (lists != null) {
				for(int i=0;i<lists.length;i++) {
					ServerList sl = lists[i];
					txt += sl.getName();
					if (i != lists.length-1) txt += ",";
				}
				txt = "'" + txt + "'";
			} else {
				txt = "NULL";
			}
			conn.executeQuery("INSERT INTO `channels` VALUES('" + c.getNumber() + "','" + c.getName() + "','" + c.getDescription() + "','" + c.getLevel().name() + "'," + txt + ") ON DUPLICATE KEY UPDATE `chname` = '" + c.getName() + "',`chdescription` = '" + c.getDescription() + "',`level` = '" +  c.getLevel().name() + "',`canJoinLists` = " + txt + "");
		}
	}

	public Channel getChannel(int number) {
		for (Channel c : getChannels()) {
			if (c.getNumber() == number)
				return c;
		}
		return null;
	}

	public List<Channel> getChannels() {
		return channels;
	}

	public void setChannels(List<Channel> channels) {
		this.channels = channels;
	}

	public int tell(Channel channel, String message, UserSession sender) {
		int sentTo = 0;
		for (UserSession person : channel.getListeners()) {
			if (person.getUser().isOnList(PersonalList.censor,
					sender.getUser().getUserName())
					|| (person.getUser().getUserName().equals(
							sender.getUser().getUserName()) && sender.getUser()
							.getUserVars().getVariables().get("echo").equals(
									"0"))) {
				continue;
			}
			
			if (!sender.getUser().isRegistered()
					&& person.getUser().getUserVars().getVariables().get(
							"ctell").equals("0")) {
				continue;
			}
			
			if (person.getUser().getUserVars().getVariables().get("chanoff")
					.equals("1")) {
				continue;
			}
			
			person.send(UserService.getInstance().getTags(
							sender.getUser().getUserName()) + "("
					+ channel.getNumber() + "): " + message);
			sentTo++;
		}
		return sentTo;
	}
}
