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
import morphy.game.Variant;
import morphy.service.SeekService;
import morphy.user.UserSession;
import morphy.user.UserVars;
import morphy.utils.MorphyStringTokenizer;

/**
 * Created by John on 09/25/2016.
 */
public class UnseekCommand extends AbstractCommand {
	public UnseekCommand() {
		super("unseek");
	}
	
	public void process(String arguments, UserSession userSession) {
		final SeekService seekService = SeekService.getInstance();
		
		// unseek 10
		// You have no active seeks.
		
		// unseek 98
		// You have no seek 98.
		
		StringBuilder messageToSendBuilder = new StringBuilder();
		
		Seek[] mySeeks = seekService.getSeeksByUsername(userSession.getUser().getUserName());
		if (mySeeks.length == 0) {
			userSession.send("You have no active seeks.");
			return;
		}
		
		if (arguments.isEmpty()) {
			// unseek all
			
			seekService.removeSeeksByUsername(userSession.getUser().getUserName());
			userSession.send("Your seeks have been removed.");
		} else if (arguments.matches("[0-9]+")) {
			// unseek by index
			
			int seekIndex = Integer.parseInt(arguments);
			
			if (hasSeek(mySeeks, seekIndex)) {
				// unseek
				
				seekService.unseek(seekIndex);
				userSession.send(String.format("Your seek %d has been removed.", seekIndex));
			} else {
				userSession.send(String.format("You have no seek %d.", seekIndex));
			}
			
		}
	}
	
	/**
	 *
	 * @param seeks
	 * @param seekIndex
	 * @return
	 */
	private boolean hasSeek(Seek[] seeks, int seekIndex) {
		boolean retVal = false;
		for(int i=0;i<seeks.length;i++) {
			Seek _seek = seeks[i];
			if (_seek.getSeekIndex() == seekIndex) {
				// we have a seek by this index
				retVal = true;
				break;
			}
		}
		return retVal;
	}
}
