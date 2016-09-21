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

public enum Variant {
	blitz,lightning,bughouse,frbughouse,crazyhouse,losers,suicide,frcrazyhouse,atomic,standard;
	
	/** For standard chess only */
	public static Variant getVariantBasedOnTimeAndIncrement(int time,int inc) {
		int etime = (time + inc*2/3);
		if (etime < 3) return lightning;
		if (etime > 3 && etime < 15) return blitz;
		if (etime > 15) return standard;
		
		return null;
	}
}
