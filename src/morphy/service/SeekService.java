/*
 *   Morphy Open Source Chess Server
 *   Copyright (C) 2016 http://code.google.com/p/morphy-chess-server/
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

import morphy.game.Seek;
import morphy.user.SocketChannelUserSession;
import morphy.user.UserSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.nio.channels.SocketChannel;
import java.util.*;

/**
 * Created by John on 09/23/2016.
 */
public class SeekService implements Service {
	public static final int MAX_SEEKS_PER_USER = 3;
	
	protected static Log LOG = LogFactory.getLog(SeekService.class);
	
	private static final SeekService singletonInstance = new SeekService();
	
	public static SeekService getInstance() {
		return singletonInstance;
	}
	
	private Integer highestSeekIndex = 0;
	private Stack<Integer> seekIndicesStack = new Stack<Integer>();
	private Map<Integer, Seek> seekMap;
	
	public SeekService() {
		this.seekMap = new HashMap<Integer, Seek>();
	}
	
	private int getNextAvailableSeekIndex() {
		if (seekIndicesStack.size() == 0) {
			seekIndicesStack.add(++highestSeekIndex);
		}
		return seekIndicesStack.pop();
	}
	
	public Seek registerSeek(Seek seek) {
		int seekIndex = this.getNextAvailableSeekIndex();
		seek.setSeekIndex(seekIndex);
		
		this.seekMap.put(seekIndex, seek);
		this.notifyAllSeekers(seek);
		return seek;
	}
	
	public Seek[] getSeeksByUsername(String username) {
		List<Seek> seeksByUsername = new ArrayList<Seek>(MAX_SEEKS_PER_USER);
		Collection<Seek> allSeeks = this.seekMap.values();
		for (Seek seek : allSeeks) {
			if (username.equalsIgnoreCase(seek.getUserSession().getUser().getUserName())) {
				seeksByUsername.add(seek);
			}
		}
		return seeksByUsername.toArray(new Seek[seeksByUsername.size()]);
	}
	
	public void removeSeeksByUsername(String username) {
		Seek[] userSeeks = this.getSeeksByUsername(username);
		for(Seek seek : userSeeks) {
			this.unseek(seek.getSeekIndex());
		}
	}
	
	public boolean unseek(int seekIndex) {
		if (this.seekMap.containsKey(seekIndex)) {
			this.seekMap.remove(seekIndex);
			
			this.seekIndicesStack.add(seekIndex);
			return true;
		} else {
			return false;
		}
	}
	
	public Seek getSeekByIndex(int seekIndex) {
		if (this.seekMap.containsKey(seekIndex)) {
			return this.seekMap.get(seekIndex);
		} else {
			return null;
		}
	}
	
	private void notifyAllSeekers(Seek seek) {
		UserService s = UserService.getInstance();
		UserSession[] arr = s.fetchAllUsersWithVariable("seek","1");
		String line = generateSeekLine(seek);
		for(UserSession sess : arr) {
			// TODO: get rid of this downcast
			SocketChannelUserSession userSession = (SocketChannelUserSession)sess;
			
			if (seek.getUserSession() == userSession && userSession.getUser().getUserVars().getVariables().get("showownseek").equals("0")) {
				continue;
			}
			
			if (userSession.isExamining() || userSession.isPlaying()) {
				// it looks like FICS does not send seek=1 message to people who are currently playing or examining.
				continue;
			}
			userSession.send(line);
		}
	}
	
	private String generateSeekLine(Seek seek) {
		//GuestWNKT (++++) seeking 10 0 unrated blitz ("play 100" to respond)
		//GuestWNKT (++++) seeking 0 0 unrated untimed ("play 100" to respond)
		
		String variantName = seek.getSeekParams().getVariant().name();
		if (seek.getSeekParams().getTime() == 0 && seek.getSeekParams().getIncrement() == 0) {
			variantName = "untimed";
		}
		
		String seekFlags = "";
		if (seek.isUseManual()) {
			seekFlags += " m";
		}
		if (seek.isUseFormula()) {
			seekFlags += " f";
		}
		
		String seekLine = String.format("%s (++++) seeking %d %d %s %s%s (\"play %d\" to respond)",
				seek.getUserSession().getUser().getUserName(),
				seek.getSeekParams().getTime(),
				seek.getSeekParams().getIncrement(),
				seek.getSeekParams().isRated() ? "rated" : "unrated",
				variantName,
				seekFlags,
				seek.getSeekIndex());
		
		return seekLine;
	}
	
	public Seek[] getAllSeeks() {
		Collection<Seek> seekCollection = this.seekMap.values();
		return seekCollection.toArray(new Seek[seekCollection.size()]);
	}
	
	public void dispose() {
		this.seekMap.clear();
		this.seekIndicesStack.clear();
		this.highestSeekIndex = null;
		
		if (LOG.isInfoEnabled()) {
			LOG.info("SeekService disposed.");
		}
	}
}
