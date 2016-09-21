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
package morphy.command.admin;

import morphy.command.AbstractCommand;
import morphy.user.UserSession;

public class AdminCommand extends AbstractCommand {
	public AdminCommand() {
		super("admin/admin");
	}

	public void process(String arguments, UserSession userSession) {
		morphy.user.UserVars uv = userSession.getUser().getUserVars();
		String val = uv.getVariables().get("showadmintag");
		if (val.equals("1")) {
			uv.getVariables().put("showadmintag","0");
			userSession.send("Admin mode (*) is now not shown.");
		} else if (val.equals("0")) {
			uv.getVariables().put("showadmintag","1");
			userSession.send("Admin mode (*) is now shown.");
		}
	}
}
