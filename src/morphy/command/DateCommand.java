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
package morphy.command;

import morphy.user.UserSession;
import morphy.utils.john.TimeZoneUtils;

public class DateCommand extends AbstractCommand {

	public DateCommand() {
		super("date");
	}
	
	public void process(String arguments, UserSession userSession) {
		final java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEE MMM dd, HH:mm z yyyy");
		final java.util.Date d = new java.util.Date();
		
		StringBuilder b = new StringBuilder();
		
		// need to get user's timezone here from 'tzone' variable.
		sdf.setTimeZone(TimeZoneUtils.getTimeZone(userSession.getUser()
				.getUserVars().getVariables().get("tzone").toUpperCase()));
		if (sdf.getTimeZone() == null)
			sdf.setTimeZone(java.util.TimeZone.getDefault());
		
		b.append("Local time     - " + sdf.format(d) + "\n");
		sdf.setTimeZone(java.util.TimeZone.getDefault());
		b.append("Server time    - " + sdf.format(d) + "\n");
		sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
		b.append("GMT            - " + sdf.format(d));
		
		userSession.send(b.toString());
	}
}
