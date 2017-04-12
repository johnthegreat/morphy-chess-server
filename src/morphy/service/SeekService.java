/*
 *   Morphy Open Source Chess Server
 *   Copyright (C) 2016-2017 http://code.google.com/p/morphy-chess-server/
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
import morphy.user.PersonalList;
import morphy.user.SocketChannelUserSession;
import morphy.user.UserSession;
import morphy.user.UserVars;
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
	
	public int registerSeek(Seek seek) {
		int seekIndex = this.getNextAvailableSeekIndex();
		seek.setSeekIndex(seekIndex);
		
		this.seekMap.put(seekIndex, seek);
		return this.notifyAllSeekers(seek);
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
	
	public Seek[] findMatchingSeeks(Seek seek) {
		final Seek[] allSeeks = this.getAllSeeks();
		List<Seek> matchingSeeks = new ArrayList<Seek>();
		for(int i=0;i<allSeeks.length;i++) {
			Seek curSeek = allSeeks[i];
			
			if (curSeek.getSeekIndex() == seek.getSeekIndex()) {
				continue;
			}
			
			GameService.UsersCannotPlayReason reason = GameService.getInstance().verifyUserCanPlay(seek.getUserSession(), curSeek.getUserSession());
			if (reason != GameService.UsersCannotPlayReason.NONE) {
				continue;
			}
			
			if (this.areSeeksIdentical(curSeek, seek)) {
				matchingSeeks.add(curSeek);
			}
		}
		return matchingSeeks.toArray(new Seek[matchingSeeks.size()]);
	}
	
	public boolean areSeeksIdentical(Seek a, Seek b) {
		return a.getSeekParams().getTime() == b.getSeekParams().getTime()
				&& a.getSeekParams().getIncrement() == b.getSeekParams().getIncrement()
				&& a.getSeekParams().getVariant() == b.getSeekParams().getVariant();
	}
	
	private int notifyAllSeekers(Seek seek) {
		int numSentTo = 0;
		
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
			
			// TODO: add formula, rating checks
			
			userSession.send(line);
			numSentTo++;
		}
		return numSentTo;
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
	
	public void tryAcceptSeek(UserSession userSession, Seek seek) {
		this.tryAcceptSeek(userSession, seek, null, null);
	}
	
	/**
	 *
	 * @param userSession
	 * @param seek
	 * @param messageToSendBuilder
	 * @param messageToSendOtherPlayerBuilder
	 * @return
	 */
	public void tryAcceptSeek(UserSession userSession, Seek seek, StringBuilder messageToSendBuilder, StringBuilder messageToSendOtherPlayerBuilder) {
		if (GameService.getInstance().verifyUserCanPlay(userSession, seek.getUserSession()) != GameService.UsersCannotPlayReason.NONE) {
			return;
		}
		
		SeekService seekService = SeekService.getInstance();
		final UserSession otherPlayerUserSession = seek.getUserSession();
		
		if (messageToSendBuilder == null) {
			messageToSendBuilder = new StringBuilder();
		}
		
		if (messageToSendOtherPlayerBuilder == null) {
			messageToSendOtherPlayerBuilder = new StringBuilder();
		}
		
		if (seek.isUseManual()) {
			//
			// Send message to the initiating user
			//
			
			messageToSendBuilder.append("Issuing match request since the seek was set to manual.\n");
			// TODO: watch out for 0 0 case... use buildVariantString() ?
			messageToSendBuilder.append(String.format("Issuing: %s (%s) %s (%s) %s %s",
					userSession.getUser().getUserName(), "----", otherPlayerUserSession.getUser().getUserName(), "----",
					seek.getSeekParams().isRated() ? "rated" : "unrated", buildVariantString(seek)));
			userSession.send(messageToSendBuilder.toString());
			
			//
			// Send message to the user that posted the seek
			//
			
			UserVars otherPlayerUserVars = otherPlayerUserSession.getUser().getUserVars();
			messageToSendOtherPlayerBuilder.append(String.format("%s accepts your seek.\n\n", userSession.getUser().getUserName()));
			// TODO: watch out for 0 0 case... use buildVariantString() ?
			messageToSendOtherPlayerBuilder.append(String.format("Challenge: %s (%s) %s (%s) %s %s %d %d.",
					userSession.getUser().getUserName(),"----",
					otherPlayerUserSession.getUser().getUserName(), "----",
					seek.getSeekParams().isRated()?"rated":"unrated", seek.getSeekParams().getVariant(), seek.getSeekParams().getTime(), seek.getSeekParams().getIncrement()));
			messageToSendOtherPlayerBuilder.append(String.format("\nYou can \"accept\" or \"decline\", or propose different parameters.%s",
					(otherPlayerUserVars.getVariables().get("bell").equals("1")?((char)7):"")));
			otherPlayerUserSession.send(messageToSendOtherPlayerBuilder.toString());
		} else {
			//
			// remove all seeks for both players, they are starting a game.
			//
			seekService.removeSeeksByUsername(userSession.getUser().getUserName());
			seekService.removeSeeksByUsername(otherPlayerUserSession.getUser().getUserName());
			
			// prepended message is only sent to the other player
			messageToSendOtherPlayerBuilder.append(String.format("%s accepts your seek.\n\n", userSession.getUser().getUserName()));
			
			//
			// start the game.
			//
			GameService gameService = GameService.getInstance();
			gameService.createGame(userSession, otherPlayerUserSession, seek.getSeekParams(), messageToSendBuilder, messageToSendOtherPlayerBuilder);
		}
	}
	
	/*
	 * used in tryAcceptSeek()
	 */
	protected String buildVariantString(Seek seek) {
		// ... unrated untimed
		// ... unrated standard 999 999
		
		int timeMinutes = seek.getSeekParams().getTime();
		int incSeconds = seek.getSeekParams().getIncrement();
		
		if (timeMinutes == 0 && incSeconds == 0) {
			return "untimed";
		} else {
			return String.format("%s %d %d",
					seek.getSeekParams().getVariant().name(), timeMinutes, incSeconds);
		}
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
