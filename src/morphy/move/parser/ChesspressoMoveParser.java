/*
 *   Morphy Open Source Chess Server
 *   Copyright (C) 2017  http://code.google.com/p/morphy-chess-server/
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
/*
 * Portions of this file were adapted from the Chesspresso open source library. (LGPL v.2)
 * Copyright (C) Bernhard Seybold
 * http://chesspresso.sourceforge.net/
 */
package morphy.move.parser;

import morphy.utils.ChesspressoUtils;
import chesspresso.Chess;
import chesspresso.move.IllegalMoveException;
import chesspresso.move.Move;
import chesspresso.position.Position;

public class ChesspressoMoveParser implements NotationParser {
	public MoveParseResult parseMove(Position position, String notation, boolean whiteToMove) throws IllegalMoveException {
		int toPlay = whiteToMove ? Chess.WHITE : Chess.BLACK;
		
		//
		// NOTATION FIXES
		//
		
		if (notation.equals("0-0")) {
			notation = "O-O";
		} else if (notation.equals("0-0-0")) {
			notation = "O-O-O";
		}
		
		int promoPiece = Chess.NO_PIECE;
		int idx = notation.indexOf("=");
		if (idx > -1) {
			char ch = notation.charAt(idx+1);
			promoPiece = Chess.charToPiece(ch);
			notation = notation.substring(0,idx);
		}
		
		short move = Move.ILLEGAL_MOVE;
		
		if (notation.equalsIgnoreCase("O-O")) {
			move = Move.getShortCastle(toPlay);
		} else if (notation.equalsIgnoreCase("O-O-O")) {
			move = Move.getLongCastle(toPlay);
		} else if (notation.length() == 4) {
			// for now, assuming from square, to square, e.g. e2e4, d7d5
			
			String fromSq = notation.substring(0,2);
			String toSq = notation.substring(2,4);
			
			int fromSqInt = Chess.strToSqi(fromSq);
			int toSqInt = Chess.strToSqi(toSq);
			
			move = position.getMove(fromSqInt,toSqInt,promoPiece);
		}
		
		
		MoveParseResult moveParseResult = new MoveParseResult();
		
		moveParseResult.input = notation;
		moveParseResult.fromSquare = Chess.sqiToStr(Move.getFromSqi(move));
		moveParseResult.toSquare = Chess.sqiToStr(Move.getToSqi(move));
		// TODO: prettyNotation here
		moveParseResult.prettyNotation = "Nf3";
		moveParseResult.verboseNotation = ChesspressoUtils.getVerboseNotation(position, move);
		moveParseResult.promotionPiece = Character.toString(Chess.pieceToChar(Move.getPromotionPiece(move)));
		moveParseResult.setInternalMove(move);
		return moveParseResult;
	}
	
	// HELPER METHODS
	
	
}