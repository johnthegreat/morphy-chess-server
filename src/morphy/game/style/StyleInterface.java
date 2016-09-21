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

import morphy.game.GameInterface;
import morphy.user.UserSession;

/** All style implementations (1-15) should implement this interface. 
 * Each style MUST have a getSingletonInstance() method. Each class should set the default constructor to private.<br />
 * By doing this, we will save heap space by having multiple pointers to a single object rather than multiple objects. */
public interface StyleInterface {
	/** Prints <tt>board</tt> to <tt>userSession</tt> */
	public String print(UserSession userSession,GameInterface g);
	//public Class<? extends StyleInterface> getSingletonInstance();
}