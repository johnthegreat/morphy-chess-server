/*
 *   Morphy Open Source Chess Server
 *   Copyright (C) 2008-2011, 2017  http://code.google.com/p/morphy-chess-server/
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
package morphy.game;

import java.util.ArrayList;
import java.util.List;

import morphy.user.SocketChannelUserSession;
import morphy.user.UserSession;
import morphy.utils.ChesspressoUtils;

public class Game implements GameInterface {
	private int gameNumber;
	private UserSession white;
	private UserSession black;
	private int time;
	private int increment;
	private boolean rated;
	private Variant variant;
	private Board board;
	private long timeLastMoveMade;
	private long timeGameStarted;
	private long timeGameEnded;
	private String reason;
	private String result;
	private boolean isClockTicking;
	
	private int whiteClock;
	private int blackClock;

	List<UserSession> observers;
	
	public Game() {
		observers = new ArrayList<UserSession>(0);
		
		setBoard(new Board());
	}
	
	public Game(UserSession white,UserSession black,int time,int increment) {
		this();
		setWhite(white);
		setBlack(black);
		setTime(time);
		setIncrement(increment);
		setWhiteClock(time * (60*1000));
		setBlackClock(time * (60*1000));
	}
	
	/** boolean all - true for all, false for observers only. */
	public void processMoveUpdate(boolean all) {
		if (all) {
			getWhite().send(getWhite().getUser().getUserVars().getStyle().print(white, this));
			getBlack().send(getBlack().getUser().getUserVars().getStyle().print(black, this));
		}
		
		UserSession[] observers = getObservers();
		for(int i=0;i<observers.length;i++) {
			observers[i].send(observers[i].getUser().getUserVars().getStyle().print(observers[i], this));
		}
	}
	
	/** Sends a single player the style 12 string (again).<br /> 
	 * Useful for the "refresh" command, etc. */
	public String processMoveUpdate(UserSession s) {
		return s.getUser().getUserVars().getStyle().print(s, this);
	}
	
	public String generateGameInfoLine(boolean provshow) {
		// TODO add provshow=1
		Game g = this;
		String g1 = "\n<g1> " + g.getGameNumber() + " p=0 t=" + g.getVariant().name() + " r=" + (g.isRated()?"1":"0") + " u=" + String.format("%s,%s",g.getWhite().getUser().isRegistered()?"1":"0",g.getBlack().getUser().isRegistered()?"1":"0") + " it="+(g.getTime()*60)+","+ g.getIncrement() + " i="+(g.getTime()*60)+","+g.getIncrement()+" pt=0 rt=0" + (provshow?"P":"") + ",0" + (provshow?"P":"E") + " ts=0,0 m=2 n=0\n";
		return g1;
	}
	
	public void addObserver(UserSession observer) {
		((SocketChannelUserSession)observer).getGamesObserving().add(gameNumber);
		observers.add(observer);
	}
	
	public UserSession[] getObservers() {
		return observers.toArray(new UserSession[observers.size()]);
	}
	
	public int getWhiteBoardStrength() {
		return ChesspressoUtils.getWhiteBoardStrength(getBoard().getGame().getPosition());
	}
	
	public int getBlackBoardStrength() {
		return ChesspressoUtils.getBlackBoardStrength(getBoard().getGame().getPosition());
	}

	public void setWhite(UserSession white) {
		this.white = white;
	}

	public UserSession getWhite() {
		return white;
	}

	public void setBlack(UserSession black) {
		this.black = black;
	}

	public UserSession getBlack() {
		return black;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public int getTime() {
		return time;
	}

	public void setIncrement(int increment) {
		this.increment = increment;
	}

	public int getIncrement() {
		return increment;
	}

	public void setBoard(Board board) {
		this.board = board;
	}

	public Board getBoard() {
		return board;
	}

	public void setGameNumber(int gameNumber) {
		this.gameNumber = gameNumber;
	}

	public int getGameNumber() {
		return gameNumber;
	}

	public void setRated(boolean rated) {
		this.rated = rated;
	}

	public boolean isRated() {
		return rated;
	}

	public void setVariant(Variant variant) {
		this.variant = variant;
	}

	public Variant getVariant() {
		return variant;
	}

	public long touchLastMoveMadeTime() {
		timeLastMoveMade = System.currentTimeMillis();
		return timeLastMoveMade;
	}

	public long getTimeLastMoveMade() {
		return timeLastMoveMade;
	}

	public void setTimeGameStarted(long timeGameStarted) {
		this.timeGameStarted = timeGameStarted;
	}

	public long getTimeGameStarted() {
		return timeGameStarted;
	}

	public void setTimeGameEnded(long timeGameEnded) {
		this.timeGameEnded = timeGameEnded;
	}

	public long getTimeGameEnded() {
		return timeGameEnded;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getResult() {
		return result;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getReason() {
		return reason;
	}

	public void setWhiteClock(int whiteClock) {
		this.whiteClock = whiteClock;
	}

	public int getWhiteClock() {
		return whiteClock;
	}

	public void setBlackClock(int blackClock) {
		this.blackClock = blackClock;
	}

	public int getBlackClock() {
		return blackClock;
	}

	/** Used to put examined games before games 
	 * being played in "games" output */
	public int compareTo(GameInterface o) {
		if (o instanceof ExaminedGame) return -1; 
		else return new Integer(gameNumber).compareTo(o.getGameNumber());
	}

	public void setClockTicking(boolean isClockTicking) {
		this.isClockTicking = isClockTicking;
	}

	public boolean isClockTicking() {
		return isClockTicking;
	}
}
