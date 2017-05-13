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

import java.util.ArrayList;
import java.util.List;

public class ChesspressoMoveParser implements NotationParser {
	public MoveParseResult parseMove(Position position, String notation, boolean whiteToMove) throws IllegalMoveException {
		short move = (short) this._parseMove(position, notation, whiteToMove);
		
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
	
	private int _parseMove(Position position, String notation, boolean whiteToMove) {
		int toPlay = whiteToMove ? Chess.WHITE : Chess.BLACK;
		
		//
		// NOTATION FIXES
		//
		
		if (notation.equals("0-0")) {
			notation = "O-O";
		} else if (notation.equals("0-0-0")) {
			notation = "O-O-O";
		}
		
		int idx = notation.indexOf("+");
		if (idx > -1) {
			notation = notation.substring(0, idx);
		} else if ((idx = notation.indexOf("#")) > -1) {
			notation = notation.substring(0, idx);
		}
		
		if ((idx = notation.indexOf("x")) > -1) {
			// converts e.g. Ngxe4 to Nge4 which this program knows how to handle.
			notation = notation.substring(0,idx) + notation.substring(idx+1);
		}
		
		
		int promoPiece = Chess.NO_PIECE;
		idx = notation.indexOf("=");
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
		} else if (notation.length() == 2) {
			// this must be a pawn move
			short tmpMove = this.tryParseBasicPawnMove(position, notation, promoPiece);
			if (Move.isValid(tmpMove)) {
				move = tmpMove;
			}
		} else if (notation.length() == 4) {
			short tmpMove = this.tryParseAmbigPieceMove(position, notation, promoPiece);
			if (Move.isValid(tmpMove)) {
				move = tmpMove;
			} else {
				tmpMove = this.tryParseLongAlgebraic(position, notation, promoPiece);
				if (Move.isValid(tmpMove)) {
					move = tmpMove;
				}
			}
		} else {
			short tmpMove = this.tryParseAmbigPieceMove(position, notation, promoPiece);
			if (Move.isValid(tmpMove)) {
				move = tmpMove;
			}
		}
		
		return move;
	}
	
	private short tryParseAmbigPieceMove(Position position, String notation,int promoPiece) {
		String piece = null;
		char rankOrFile = 0;
		if (notation.length() >= 3) {
			piece = notation.substring(0,notation.length()-2);
			if (piece.length() == 2) {
				rankOrFile = piece.charAt(1);
				if (rankOrFile == 'x') {
					rankOrFile = 0;
				}
				piece = piece.substring(0,1);
			}
			//System.out.println(piece);
		}
		String toSq = notation.substring(notation.length()-2);
		int toSqi = Chess.strToSqi(toSq);
		//System.out.println(toSq);
		
		short[] legalMoves = position.getAllMoves();
		List<Integer> fromSqs = new ArrayList<Integer>();
		for(int i=0;i<legalMoves.length;i++) {
			// loop through all of the possible legal moves and see if any of them match our known toSquare
			int mToSqi = Move.getToSqi(legalMoves[i]);
			if (mToSqi == toSqi) {
				// now we can get from squares.
				int fromSqi = Move.getFromSqi(legalMoves[i]);
				//System.out.println(Chess.sqiToStr(fromSqi));
				
				// At this point we think we are dealing with a piece (R, N, B, Q, K)
				char ch = Chess.pieceToChar(position.getPiece(fromSqi));
				if (piece != null && ch == piece.charAt(0)) {
					// this code block is all to handle ambiguous piece resolution :/
					if (rankOrFile != 0) {
						int col = Chess.charToCol(rankOrFile);
						if (col != Chess.NO_COL && col != Chess.sqiToCol(fromSqi)) {
							continue;
						}
						
						if (col == Chess.NO_COL) {
							int row = Chess.charToRow(rankOrFile);
							if (row != Chess.NO_ROW && row != Chess.sqiToRow(fromSqi)) {
								continue;
							}
						}
					}
					fromSqs.add(Integer.valueOf(fromSqi));
				} else if (ch == 'P' && piece != null && Chess.charToPiece(piece.charAt(0)) == Chess.NO_PIECE) {
					//
					// This logic block is to handle en-passant.
					//
					if (position.getSqiEP() == toSqi && position.getPiece(toSqi) == Chess.NO_PIECE) {
						int epCol = Chess.sqiToCol(toSqi);
						int epRow = Chess.sqiToRow(fromSqi);
						int epSqi = Chess.coorToSqi(epCol, epRow);
						
						int opponentPawn = position.getToPlay() == Chess.WHITE ? Chess.BLACK_PAWN : Chess.WHITE_PAWN;
						
						if (position.getStone(epSqi) == opponentPawn) {
							// this is an enemy pawn.
							short move = Move.getEPMove(fromSqi, toSqi);
							if (Move.isValid(move)) {
								return move;
							}
						}
					}
					
					// handle the case where piece contains a column name instead of a piece
					// so instead of Bxe4, try to cater for dxe4.
					int col = Chess.charToCol(piece.charAt(0));
					Integer fromSq = Integer.valueOf(fromSqi);
					
					// added the fromSqs.contains() because I think it is counting the multiple available promotion options as legal moves, which makes sense.
					if (col != Chess.NO_COL && col == Chess.sqiToCol(fromSqi) && !fromSqs.contains(fromSq)) {
						fromSqs.add(fromSq);
					}
				}
			}
		}
		
		if (fromSqs.size() == 0 || fromSqs.size() > 1) {
			return Move.NO_MOVE;
		}
		
		int fromSqi = fromSqs.get(0);
		// found our from square it appears.
		short move = position.getMove(fromSqi, toSqi, promoPiece);
		return move;
	}
	
	private short tryParseLongAlgebraic(Position position, String notation, int promoPiece) {
		// for now, assuming from square, to square, e.g. e2e4, d7d5
		String fromSq = notation.substring(0,2);
		String toSq = notation.substring(2,4);
		
		int fromSqInt = Chess.strToSqi(fromSq);
		int toSqInt = Chess.strToSqi(toSq);
		
		short move = position.getMove(fromSqInt,toSqInt,promoPiece);
		return move;
	}
	
	private short tryParseBasicPawnMove(Position position, String notation, int promoPiece) {
		String toSq = notation;
		int toSqi = Chess.strToSqi(toSq);
		
		short[] legalMoves = position.getAllMoves();
		for(int i=0;i<legalMoves.length;i++) {
			int mToSqi = Move.getToSqi(legalMoves[i]);
			if (mToSqi == toSqi) {
				// now we can get from squares.
				int fromSqi = Move.getFromSqi(legalMoves[i]);
				short stone = position.getToPlay() == Chess.WHITE ? Chess.WHITE_PAWN : Chess.BLACK_PAWN;
				if (position.getStone(fromSqi) == stone) {
					// looks like we found our pawn move.
					short move = Move.getPawnMove(fromSqi, toSqi, false, promoPiece);
					return move;
				}
			}
		}
		return Move.NO_MOVE;
	}
}