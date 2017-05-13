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
package morphy.game;

import chesspresso.Chess;
import chesspresso.move.IllegalMoveException;
import morphy.move.parser.ChesspressoMoveParser;
import morphy.move.parser.MoveParseResult;
import morphy.move.parser.NotationParser;
import morphy.service.GameService;
import morphy.user.UserSession;

/**
 * Created by John on 05/13/2017.
 */
public class ProcessGameMoveHelper {
	public static void processGameMove(UserSession userSession, String command) {
		GameInterface g = GameService.getInstance().map.get(userSession);
		if (g != null) {
			if (g instanceof Game) {
				Game gg = (Game)g;
				/*if (!gg.isClockTicking()) {
					userSession.send("The clock is paused, use \"unpause\" to resume.\n\n"+gg.processMoveUpdate(userSession));
					return;
				}*/
				boolean isWhiteMove = gg.getWhite().equals(userSession);
				long last = gg.getTimeLastMoveMade();
				if (last == 0L) {
					last = System.currentTimeMillis();
				}
				
				// now, parse the SAN into a move.
				
				int myColor = isWhiteMove ? Chess.WHITE : Chess.BLACK;
				if (g.getBoard().getGame().getPosition().getToPlay() == myColor) {
					tryProcessMove(userSession, g, command, isWhiteMove);
				} else {
					userSession.send("It is not your move.");
					return;
				}
				
				long newt = gg.touchLastMoveMadeTime();
				if (isWhiteMove) {
					g.setWhiteClock((g.getWhiteClock()-(int)(newt-last)) + (g.getIncrement()*1000));
				} else {
					g.setBlackClock((g.getBlackClock()-(int)(newt-last)) + (g.getIncrement()*1000));
				}
				g.processMoveUpdate(true);
				
				// Check for game end
				if (g.getBoard().getGame().getPosition().isMate()) {
					// game over
					String result = g.getBoard().getGame().getPosition().getLastMove().isWhiteMove() ? "1-0" : "0-1";
					g.setResult(result);
					
					final String whoWasCheckmated = g.getBoard().getGame().getPosition().getLastMove().isWhiteMove() ?
							g.getBlackName() : g.getWhiteName();
					
					String reason = String.format("%s checkmated", whoWasCheckmated);
					g.setReason(reason);
					
					GameService.getInstance().endGame(gg);
					return;
				} else if (gg.getBoard().getGame().getPosition().isStaleMate()) {
					// game over
					g.setResult("1/2-1/2");
					g.setReason("Game drawn by stalemate");
					GameService.getInstance().endGame(gg);
					return;
				}
			}
			
			if (g instanceof ExaminedGame) {
				ExaminedGame gg = (ExaminedGame)g;
				tryProcessMove(userSession, g, command, gg.isWhitesMove());
				g.processMoveUpdate(true);
			}
		} else {
			userSession.send("You are not playing or examining a game.");
		}
	}
	
	private static void tryProcessMove(UserSession userSession, GameInterface g, String command, boolean isWhiteMove) {
		try {
			NotationParser notationParser = new ChesspressoMoveParser();
			MoveParseResult moveParseResult = notationParser.parseMove(g.getBoard().getGame().getPosition(), command, isWhiteMove);
			g.getBoard().getGame().getPosition().doMove(moveParseResult.getInternalMove());
		} catch(IllegalMoveException e) {
			userSession.send(String.format("Illegal move (%s).",command));
			System.err.print(e.getMessage());
		}
	}
}
