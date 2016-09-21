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
package morphy.command;

import morphy.user.UserSession;

public interface Command extends Comparable<Command> {
	public CommandContext getContext();

	/**
	 * Processes the command.
	 * 
	 * @param arguments
	 *            The arguments, should be an empty string if there are none.
	 * @param userSession
	 *            The SocketChannelUserSession of the user executing the
	 *            command.
	 */
	public void process(String arguments, UserSession userSession);

	/**
	 * Returns true if the command will be processed. This can be used to check
	 * for UserLevel,PlayerType, and other things.
	 * 
	 * @param userSession
	 *            The user session.
	 * @return The result.
	 */
	public boolean willProcess(UserSession userSession);
}
