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

import java.util.HashMap;
import java.util.Map;

/** A MatchParam instance should also be flexible enough to support a seek. */
public class NewMatchParams {
	public static enum Key { time,inc,rated,variant,colorRequested }
	public static enum ColorRequested { White,Black,Neither; };
	
	private Map<Key,Object> map = new HashMap<Key,Object>();
	
	public NewMatchParams() {
		
	}
	
	public void setParam(Key k,Object value) {
		map.put(k, value);
	}
	
	public Object getParam(Key k) {
		return map.get(k);
	}
	
	public static int objToInt(Object o) {
		return ((Integer)o).intValue();
	}
	
	public static boolean objToBoolean(Object o) {
		return ((Boolean)o).booleanValue();
	}
	
	public static Variant objToVariant(Object o) {
		return (Variant)o;
	}
	
	public static ColorRequested objToColorRequested(Object o) {
		return (ColorRequested)o;
	}
}
