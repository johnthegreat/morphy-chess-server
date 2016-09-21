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
package morphy.channel;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import morphy.service.ServerListManagerService;
import morphy.user.User;
import morphy.user.UserLevel;
import morphy.user.UserSession;
import morphy.utils.john.ServerList;

public class Channel implements Comparable<Channel> {
	protected static Log LOG = LogFactory.getLog(Channel.class);
	
	public static final int MINIMUM = 0x0;  // 0
	public static final int MAXIMUM = 0xFF; // 255

	protected int number;
	protected String name;
	protected String description;
	protected UserLevel level;
	protected ServerList[] canJoin;
	private ServerListManagerService listManager = ServerListManagerService.getInstance();

	private List<UserSession> listeners;

	public Channel(int number, String name, String description,
			UserLevel level, ServerList[] canJoin) {
		if ((number < MINIMUM || number > MAXIMUM) && LOG.isWarnEnabled()) {
			LOG.warn("Channel number provided is out of bounds. MIN=" + MINIMUM + " MAX=" + MAXIMUM);
		}
		
		this.number = number;
		this.name = name;
		this.description = description;
		this.level = level;
		this.canJoin = canJoin;
		setListeners(new ArrayList<UserSession>());
	}

	public void addListener(UserSession ses) {
		getListeners().add(ses);
	}

	public int compareTo(Channel channel) {
		return new Integer(number).compareTo(new Integer(channel.number));
	}

	public String getDescription() {
		return description;
	}

	public List<UserSession> getListeners() {
		return listeners;
	}

	public String getName() {
		return name;
	}

	public int getNumber() {
		return number;
	}
	
	public UserLevel getLevel() {
		return level;
	}
	
	public ServerList[] getCanJoinLists() {
		return canJoin;
	}

	public boolean hasAccess(User user) {
		boolean result = false;
		if (level == null && canJoin == null) {
			result = true;
		} else {
			if (level != null) {
				result = user.getUserLevel().ordinal() >= level.ordinal();
			}
			if (!result && canJoin != null) {
				result = listManager.isOnAnyList(canJoin,user.getUserName());
			}
		}
		return result;
	}

	public void removeListener(UserSession ses) {
		getListeners().remove(ses);
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setListeners(List<UserSession> listeners) {
		this.listeners = listeners;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNumber(int number) {
		this.number = number;
	}
}
