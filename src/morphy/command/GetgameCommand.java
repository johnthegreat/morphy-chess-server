/*
 *   Morphy Open Source Chess Server
 *   Copyright (C) 2016 http://code.google.com/p/morphy-chess-server/
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

import morphy.game.Seek;
import morphy.service.SeekService;
import morphy.service.UserService;
import morphy.user.UserSession;

/**
 * Created by John on 09/25/2016.
 */
public class GetgameCommand extends AbstractCommand {
	public GetgameCommand() {
		super("getgame");
	}
	
	@Override
	public void process(String arguments, UserSession userSession) {
		// Usage: getgame [f | ?]
		
		// TODO: not quite sure what all this command does, look into it later.
		
		SeekService seekService = SeekService.getInstance();
		
		if (arguments.isEmpty()) {
			
		} else if (arguments.equals("f")) {
			
		} else if (arguments.equals("?")) {
			
		}
	}
	
//	protected void onMatchFound(UserSession userSession) {
//		// "Your getgame qualifies for %s's seek."
//	}
}
