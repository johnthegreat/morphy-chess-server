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
package morphy.game;

import java.util.ArrayList;
import java.util.List;

import board.Board;
import board.Piece;

import morphy.user.SocketChannelUserSession;
import morphy.user.UserSession;

public class ExaminedGame implements GameInterface {
	private int gameNumber;
	private String whiteName;
	private String blackName;
	private int whiteRating;
	private int blackRating;
	private int time;
	private int increment;
	private boolean rated;
	private Variant variant;
	private Board board;
	private String reason;
	private String result;
	private boolean isWhitesMove = true;
	private long timeGameStarted;
	
	private int whiteClock;
	private int blackClock;

	List<UserSession> examiningUsers;
	List<UserSession> observers;
	
	private UserSession userLastMoveMadeBy;
	
	public ExaminedGame() {
		observers = new ArrayList<UserSession>(0);
		examiningUsers = new ArrayList<UserSession>(1);
		
		setBoard(new Board());
	}
	
	public ExaminedGame(String white,String black,int time,int increment) {
		this();
		setWhiteName(white);
		setBlackName(black);
		setTime(time);
		setIncrement(increment);
		setWhiteClock(time * (60*1000));
		setBlackClock(time * (60*1000));
	}
	
	public void addExaminingUser(UserSession userSession) {
		examiningUsers.add(userSession);
	}
	
	public void removeExaminingUser(UserSession userSession) {
		examiningUsers.remove(userSession);
	}
	
	/** boolean all - true for all, false for observers only. */
	public void processMoveUpdate(boolean all) {
		UserSession[] examiners = getExaminers();
		for(int i=0;i<examiners.length;i++) {
			examiners[i].send(processMoveUpdate(examiners[i]));
		}
		
		if (!all) {	
			UserSession[] observers = getObservers();
			for(int i=0;i<observers.length;i++) {
				observers[i].send(processMoveUpdate(observers[i]));
			}
		}
	}
	
	public UserSession[] getExaminers() {
		return examiningUsers.toArray(new UserSession[examiningUsers.size()]);
	}
	
	public String processMoveUpdate(UserSession s) {
		String str = s.getUser().getUserVars().getStyle().print(s, this);
		if (getUserLastMoveMadeBy() != null) {
			str += "\n\nGame " + getGameNumber() + ": " + getUserLastMoveMadeBy().getUser().getUserName() + " moves: " + getBoard().getLatestMove().getPrettyNotation();
		}
		return str;
	}
	
	public void addObserver(UserSession observer) {
		((SocketChannelUserSession)observer).getGamesObserving().add(gameNumber);
		observers.add(observer);
	}
	
	public UserSession[] getObservers() {
		return observers.toArray(new UserSession[observers.size()]);
	}
	
	public List<UserSession> getObserversAsList() {
		return observers;
	}
	
	public int getWhiteBoardStrength() {
		int strength = 0;
		strength += board.getLatestMove().getAllSquaresWithPiece(Piece.WHITE_PAWN).length*1;
		strength += board.getLatestMove().getAllSquaresWithPiece(Piece.WHITE_KNIGHT).length*3;
		strength += board.getLatestMove().getAllSquaresWithPiece(Piece.WHITE_BISHOP).length*3;
		strength += board.getLatestMove().getAllSquaresWithPiece(Piece.WHITE_ROOK).length*5;
		strength += board.getLatestMove().getAllSquaresWithPiece(Piece.WHITE_KING).length*0;
		strength += board.getLatestMove().getAllSquaresWithPiece(Piece.WHITE_QUEEN).length*9;
		return strength;
	}
	
	public int getBlackBoardStrength() {
		int strength = 0;
		strength += board.getLatestMove().getAllSquaresWithPiece(Piece.BLACK_PAWN).length*1;
		strength += board.getLatestMove().getAllSquaresWithPiece(Piece.BLACK_KNIGHT).length*3;
		strength += board.getLatestMove().getAllSquaresWithPiece(Piece.BLACK_BISHOP).length*3;
		strength += board.getLatestMove().getAllSquaresWithPiece(Piece.BLACK_ROOK).length*5;
		strength += board.getLatestMove().getAllSquaresWithPiece(Piece.BLACK_KING).length*0;
		strength += board.getLatestMove().getAllSquaresWithPiece(Piece.BLACK_QUEEN).length*9;
		return strength;
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

	public void setWhitesMove(boolean isWhitesMove) {
		this.isWhitesMove = isWhitesMove;
	}

	public boolean isWhitesMove() {
		return isWhitesMove;
	}

	public void setTimeGameStarted(long timeGameStarted) {
		this.timeGameStarted = timeGameStarted;
	}

	public long getTimeGameStarted() {
		return timeGameStarted;
	}

	public void setWhiteName(String whiteName) {
		this.whiteName = whiteName;
	}

	public String getWhiteName() {
		return whiteName;
	}

	public void setBlackName(String blackName) {
		this.blackName = blackName;
	}

	public String getBlackName() {
		return blackName;
	}

	public void setWhiteRating(int whiteRating) {
		this.whiteRating = whiteRating;
	}

	public int getWhiteRating() {
		return whiteRating;
	}

	public void setBlackRating(int blackRating) {
		this.blackRating = blackRating;
	}

	public int getBlackRating() {
		return blackRating;
	}

	public void setUserLastMoveMadeBy(UserSession userLastMoveMadeBy) {
		this.userLastMoveMadeBy = userLastMoveMadeBy;
	}

	public UserSession getUserLastMoveMadeBy() {
		return userLastMoveMadeBy;
	}

	/** Used to put examined games before games 
	 * being played in "games" output */
	public int compareTo(GameInterface o) {
		if (o instanceof Game) return -1; 
		else return new Integer(gameNumber).compareTo(o.getGameNumber());		
	}
}
