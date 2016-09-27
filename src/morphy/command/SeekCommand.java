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
import morphy.service.GameService;
import morphy.service.SeekService;
import morphy.user.SocketChannelUserSession;
import morphy.user.UserSession;
import morphy.user.UserVars;
import morphy.utils.MorphyStringTokenizer;

import java.net.Socket;

/**
 * Created by John on 09/23/2016.
 */
public class SeekCommand extends AbstractCommand {
	public SeekCommand() {
		super("seek");
	}
	
	public void process(String arguments, UserSession userSession) {
		final SeekService seekService = SeekService.getInstance();
		final String myUsername = userSession.getUser().getUserName();
		
		StringBuilder messageToSendBuilder = new StringBuilder();
		
		Seek seek = new Seek();
		seek.setUserSession(userSession);
		
		// TODO: add updating seek support
		
		// TODO: get rid of the downcast
		SocketChannelUserSession socketChannelUserSession = (SocketChannelUserSession) userSession;
		if (socketChannelUserSession.isPlaying()) {
			userSession.send("You cannot challenge while you are playing a game.");
			return;
		} else if (socketChannelUserSession.isExamining()) {
			userSession.send("You cannot challenge while you are examining a game.");
			return;
		}
		
		// You can only have 3 active seeks.
		if (seekService.getSeeksByUsername(myUsername).length == SeekService.MAX_SEEKS_PER_USER) {
			userSession.send(String.format("You can only have %d active seeks.",SeekService.MAX_SEEKS_PER_USER));
			return;
		}
		
		// Usage: seek [time inc] [rated|unrated] [white|black] [crazyhouse] [suicide]
        //    [wild #] [auto|manual] [formula] [rating-range]
		
		MorphyStringTokenizer tokenizer = new MorphyStringTokenizer(arguments, " ");
		String timeStr = tokenizer.nextToken();
		String incStr = tokenizer.nextToken();
		String ratedStr = tokenizer.nextToken();
		String colorPrefStr = tokenizer.nextToken();
		
		UserVars myUserVars = userSession.getUser().getUserVars();
		
		//
		// use defaults if not passed in
		//
		
		if (timeStr == null) {
			timeStr = myUserVars.getVariables().get("time");
		}
		
		if (incStr == null) {
			incStr = myUserVars.getVariables().get("inc");
		}
		
		if (ratedStr == null) {
			ratedStr = myUserVars.getVariables().get("rated");
		}
		
		if (timeStr.matches("[0-9]+")) {
			int timeMinutes = Integer.parseInt(timeStr);
			seek.getSeekParams().setTime(timeMinutes);
		}
		
		if (incStr.matches("[0-9]+")) {
			int incSeconds = Integer.parseInt(incStr);
			seek.getSeekParams().setIncrement(incSeconds);
		}
		
		if (ratedStr != null) {
			ratedStr = ratedStr.toLowerCase();
			
			boolean isRated = false;
			
			if (ratedStr.equals("u") || ratedStr.equals("unrated")) {
				isRated = false;
			}
			
			if (ratedStr.equalsIgnoreCase("r") || ratedStr.equalsIgnoreCase("rated")) {
				if (!userSession.getUser().isRegistered()) {
					messageToSendBuilder.append("You are unregistered - setting to unrated.");
					isRated = false;
				} else {
					isRated = true;
				}
			}
			
			seek.getSeekParams().setRated(isRated);
		}
		
		seek.getSeekParams().setVariant(Variant.blitz);
		
		Seek registeredSeek = seekService.registerSeek(seek);
		int seekIndex = registeredSeek.getSeekIndex();
		
		int numPlayersWhoSawSeek = 0; // TODO: number of players who saw seek
		messageToSendBuilder.append(String.format("Your seek has been posted with index %d.\n(%d player(s) saw the seek.)", seekIndex, numPlayersWhoSawSeek));
		
		userSession.send(messageToSendBuilder.toString());
	}
}
