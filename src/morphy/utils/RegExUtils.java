/*
 *   Morphy Open Source Chess Server
 *   Copyright (C) 2008,2009  http://code.google.com/p/morphy-chess-server/
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

import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RegExUtils {
	private static final Log LOG = LogFactory.getLog(RegExUtils.class);

	public static Pattern getPattern(String regularExpression) {
		return Pattern.compile(regularExpression, Pattern.MULTILINE
				| Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	}

	public static boolean matches(Pattern pattern, String stringToTest) {
		try {
			return pattern.matcher(stringToTest).matches();
		} catch (Throwable t) {
			LOG.warn("matches threw exception. regex=" + pattern.pattern()
					+ " test=" + stringToTest, t);
			return false;
		}
	}

	public static boolean matches(String regularExpression, String stringToTest) {
		try {
			return getPattern(regularExpression).matcher(stringToTest)
					.matches();
		} catch (Throwable t) {
			LOG.warn("matches threw exception. regex=" + regularExpression
					+ " test=" + stringToTest, t);
			return false;
		}
	}
}
