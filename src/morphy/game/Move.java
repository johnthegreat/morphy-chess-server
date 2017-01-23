/*
 *   Morphy Open Source Chess Server
 *   Copyright (C) 2008-2010, 2017 http://code.google.com/p/morphy-chess-server/
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

public class Move {
	// TODO: find a new home for this code... GameUtils ?
	// moved over from my (John Nahlen's) javachesslib library
	public static boolean isValidSAN(String notation) {
		// TODO why doesn't this support "kxq"?
		if (notation.equals("00") || notation.equals("OO") || notation.equals("000") || notation.equals("OOO")) return false;
		notation = notation.toUpperCase().replaceAll("[^A-Z0-8@\\=]","");
		//System.out.print(notation + " ");
		return notation.matches("[A-H][1-8]") || notation.matches("[PNBRQK]?[A-H][1-8]X?[A-H][1-8]") ||
			notation.matches("[PNBRQKA-H]X?[A-H][1-8]") || notation.matches("[QRBNP][A-H1-8]X?[A-H][1-8]") || 
			notation.matches("[P|A-H](X[A-H])?[1-8]=[KQRBN]") || notation.matches("[PNBRQK]@[A-H][1-8]") || 
			notation.matches("[A-H][1-8][A-H][1-8]=[KQRBN]") ||
			notation.equals("00") || notation.equals("OO") || notation.equals("000") || notation.equals("OOO");
	}
}
