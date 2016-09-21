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
package morphy.user;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TreeMap;

import morphy.Morphy;
import morphy.channel.Channel;
import morphy.game.Game;
import morphy.game.GameInterface;
import morphy.game.request.MatchRequest;
import morphy.game.request.PartnershipRequest;
import morphy.game.request.Request;
import morphy.service.DBConnectionService;
import morphy.service.GameService;
import morphy.service.RequestService;
import morphy.service.ScreenService;
import morphy.service.SocketConnectionService;
import morphy.service.UserService;
import morphy.service.ScreenService.Screen;
import morphy.utils.BufferUtils;
import morphy.utils.john.TimeZoneUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SocketChannelUserSession implements UserSession,
		Comparable<UserSession> {
	protected static Log LOG = LogFactory
			.getLog(SocketChannelUserSession.class);

	protected User user;
	protected SocketChannel channel;
	protected StringBuilder inputBuffer;
	protected long lastReceivedTime;
	protected boolean hasLoggedIn = false;
	protected long loginTime;
	protected Map<UserSessionKey, Object> objectMap;
	protected Timer idleLogoutTimer;
	protected Channel lastChannelToldTo = null;
	protected UserSession lastPersonToldTo = null;
	protected boolean isPlaying = false;
	protected boolean isExamining = false;
	protected List<Integer> gamesObserving;
	
	protected List<SocketChannelUserSession> multipleLogins;
	protected SocketChannelUserSession multipleLoginsParent;
	
	public SocketChannelUserSession(User user, SocketChannel channel) {
		this.user = user;
		this.channel = channel;
		
		inputBuffer = new StringBuilder(400);
		loginTime = System.currentTimeMillis();
		objectMap = new TreeMap<UserSessionKey, Object>();
		if (!UserService.getInstance().isAdmin(user.getUserName())) idleLogoutTimer = new Timer();
		gamesObserving = new ArrayList<Integer>();

		if (LOG.isInfoEnabled()) {
			LOG.info("Created SocketChannelUserSession user "
					+ user.getUserName() + " "
					+ channel.socket().getInetAddress());
		}
		multipleLogins = new ArrayList<SocketChannelUserSession>(0);
	}
	
	public void addParentOnMultipleLogins(SocketChannelUserSession e) {
		multipleLoginsParent = e;
	}
	
	public void addUserOnMultipleLogins(SocketChannelUserSession e) {
		multipleLogins.add(e);
	}
	
	public void scheduleIdleTimeout() {
		if (idleLogoutTimer != null)
			idleLogoutTimer.cancel();
		
		// admins don't idle out
		if (UserService.getInstance().isAdmin(getUser().getUserName())) {
			return;
		}
		
		/*LOG.info("scheduling " + getUser().getUserName() + " for idle logout (60 minutes)");*/
		
		final int millis = 60*60*1000;
		
		if (idleLogoutTimer != null)
			idleLogoutTimer.cancel();
		
		final UserSession sess = this;
		idleLogoutTimer = new Timer();
		idleLogoutTimer.schedule(new java.util.TimerTask() {
			public void run() {
					if (getIdleTimeMillis() >= millis-1) {
						send("\n\n**** Auto-logout because you were idle for 60 minutes ****\n");
						if (isExamining()) { 
							GameService gs = GameService.getInstance();
							GameInterface gi = gs.map.get(sess);
							if (gi != null) {
								gs.unexamineGame(sess);
							}
						}
						disconnect();
					} else {
						idleLogoutTimer.purge();
						scheduleIdleTimeout(); // recursion
					}
			} }, 60*60*1000);
	}

	public void disconnect() {
		if (isConnected()) {
			try {
				String v = getNotifyNames();
				if (!v.equals("")) send("Your departure was noted by the following: " + v);
				send(ScreenService.getInstance().getScreen(Screen.Logout));
				if (getUser().isRegistered()) { getUser().getUserVars().dumpToDB(); }
				channel.close();
			} catch (Throwable t) {
				if (LOG.isErrorEnabled())
					LOG.error("Error disconnecting socket channel", t);
			}
			
			if (user.getUserName() != null) {
				UserService.getInstance().removeLoggedInUser(this);
				SocketConnectionService.getInstance().removeUserSession(this);
				GameService.getInstance().map.remove(this);

				if (LOG.isInfoEnabled()) {
					LOG.info("Disconnected user " + user.getUserName());
				}
				
				if (idleLogoutTimer != null) {
					idleLogoutTimer.cancel();
				}
					
				RequestService rs = RequestService.getInstance();
				List<Request> list = rs.getRequestsTo(this);
				if (list != null) {
					for(Request r : list) {
						String toUsername = r.getTo().getUser().getUserName();
						if (r.getClass() == MatchRequest.class) {
							r.getFrom().send(toUsername + " whom you were challenging, has departed.\nChallenge to " + toUsername + " withdrawn.");
						}
						if (r.getClass() == PartnershipRequest.class) {
							r.getFrom().send(toUsername + ", whom you were offering a partnership with, has departed.\n" +
									"Partnership offer to " + toUsername + " withdrawn.");
						}
					}
				}
				rs.removeAllRequestsTo(this);
				
				GameService gs = GameService.getInstance();
				morphy.game.GameInterface g = gs.map.get(this);
				if (g != null) {
					if (g instanceof Game) {
						Game gg = (Game)g;
						gg.setReason(user.getUserName() + " forfeits by disconnection");
						gg.setResult(this==gg.getWhite()?"0-1":"1-0");
						gs.endGame(gg);
						final String line = "\n{Game " + g.getGameNumber() + " (" + gg.getWhite().getUser().getUserName() + " vs. " + gg.getBlack().getUser().getUserName() + ") " + gg.getReason() + "} " + gg.getResult() + "";
						if (this != gg.getWhite()) gg.getWhite().send(line);
						if (this != gg.getBlack()) gg.getBlack().send(line);
					}
					
				}
				
				sendDisconnectPinNotifications();
				sendDisconnectNotifications();
			}
		}
	}
	
	private void sendDisconnectPinNotifications() {
		UserSession[] sessions = UserService.getInstance().fetchAllUsersWithVariable("pin","1");
		for(UserSession s : sessions) {
			s.send(String.format("[%s has disconnected.]",getUser().getUserName()));
		}
	}
	
	private void sendDisconnectNotifications() {
		// Notifications are only sent if this user is registered.
		if (getUser().isRegistered()) {
			String query = "SELECT u.username FROM personallist pl INNER JOIN personallist_entry ple ON (pl.id = ple.personallist_id) INNER JOIN users u ON (u.id = pl.user_id) WHERE pl.`name` = 'notify' && ple.`value` LIKE '" + getUser().getUserName() + "';";
			DBConnectionService dbcs = DBConnectionService.getInstance();
			ResultSet resultSet = dbcs.getDBConnection().executeQueryWithRS(query);
			try {
				UserService us = UserService.getInstance();
				while(resultSet.next()) {
					String username = resultSet.getString(1);
					UserSession sess = us.getUserSession(username);
					
					if (sess != null && sess.isConnected()) {
						boolean highlight = sess.getUser().getUserVars().getVariables().get("highlight").equals("1");
						sess.send("Notification: " + (highlight?((char)27)+"[7m":"") + getUser().getUserName() + (highlight?((char)27)+"[0m":"") + " has departed.");
					}
				} 
			} catch(SQLException e) { Morphy.getInstance().onError(e); }
		}
	}

	public Object get(UserSessionKey key) {
		return objectMap.get(key);
	}

	public Boolean getBoolean(UserSessionKey key) {
		return (Boolean) get(key);
	}

	public SocketChannel getChannel() {
		return channel;
	}

	public long getIdleTimeMillis() {
		return lastReceivedTime == 0 ? 0 : System.currentTimeMillis()
				- lastReceivedTime;
	}

	public StringBuilder getInputBuffer() {
		return inputBuffer;
	}

	public Integer getInt(UserSessionKey key) {
		return (Integer) get(key);
	}

	public long getLoginTime() {
		return loginTime;
	}

	public String getString(UserSessionKey key) {
		return (String) get(key);
	}

	public User getUser() {
		return user;
	}

	public boolean hasLoggedIn() {
		return hasLoggedIn;
	}

	public boolean isConnected() {
		return channel.isOpen();
	}

	public void put(UserSessionKey key, Object object) {
		objectMap.put(key, object);
	}

	public void send(String message) {
		try {
			/* this logic block should be commented out to get rid of multiple-login implementation. */
			if (multipleLoginsParent != null) {
				List<SocketChannelUserSession> list = multipleLoginsParent.multipleLogins;
				multipleLoginsParent.multipleLogins = null;
				multipleLoginsParent.send(message);
				multipleLoginsParent.multipleLogins = list;			
			} else {
				if (multipleLogins != null) {
					for(SocketChannelUserSession sess : multipleLogins) {
						if (sess != null) sess.send(message);
					}
				}
			}
			
			if (isConnected()) {
				String prompt = "fics% ";
					HashMap<String,String> map = getUser().getUserVars().getVariables();
				if (map.containsKey("prompt") && map.containsKey("ptime") && map.containsKey("tzone")) {
					prompt = map.get("prompt");
					boolean useptime = map.get("ptime").equals("1");
					if (useptime) {
						Date d = new Date();
						java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
						String tzone = getUser().getUserVars().getVariables().get("tzone").toUpperCase();
						TimeZone tz = TimeZone.getDefault();
						if (tzone.equals("SERVER")) tzone = tz.getDisplayName(tz.inDaylightTime(d),TimeZone.SHORT);
						sdf.setTimeZone(TimeZoneUtils.getTimeZone(tzone));
						prompt = sdf.format(d) + "_" + prompt;
					}
				}
				
				ByteBuffer buffer = BufferUtils
						.createBuffer(SocketConnectionService.getInstance()
								.formatMessage(this, message + "\n\r" + prompt + " "));
				System.out.println((message + "\n\r" + prompt + " ").replace("\n","\\n").replace("\r","\\r"));
				try {
					channel.write(buffer);
				} catch(java.io.IOException e) { 
					Morphy.getInstance().onError("IOException while trying to write to channel.",e);
				}
			} else {
				if (LOG.isInfoEnabled()) {
					LOG.info("Tried to send message to a logged off user "
							+ user.getUserName() + " " + message);
				}
				disconnect();
			}
		} catch (Throwable t) {
			if (LOG.isErrorEnabled())
				LOG.error("Error sending message to user " + user.getUserName()
						+ " " + message, t);
			disconnect();
		}
	}

	public void setChannel(SocketChannel channel) {
		this.channel = channel;
	}

	public void setHasLoggedIn(boolean hasLoggedIn) {
		this.hasLoggedIn = hasLoggedIn;
	}

	public void setInputBuffer(StringBuilder inputBuffer) {
		this.inputBuffer = inputBuffer;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void touchLastReceivedTime() {
		lastReceivedTime = System.currentTimeMillis();
		scheduleIdleTimeout();
	}

	public int compareTo(UserSession o) {
		return getUser().getUserName().compareToIgnoreCase(
				o.getUser().getUserName());
	}
	
	/** Gets the player names who have this user on their notify list.<br />
	 * Note that this method has poor performance, O(N), where N = number of logged in players.<br />
	 * Returns an empty string if no names. */
	private String getNotifyNames() {
		StringBuilder b = new StringBuilder();
		final UserSession[] arr = UserService.getInstance().getLoggedInUsers();
		java.util.Arrays.sort(arr);
		for(int i=0;i<arr.length;i++) {
			UserSession s = arr[i];
			List<String> l = s.getUser().getLists().get(PersonalList.notify);
			if (l.contains(getUser().getUserName())) {
				b.append(s.getUser().getUserName());
				
				if (i != l.size()-1)
					b.append(" ");
			}
		}
		return b.toString();
	}

	public Channel getLastChannelToldTo() {
		return lastChannelToldTo;
	}

	public void setLastChannelToldTo(Channel lastChannelToldTo) {
		this.lastChannelToldTo = lastChannelToldTo;
	}

	public UserSession getLastPersonToldTo() {
		return lastPersonToldTo;
	}

	public void setLastPersonToldTo(UserSession lastPersonToldTo) {
		this.lastPersonToldTo = lastPersonToldTo;
	}

	public boolean isPlaying() {
		return isPlaying;
	}

	public void setPlaying(boolean isPlaying) {
		this.isPlaying = isPlaying;
	}
	
	public boolean isExamining() {
		return isExamining;
	}

	public void setExamining(boolean isExamining) {
		this.isExamining = isExamining;
	}

	public List<Integer> getGamesObserving() {
		return gamesObserving;
	}

	public void setGamesObserving(List<Integer> gamesObserving) {
		this.gamesObserving = gamesObserving;
	}
}
