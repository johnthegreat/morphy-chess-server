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
package morphy.user;

public enum PersonalList {
	censor, gnotify, noplay, notify, channel, nopartner, remote, idlenotify;
	
	public String toString() {
		return name();
	}
	
	public static PersonalList[] sortByName() {
		PersonalList[] pl = PersonalList.values();
		String[] stringArr = new String[pl.length];
		for(int i=0;i<pl.length;i++) {
			stringArr[i] = pl[i].name();
		}
		java.util.Arrays.sort(stringArr);
		for(int i=0;i<stringArr.length;i++) {
			pl[i] = PersonalList.valueOf(stringArr[i]);
		}
		return pl;
	}
}
