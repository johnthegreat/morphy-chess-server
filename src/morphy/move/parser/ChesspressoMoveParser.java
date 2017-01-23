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
		
		char[] chars = notation.toCharArray();
		int last = chars.length - 1;
		int next = 0;
		if (chars[last] == '+' || chars[last] == '#') {
			last--;
		}
		
		short move = Move.ILLEGAL_MOVE;
		
		/*
		 * PLEASE NOTE:
		 * 
		 * This algorithm to convert SAN into moves is very naive, simple, and problematic.
		 * It determines whether the move is a pawn move by looking at case of the first character of the notation.
		 * This fails to allow for sloppy SAN notation such as "bc4" that we need to support.
		 * Also during testing, when entering "e7e5" (using Thief interface), the black king disappeared.
		 * Please refactor this code as soon as possible in order to be more correct.
		 * 
		 */
		
		// CASTLING
		
		if (last > 3 && chars[0] == 'O' && chars[1] == '-' && chars[2] == 'O') {
			if (last >= 5 && chars[3] == '-' && chars[4] == 'O') {
				move = Move.getLongCastle(toPlay);
			} else {
				move = Move.getShortCastle(toPlay);
			}
		} else {
			
			char ch = chars[0];
			if (ch >= 'a' && ch <= 'h') {
				// pawn move
				int col = Chess.NO_COL;
				if (1 > last) { /* illegal */ }
				if (chars[1] == 'x') {
					col = Chess.charToCol(chars[1]);
					next = 2;
				}
				
				if (next + 1 > last) { /* illegal */ }
				int toSqi = Chess.strToSqi(chars[next], chars[next + 1]);
				next += 2;
				
				int promo = Chess.NO_PIECE;
				if (next <= last && chars[next] == '=') {
					if (next < last) {
						promo = Chess.charToPiece(chars[next + 1]);
					} else {
						/* illegal */
					}
				}
				
				move = position.getPawnMove(col, toSqi, promo);
			} else {
				// non-pawn move
				
				int piece = Chess.charToPiece(ch);
				if (last < 2) { /* illegal */ }
				int toSqi = Chess.strToSqi(chars[last - 1], chars[last]);
				last -= 2;
				if (chars[last] == 'x') {
					last--;
				}
				
				int row = Chess.NO_ROW, col = Chess.NO_COL;
				while (last >= 1) {
					char rowColChar = chars[last];
					int r = Chess.charToRow(rowColChar);
					if (r != Chess.NO_ROW) {
						row = r;
						last--;
					} else {
						int c = Chess.charToCol(rowColChar);
						if (c != Chess.NO_COL) {
							col = c;
						} else {
							/* illegal */
						}
						last--;
					}
				}
				move = position.getPieceMove(piece, col, row, toSqi);
			}
			
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