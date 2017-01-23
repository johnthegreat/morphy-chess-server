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

import morphy.game.GameInterface;
import morphy.user.UserSession;

public class Style13 implements StyleInterface {

	private static Style13 singletonInstance = new Style13();
	public static Style13 getSingletonInstance() {
		return singletonInstance;
	}
	
	private Style13() { }
	
	// http://pueblo.sourceforge.net/doc/manual/ansi_color_codes.html
	// http://bluesock.org/~willg/dev/ansi.html#ansicodes
	public String print(UserSession userSession, GameInterface g) {
		return "";
		/*StringBuilder b = new StringBuilder();
		
		UserVars uv = userSession.getUser().getUserVars();
		boolean millis = uv.getIVariables().get("ms").equals("1");
		
		b.append(StringUtils.repeat(" ",7) + "+" + StringUtils.repeat("-", 3*8) + "+\n");
		PositionState ps = g.getBoard().getLatestMove();
		Piece[][] board = ps.getBoard();
		for(int i=0;i<board.length;i++) {
			b.append(StringUtils.repeat(" ",4)+(8-i)+StringUtils.repeat(" ",2)+"|");
			for(int j=0;j<board[i].length;j++) {
				String abbrev = board[7-i][j].getAbbreviation();
				
				if (abbrev.equals("-")) abbrev = " ";
				String square = PositionState.convertFromPointToSquare(new java.awt.Point(j,7-i));
				boolean isWhiteSquare = PositionState.isWhiteColoredSquare(square);
				boolean isWhitePiece = ps.getPiece(square).isWhite();
				//System.out.println(square + " " + isWhiteSquare);
				String bgcolor = ((char)27) + (isWhiteSquare?"[45m":"[49m");
				String fgcolor = ((char)27) + (isWhitePiece?"[1;37m":"[1;39m");
				b.append(bgcolor + fgcolor + " " + abbrev.toUpperCase() + " " + ((char)27)+"[0m");
			}
			b.append("|");
			if (i == 0 || (i >= 3 && i <= 6)) { b.append(StringUtils.repeat(" ",5)); }
			if (i == 0) { b.append("Move # : 1 (White)"); }
			else if (i == 3) { b.append("Black Clock : 0:00" + (millis?".000":"")); }
			else if (i == 4) { b.append("White Clock : 0:00" + (millis?".000":"")); }
			else if (i == 5) { b.append("Black Strength : " + g.getBlackBoardStrength()); }
			else if (i == 6) { b.append("White Strength : " + g.getWhiteBoardStrength()); } 
			b.append("\n");
			
		}
		b.append(StringUtils.repeat(" ",7) + "+" + StringUtils.repeat("-", 3*8) + "+\n");
		b.append(StringUtils.repeat(" ",8));
		
		char c = 'a';
		for(int i=0;i<8;i++) { b.append(" "+c+" "); c++; }
		
		
		return b.toString();*/
	}

}
