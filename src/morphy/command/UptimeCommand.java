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
package morphy.command;

import java.text.SimpleDateFormat;

import morphy.Morphy;
import morphy.properties.PreferenceKeys;
import morphy.service.GameService;
import morphy.service.PreferenceService;
import morphy.service.UserService;
import morphy.user.UserSession;

public class UptimeCommand extends AbstractCommand {
	public UptimeCommand() {
		super("uptime");
	}

	public void process(String arguments, UserSession userSession) {
		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd, HH:mm z yyyy");
		
		StringBuilder b = new StringBuilder();
		b.append("Morphy ICS 0.00.01: compiled on Aug  7 2011 at 09:45:13\n");
		b.append("The server has been up since " + sdf.format(Morphy.getInstance().getUpSinceTime()) + ".\n");
		
		long diff = System.currentTimeMillis()-Morphy.getInstance().getUpSinceTime();
		System.err.println(diff);
		int days = 0,hours = 0,minutes = 0;
		days = (int)(diff / (1000*60*60*24));
		hours = (int)(diff / (1000*60*60)) % 24;
		minutes = (int)(diff / (1000*60)) % 60;
		b.append("(Up for " + days + " days, " + hours + " hrs, " + minutes + " mins)\n\n");

		UserService us = UserService.getInstance();
		GameService gs = GameService.getInstance();
		b.append("There are currently " + us.getLoggedInUserCount() + " players, with a high of XXX since last restart.\n");
		b.append("There are currently " + gs.getCurrentNumberOfGames() + " games, with a high of " + gs.getHighestNumberOfGames() + " since last restart.\n");

		b.append("Player limit: " + PreferenceService.getInstance().getInt(PreferenceKeys.ThreadServiceMaxThreads) + " users (+ 10 admins)\n");
		b.append("Unregistered user restriction at XXX users.\n");
		
		userSession.send(b.toString());
		return;
	}
}
