/*
 *   Morphy Open Source Chess Server
 *   Copyright (C) 2008,2009, 2016  http://code.google.com/p/morphy-chess-server/
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
package morphy.utils;

import java.nio.ByteBuffer;

import morphy.Morphy;
import morphy.properties.PreferenceKeys;

public class BufferUtils {
	public static ByteBuffer createBuffer(String message) {
		ByteBuffer buffer = ByteBuffer.allocate(message.length() * 4);
		try {
			buffer
					.put(message
							.getBytes(Morphy.getInstance().getMorphyPreferences()
									.getString(
											PreferenceKeys.SocketConnectionServiceCharEncoding)));
		} catch (Throwable t) {
			Morphy.getInstance().onError("Error encoding message", t);
		}
		buffer.flip();
		return buffer;
	}
}
