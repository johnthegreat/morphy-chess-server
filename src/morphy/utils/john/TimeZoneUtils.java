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
package morphy.utils.john;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class TimeZoneUtils {
	private static Map<String, TimeZone> map;

	static {
		TimeZoneUtils.map = load();
	}

	public static String getAbbreviation(TimeZone tz) {
		return getAbbreviation(tz, new Date());
	}

	public static String getAbbreviation(TimeZone tz, Date d) {
		return tz.getDisplayName(tz.inDaylightTime(d), TimeZone.SHORT)
				.toUpperCase();
	}

	public static TimeZone getTimeZone(String abbrev) {
		Map<String, TimeZone> v = TimeZoneUtils.map;
		TimeZone tz = v.get(abbrev);
		return tz;
	}

	private static Map<String, TimeZone> load() {
		final Map<String, TimeZone> map = new HashMap<String, TimeZone>();
		final String[] arr = TimeZone.getAvailableIDs();
		final Date d = new Date();

		for (final String tmp : arr) {
			final TimeZone tz = TimeZone.getTimeZone(tmp);
			final String abbrev = tz.getDisplayName(tz.inDaylightTime(d),
					TimeZone.SHORT);

			if (map.containsKey(abbrev)) {
				continue;
			}

			map.put(abbrev, tz);
		}

		return map;
	}
}
