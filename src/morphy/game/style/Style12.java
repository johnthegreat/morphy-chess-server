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
package morphy.game.style;

import chesspresso.Chess;
import chesspresso.position.Position;
import morphy.game.ExaminedGame;
import morphy.game.Game;
import morphy.game.GameInterface;
import morphy.game.Variant;
import morphy.user.UserSession;
import morphy.user.UserVars;
import morphy.utils.ChesspressoUtils;

/** Class implementing the style12 string. */
public class Style12 implements StyleInterface {

	private static Style12 singletonInstance = new Style12();
	public static Style12 getSingletonInstance() {
		return singletonInstance;
	}
	
	private Style12() { }
	
	public String print(UserSession userSession, GameInterface g) {
		Position position = g.getBoard().getGame().getPosition();
		
		String notation = "none", verboseNotation = "none";
		if (position.getLastMove() != null) {
			notation = position.getLastMove().getSAN();
			verboseNotation = ChesspressoUtils.getVerboseNotation(position);
		}
		
		int numMoves = g.getBoard().getGame().getPosition().getPlyNumber();
		
//		-3 isolated position, such as for "ref 3" or the "sposition" command
//		-2 I am observing game being examined
//		2 I am the examiner of this game
//		-1 I am playing, it is my opponent's move
//		1 I am playing and it is my move
//		0 I am observing a game being played
//		* initial time (in seconds) of the match
		int myrelation = 0;
		
		boolean isBughouse = false;
		boolean isExaminedGame = false;
		boolean isPaused = false;
		
		String whiteName = null;
		String blackName = null;
		
		if (g instanceof ExaminedGame) {
			isExaminedGame = true;
			ExaminedGame eg = (ExaminedGame)g;
			
			whiteName = eg.getWhiteName();
			blackName = eg.getBlackName();
			
			java.util.Arrays.sort(eg.getExaminers());
			if (java.util.Arrays.binarySearch(eg.getExaminers(),userSession) >= 0) {
				myrelation = 2;
			} else {
				java.util.Arrays.sort(eg.getObservers());
				if (java.util.Arrays.binarySearch(eg.getObservers(),userSession) >= 0) {
					myrelation = -2;
				}
			}
		} else if (g instanceof Game) {
			Game gg = (Game)g;
			
			whiteName = gg.getWhite().getUser().getUserName();
			blackName = gg.getBlack().getUser().getUserName();
			
			boolean amIWhite = userSession.getUser().getUserName().equals(whiteName);
			boolean amIBlack = userSession.getUser().getUserName().equals(blackName);
			boolean amIPlaying = amIWhite || amIBlack;
			java.util.Arrays.sort(gg.getObservers());
			int pos = java.util.Arrays.binarySearch(gg.getObservers(),userSession);
			if (pos >= 0) {
				myrelation = 0;
			} else if (pos < 0 && !amIPlaying) {
				myrelation = -3;
			} else {
				boolean whiteToMove = position.getToPlay() == Chess.WHITE;
				if (amIWhite == whiteToMove) {
					myrelation = 1;
				} else {
					myrelation = -1;
				}
			}
			
			isBughouse = gg.getVariant() == Variant.bughouse || gg.getVariant() == Variant.frbughouse;
			isPaused = !gg.isClockTicking();
		}
		
		UserVars uv = userSession.getUser().getUserVars();
		
		
		int lag = 0; // requires timeseal...
		int moveNumber = numMoves/2;
		if (moveNumber == 0) moveNumber = 1;
		isPaused = ((isBughouse||numMoves>=2)&&!isExaminedGame);
		
		
		String whoseMove = position.getToPlay() == Chess.WHITE ? "W" : "B";
		
		String sign = "";
		
		if (position.isMate()) { 
			sign = "#"; 
		} else if (position.isCheck()) {
			sign = "+";
		}
		
		final int doublePawnPushFile = ChesspressoUtils.getDoublePawnPushFile(position);
		final String canWhiteCastleKingside = ChesspressoUtils.canWhiteCastleKingside(position) ? "1" : "0";
		final String canWhiteCastleQueenside = ChesspressoUtils.canWhiteCastleQueenside(position) ? "1" : "0";
		final String canBlackCastleKingside = ChesspressoUtils.canBlackCastleKingside(position) ? "1" : "0";
		final String canBlackCastleQueenside = ChesspressoUtils.canBlackCastleQueenside(position) ? "1" : "0";
		
		String style12string = String
				.format("<12> %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s%s",
						drawPosition(position),
						whoseMove,
						doublePawnPushFile,
						canWhiteCastleKingside,
						canWhiteCastleQueenside,
						canBlackCastleKingside,
						canBlackCastleQueenside,
						"0",
						g.getGameNumber(),
						whiteName,
						blackName,
						myrelation,
						g.getTime(),
						g.getIncrement(),
						g.getWhiteBoardStrength(),
						g.getBlackBoardStrength(),
						g.getWhiteClock(),
						g.getBlackClock(),
						moveNumber,
						verboseNotation,
						"(0:00" + (uv.getIVariables().get("ms").equals("1") ? ".000" : "") + ")",
						notation,
						//sign,
						"0", //uv.getVariables().get("flip").equals("1") ? "1" : "0",
						isPaused ? "1" : "0",
						lag,
						uv.getVariables().get("bell").equals("1") ? ((char) 7) : "");
		System.err.println(String.format("%-17s",userSession.getUser().getUserName()) + "" + style12string);
		return style12string;
	}
	
	private String drawPosition(Position position) {
		StringBuilder builder = new StringBuilder();
		for(int i=Chess.NUM_OF_ROWS-1;i>=0;i--) {
			for(int j=0;j<Chess.NUM_OF_COLS;j++) {
				int stone = position.getStone(Chess.coorToSqi(j, i));
				if (stone == Chess.NO_STONE) {
					builder.append("-");
				} else {
					String piece = Character.toString(Chess.stoneToChar(stone));
					builder.append(Chess.stoneHasColor(stone, Chess.WHITE) ? piece.toUpperCase() : piece.toLowerCase());
				}
			}
			if (i > 0) {
				builder.append(" ");
			}
		}
		return builder.toString();
	}
}
