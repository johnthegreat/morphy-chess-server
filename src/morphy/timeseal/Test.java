/*
 *   Morphy Open Source Chess Server
 *   Copyright (C) 2017 http://code.google.com/p/morphy-chess-server/
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
package morphy.timeseal;

import java.io.IOException;
import java.util.Arrays;

public class Test {
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		TimesealCoder coder = new TimesealCoder();
		byte[] encoded = coder.encode("TIMESEAL2|OpenSeal|OpenSeal|".getBytes(), 85812);
		System.out.println(Arrays.toString(new String(encoded).toCharArray()));
		byte[] encodedWithoutNewline = Arrays.copyOfRange(encoded, 0, encoded.length - 1);
		System.out.println(new String(encodedWithoutNewline));
		TimesealParseResult parseResult = coder.decode(encodedWithoutNewline);
		System.out.println(parseResult.getTimestamp());
		System.out.println(parseResult.getMessage());
	}
}