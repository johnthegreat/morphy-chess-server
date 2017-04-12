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
import morphy.service.GameService;
import morphy.service.SeekService;
import morphy.service.UserService;
import morphy.user.PersonalList;
import morphy.user.SocketChannelUserSession;
import morphy.user.UserSession;
import morphy.user.UserVars;

/**
 * Created by John on 09/25/2016.
 */
public class PlayCommand extends AbstractCommand {
	public PlayCommand() {
		super("play");
	}
	
	public void process(String arguments, UserSession userSession) {
		// Usage: play [#num | handle]
		
		if (arguments.equals("")) {
			userSession.send(getContext().getUsage());
			return;
		}
		
		//
		// POSSIBLE ERROR MESSAGES
		//
		
		// Knightsmasher is seeking several games.
		// That seek is not available.
		// chessty is not logged in.
		// cday isn't seeking any games.
		// You cannot accept your own seeks.
		// GuestRKMK is censoring you.
		// You are on GuestRKMK's noplay list.
		// You cannot accept seeks while you are examining a game.
		
		// TODO: get rid of the downcast
		SocketChannelUserSession socketChannelUserSession = (SocketChannelUserSession) userSession;
		if (socketChannelUserSession.isPlaying()) {
			userSession.send("You cannot accept seeks while you are playing a game.");
			return;
		} else if (socketChannelUserSession.isExamining()) {
			userSession.send("You cannot accept seeks while you are examining a game.");
			return;
		}
		
		SeekService seekService = SeekService.getInstance();
		
		if (arguments.matches("[0-9]+")) {
			int seekNumber = Integer.parseInt(arguments);
			
			Seek seek = seekService.getSeekByIndex(seekNumber);
			if (seek != null) {
				if (!verifyUserCanPlay(userSession, seek.getUserSession())) {
					return;
				}
				
				seekService.tryAcceptSeek(userSession, seek);
			} else {
				userSession.send("That seek is not available.");
			}
		} else {
			// presuming they pass in a username here..
			UserSession otherPlayer = UserService.getInstance().getUserSession(arguments);
			if (otherPlayer != null) {
				if (!verifyUserCanPlay(userSession, otherPlayer)) {
					return;
				}
				
				String otherPlayerUsername = otherPlayer.getUser().getUserName();
				Seek[] seeksByUsername = seekService.getSeeksByUsername(otherPlayerUsername);
				if (seeksByUsername.length > 1) {
					userSession.send(String.format("%s is seeking several games.", otherPlayerUsername));
				} else if (seeksByUsername.length == 1) {
					// TODO accept this seek
					Seek seekToAccept = seeksByUsername[0];
					seekService.tryAcceptSeek(userSession, seekToAccept);
				} else {
					userSession.send(String.format("%s isn't seeking any games.", otherPlayerUsername));
				}
			} else {
				userSession.send(String.format("%s is not logged in.", arguments));
			}
		}
	}
	
	public boolean verifyUserCanPlay(UserSession userSession, UserSession otherUserSession) {
		GameService.UsersCannotPlayReason reason = GameService.getInstance().verifyUserCanPlay(userSession, otherUserSession);
		
		if (reason == GameService.UsersCannotPlayReason.SELF) {
			userSession.send("You cannot accept your own seeks.");
			return false;
		}
		
		if (reason == GameService.UsersCannotPlayReason.YOU_NOPLAY_THEM) {
			userSession.send(String.format("You are censoring %s.", otherUserSession.getUser().getUserName()));
			return false;
		}
		
		if (reason == GameService.UsersCannotPlayReason.YOU_CENSOR_THEM) {
			userSession.send(String.format("You have %s on your censor list.",otherUserSession.getUser().getUserName()));
			return false;
		}
		
		if (reason == GameService.UsersCannotPlayReason.THEM_NOPLAY_YOU) {
			userSession.send(String.format("You are on %s's noplay list.", otherUserSession.getUser().getUserName()));
			return false;
		}
		
		if (reason == GameService.UsersCannotPlayReason.THEM_CENSOR_YOU) {
			userSession.send(String.format("%s is censoring you.", otherUserSession.getUser().getUserName()));
			return false;
		}
		
		// reason must be NONE
		return true;
	}
	
	// this will be needed when rating is implemented
	/*protected boolean doesUserHaveProvShowEnabled(UserSession userSession) {
		return userSession.getUser().getUserVars().getIVariables().get("provshow").equals("1");
	}*/
}
