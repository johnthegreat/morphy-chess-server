/*
 *   Morphy Open Source Chess Server
 *   Copyright (C) 2008-2011, 2016  http://code.google.com/p/morphy-chess-server/
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
import java.util.Stack;

import morphy.game.ExaminedGame;
import morphy.game.Game;
import morphy.game.GameInterface;
import morphy.game.params.GameParams;
import morphy.game.Variant;
import morphy.user.SocketChannelUserSession;
import morphy.user.UserSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GameService implements Service {
	protected static Log LOG = LogFactory.getLog(GameService.class);
	private static final GameService singletonInstance = new GameService();
	
	protected int mostConcurrentGames = 0;
	protected int stackSize = 0;
	protected Stack<Integer> stack = new Stack<Integer>();
	public HashMap<UserSession,GameInterface> map = new HashMap<UserSession,GameInterface>();
	protected List<GameInterface> games;
	
	public static GameService getInstance() {
		return singletonInstance;
	}
	
	private String generateGin(Game g,boolean gameStart) {
		if (gameStart) {
			return "{Game " + g.getGameNumber() + " (" + g.getWhite().getUser().getUserName() + " vs. " + g.getBlack().getUser().getUserName() + ") Creating " + (g.isRated()?"rated":"unrated") + " " + g.getVariant().name() + " match.}";
		} else {
			//{Game 1 (johnthegreat vs. GuestZJBY) Game aborted by mutual agreement} *
			return "{Game " + g.getGameNumber() + " (" + g.getWhite().getUser().getUserName() + " vs. " + g.getBlack().getUser().getUserName() + ") " + g.getReason() + "} " + g.getResult();
		}
	}
	
	private void sendGin(Game g,boolean gameStart) {
		UserService s = UserService.getInstance();
		UserSession[] arr = s.fetchAllUsersWithVariable("gin","1");
		String line = generateGin(g,gameStart);
		for(UserSession sess : arr) {
			sess.send(line);
		}
	}
	
	public void endGame(Game g) {
		sendGin(g,false);
		
		((SocketChannelUserSession)g.getWhite()).setPlaying(false);
		((SocketChannelUserSession)g.getBlack()).setPlaying(false);
		map.put(g.getWhite(),null);
		map.put(g.getBlack(),null);
		
		// recycle this game number
		stack.push(g.getGameNumber());
	}
	
	public Game createGame(UserSession white,UserSession black,GameParams params, StringBuilder messageToSendWhite, StringBuilder messageToSendBlack) {
		if (messageToSendWhite == null) {
			messageToSendWhite = new StringBuilder();
		}
		
		if (messageToSendBlack == null) {
			messageToSendBlack = new StringBuilder();
		}
		
		Game g = new Game();
		g.setWhite(white);
		g.setBlack(black);
		g.setTime(params.getTime());
		g.setIncrement(params.getIncrement());
		g.setRated(params.isRated());
		g.setVariant(params.getVariant());
		g.setGameNumber(getNextAvailableGameNumber());
		g.setTimeGameStarted(System.currentTimeMillis());
		g.setWhiteClock(g.getTime() * (60*1000));
		g.setBlackClock(g.getTime() * (60*1000));
		g.setClockTicking(false);
		
		map.put(g.getWhite(),g);
		map.put(g.getBlack(),g);
		
		String _cachedGin = generateGin(g,true);
		
		// TODO: watch out for 0 0 case...
		String line = String.format("Creating: %s (%s) %s (%s) %s %s %d %d",
				g.getWhite().getUser().getUserName(), "----", g.getBlack().getUser().getUserName(), "----",
				g.isRated()?"rated":"unrated", g.getVariant().name(), g.getTime(), g.getIncrement());

		//
		// Send message to white
		//
		
		messageToSendWhite.append(String.format("%s\n\r%s\n\r", line, _cachedGin));
		if (g.getWhite().getUser().getUserVars().getIVariables().get("gameinfo").equals("1")) {
			boolean provshow = g.getWhite().getUser().getUserVars().getVariables().get("provshow").equals("1");
			messageToSendWhite.append(g.generateGameInfoLine(provshow));
		}
		messageToSendWhite.append("\n\r"+g.processMoveUpdate(g.getWhite()));
		g.getWhite().send(messageToSendWhite.toString());
		
		//
		// Send message to black
		//
		
		messageToSendBlack.append(String.format("%s\n\r%s\n\r", line, _cachedGin));
		if (g.getBlack().getUser().getUserVars().getIVariables().get("gameinfo").equals("1")) {
			boolean provshow = g.getBlack().getUser().getUserVars().getVariables().get("provshow").equals("1");
			messageToSendBlack.append(g.generateGameInfoLine(provshow));
		}
		messageToSendBlack.append("\n\r"+g.processMoveUpdate(g.getBlack()));
		g.getBlack().send(messageToSendBlack.toString());
		
		g.processMoveUpdate(false);
		
		((SocketChannelUserSession)g.getWhite()).setPlaying(true);
		((SocketChannelUserSession)g.getBlack()).setPlaying(true);
		
		sendGin(g,true);
		games.add(g);
		if (games.size() > mostConcurrentGames) {
			mostConcurrentGames = games.size();
		}
		
		return g;
	}
	
	public void unexamineGame(UserSession userSession) {
		ExaminedGame g = (ExaminedGame)map.get(userSession);
			
		UserSession[] examiners = g.getExaminers();
		for(int i=0;i<examiners.length;i++) {
			if (examiners[i].equals(userSession)) {
				examiners[i].send("You are no longer examining game " + g.getGameNumber() + ".");
			} else {
				examiners[i].send(userSession.getUser().getUserName() + " stopped examining game " + g.getGameNumber() + ".");
			}
		}
		g.removeExaminingUser(userSession);
		
		String line = null;
		if (examiners.length == 1) {
			// This was the only examiner
			games.remove(g);
			map.remove(userSession);
			stack.push(g.getGameNumber());
			
			/*line = userSession.getUser().getUserName() + " stopped examining game " + g.getGameNumber() + ".\n\n" +
			"Removing game " + g.getGameNumber() + " from observation list.";*/
			line = userSession.getUser().getUserName() + " stopped examining game " + g.getGameNumber() + ".\n\n" +
				"Game " + g.getGameNumber() + " (which you were observing) has no examiners.\n" + 
				"Removing game " + g.getGameNumber() + " from observation list.";
		} else {
			// There are multiple examiners - what does an observer see?
			line = userSession.getUser().getUserName() + " stopped examining game " + g.getGameNumber() + ".";
		}
		
		UserSession[] observers = g.getObservers();
		for(int i=0;i<observers.length;i++) {
			if (line != null) observers[i].send(line);
			if (examiners.length == 1) {
				SocketChannelUserSession sess = ((SocketChannelUserSession)observers[i]); 
				sess.getGamesObserving().remove(new Integer(g.getGameNumber()));
			}
		}
		
		((SocketChannelUserSession)userSession).setExamining(false);
	}
	
	public ExaminedGame createExaminedGame(UserSession userSession) {
		ExaminedGame g = new ExaminedGame();
		g.addExaminingUser(userSession);
		g.setVariant(Variant.blitz);
		g.setWhiteName(userSession.getUser().getUserName());
		g.setBlackName(userSession.getUser().getUserName());
		g.setGameNumber(GameService.getInstance().getNextAvailableGameNumber());
		g.setTimeGameStarted(System.currentTimeMillis());
		g.setWhitesMove(true);
		//g.getBoard().
		
		games.add(g);
		map.put(userSession, g);
		
		((SocketChannelUserSession)userSession).setExamining(true);
		userSession.send("Starting a game in examine (scratch) mode.\n\n" + g.processMoveUpdate(userSession));
		
		return g;
	}
	
	public List<GameInterface> getGames() {
		return games;
	}
	
	/** O(N) performance<br/>
	 * Returns null if not found */
	public GameInterface findGameById(int id) {
		for(GameInterface g : games) {
			if (g.getGameNumber() == id)
				return g;
		}
		return null;
	}
	
	public GameService() {
		games = new ArrayList<GameInterface>();
		
		if (LOG.isInfoEnabled())
			LOG.info("Initialized GameService.");
	}
	
	/**
	 * Returns the next available (not taken) game number
	 * to be assigned to a board.
	 */
	public int getNextAvailableGameNumber() {
		if (stack.empty()) {
			stackSize++;
			stack.push(new Integer(stackSize));
		}
		return stack.pop();
	}
	
	/** Returns the number of games currently being played. */
	public int getCurrentNumberOfGames() {
		return games.size();
	}
	
	/*
	public int addGame() {
		return getNextAvailableGameNumber();
	}

	public void removeGame(int gamenumber) {
		stack.push(gamenumber);
	}*/
	
	/** Returns the most number of games played 
	 * at any given time on the server since loaded. */
	public int getHighestNumberOfGames() {
		return mostConcurrentGames;
	}

	public void dispose() {
		map.clear();
		games.clear();
		stack.clear();
		
		if (LOG.isInfoEnabled()) {
			LOG.info("GameService disposed.");
		}
	}
	
	
}
