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

public abstract class AbstractCommand implements Command {
	protected CommandContext context;

	public AbstractCommand(String helpFileName) {
		context = new CommandContext(helpFileName);
	}

	public int compareTo(Command o) {
		return context.getName().compareTo(o.getContext().getName());
	}

	public CommandContext getContext() {
		return context;
	}

	public void setContext(CommandContext context) {
		this.context = context;
	}

	/**
	 * Performs a user level check on the command.
	 */
	public boolean willProcess(UserSession userSession) {
		return userSession.getUser().getUserLevel().ordinal() >= context
				.getUserLevel().ordinal();
	}
}
