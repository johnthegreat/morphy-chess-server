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
package morphy.game.style;

import board.PositionState;
import morphy.game.ExaminedGame;
import morphy.game.Game;
import morphy.game.GameInterface;
import morphy.game.Variant;
import morphy.user.UserSession;
import morphy.user.UserVars;

/** Class implementing the style12 string. */
public class Style12 implements StyleInterface {

	private static Style12 singletonInstance = new Style12();
	public static Style12 getSingletonInstance() {
		return singletonInstance;
	}
	
	private Style12() { }
	
	public String print(UserSession userSession, GameInterface g) {
		PositionState p = g.getBoard().getLatestMove();
		if (p == null) {
			/* This should never happen */
		}
		
		String notation = "none", verboseNotation = "none";
		if (p.getPrettyNotation() != null) notation = p.getPrettyNotation();
		if (p.getVerboseNotation() != null) {
			verboseNotation = p.getVerboseNotation();
			verboseNotation = verboseNotation.substring(0,1) + verboseNotation.substring(1).toLowerCase();
		}
		
		int numMoves = g.getBoard().getPositions().size();
		
		//System.err.println(p.getNotation() + " " + p.getVerboseNotation() + " " + p.getFEN());
		int myrelation = 0;
		boolean isBughouse = false;
		boolean isExaminedGame = false;
		boolean isPaused = false;
		if (g instanceof ExaminedGame) {
			isExaminedGame = true;
			ExaminedGame eg = (ExaminedGame)g;
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
			boolean amIWhite = userSession.getUser().getUserName().equals(gg.getWhite().getUser().getUserName());
			boolean amIBlack = userSession.getUser().getUserName().equals(gg.getBlack().getUser().getUserName());
			boolean amIPlaying = amIWhite || amIBlack;
			java.util.Arrays.sort(gg.getObservers());
			int pos = java.util.Arrays.binarySearch(gg.getObservers(),userSession);
			if (pos >= 0) { myrelation = 0; }
			if (pos < 0 && !amIPlaying) {
				myrelation = -3;
			} else {
				boolean whitesMove = !p.isWhitesMove();
				if (numMoves == 1) { whitesMove = true; }
				/*if ((amIWhite && whitesMove) || (!amIWhite && !whitesMove)) myrelation = 1;
				if ((amIWhite && !whitesMove) || (!amIWhite && whitesMove)) myrelation = -1;*/
				if (amIWhite == whitesMove) {
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
		isPaused = ((isBughouse||numMoves>2)&&!isExaminedGame); 
		final String whiteName = (isExaminedGame?((ExaminedGame)g).getWhiteName():((Game)g).getWhite().getUser().getUserName());
		final String blackName = (isExaminedGame?((ExaminedGame)g).getBlackName():((Game)g).getBlack().getUser().getUserName());
		String whoseMove = (p.isWhitesMove()?"B":"W");
		if (numMoves == 1) { whoseMove = "W"; }
		
		String sign = "";
		
		final boolean isCheckmate = false;
		if (isCheckmate) { 
			sign = "#"; 
		} else {
			final boolean isCheck = p.isInCheck(!p.isWhitesMove());
			if (isCheck) { sign = "+"; }
		}
		
		
		String style12string = "<12> " + p.draw() + "" + whoseMove + " " + p.getDoublePawnPushFile() + " " + (p.canWhiteCastleKingside()?"1":"0") + " " + (p.canWhiteCastleQueenside()?"1":"0") + " " + (p.canBlackCastleKingside()?"1":"0") + " " + (p.canBlackCastleQueenside()?"1":"0") + " 0 " + g.getGameNumber() + " " + whiteName + " " + blackName + " " + myrelation + " " + (g.getTime()) + " " + g.getIncrement() + " " + g.getWhiteBoardStrength() + " " + g.getBlackBoardStrength() + " " + g.getWhiteClock() + " " + g.getBlackClock() + " " + moveNumber + " " + verboseNotation + " (0:00" + (uv.getIVariables().get("ms").equals("1")?".000":"") + ") " + notation + (sign) + " " + ((uv.getVariables().get("flip").equals("1")?"1":"0")) + " " + (isPaused?"1":"0") + " " + lag + "" + (uv.getVariables().get("bell").equals("1")?((char)7):"");
		System.err.println(String.format("%-17s",userSession.getUser().getUserName()) + "" + style12string);
		return style12string;
	}
}
