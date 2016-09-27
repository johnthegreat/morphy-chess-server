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
	
	@Override
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
				
				// TODO accept this seek
				tryAcceptSeek(userSession, seek);
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
					tryAcceptSeek(userSession, seekToAccept);
				} else {
					userSession.send(String.format("%s isn't seeking any games.", otherPlayerUsername));
				}
			} else {
				userSession.send(String.format("%s is not logged in.", arguments));
			}
		}
	}
	
	/**
	 *
	 * @param userSession
	 * @param seek
	 * @return
	 */
	protected void tryAcceptSeek(UserSession userSession, Seek seek) {
		SeekService seekService = SeekService.getInstance();
		final UserSession otherPlayerUserSession = seek.getUserSession();
		
		StringBuilder messageToSendBuilder = new StringBuilder();
		StringBuilder messageToSendOtherPlayerBuilder = new StringBuilder();
		
		if (seek.isUseManual()) {
			//
			// Send message to the initiating user
			//
			
			messageToSendBuilder.append("Issuing match request since the seek was set to manual.\n");
			// TODO: watch out for 0 0 case... use buildVariantString() ?
			messageToSendBuilder.append(String.format("Issuing: %s (%s) %s (%s) %s %s",
					userSession.getUser().getUserName(), "----", otherPlayerUserSession.getUser().getUserName(), "----",
					seek.getSeekParams().isRated() ? "rated" : "unrated", buildVariantString(seek)));
			userSession.send(messageToSendBuilder.toString());
			
			//
			// Send message to the user that posted the seek
			//
			
			UserVars otherPlayerUserVars = otherPlayerUserSession.getUser().getUserVars();
			messageToSendOtherPlayerBuilder.append(String.format("%s accepts your seek.\n\n", userSession.getUser().getUserName()));
			// TODO: watch out for 0 0 case... use buildVariantString() ?
			messageToSendOtherPlayerBuilder.append(String.format("Challenge: %s (%s) %s (%s) %s %s %d %d.",
					userSession.getUser().getUserName(),"----",
					otherPlayerUserSession.getUser().getUserName(), "----",
					seek.getSeekParams().isRated()?"rated":"unrated", seek.getSeekParams().getVariant(), seek.getSeekParams().getTime(), seek.getSeekParams().getIncrement()));
			messageToSendOtherPlayerBuilder.append(String.format("\nYou can \"accept\" or \"decline\", or propose different parameters.%s",
					(otherPlayerUserVars.getVariables().get("bell").equals("1")?((char)7):"")));
			otherPlayerUserSession.send(messageToSendOtherPlayerBuilder.toString());
		} else {
			//
			// remove all seeks for both players, they are starting a game.
			//
			seekService.removeSeeksByUsername(userSession.getUser().getUserName());
			seekService.removeSeeksByUsername(otherPlayerUserSession.getUser().getUserName());
			
			// prepended message is only sent to the other player
			messageToSendOtherPlayerBuilder.append(String.format("%s accepts your seek.\n\n", userSession.getUser().getUserName()));
			
			//
			// start the game.
			//
			GameService gameService = GameService.getInstance();
			gameService.createGame(userSession, otherPlayerUserSession, seek.getSeekParams(), messageToSendBuilder, messageToSendOtherPlayerBuilder);
		}
	}
	
	/*
	 * used in tryAcceptSeek()
	 */
	protected String buildVariantString(Seek seek) {
		// ... unrated untimed
		// ... unrated standard 999 999
		
		int timeMinutes = seek.getSeekParams().getTime();
		int incSeconds = seek.getSeekParams().getIncrement();
		
		if (timeMinutes == 0 && incSeconds == 0) {
			return "untimed";
		} else {
			return String.format("%s %d %d",
					seek.getSeekParams().getVariant().name(), timeMinutes, incSeconds);
		}
	}
	
	// this will be needed when rating is implemented
	/*protected boolean doesUserHaveProvShowEnabled(UserSession userSession) {
		return userSession.getUser().getUserVars().getIVariables().get("provshow").equals("1");
	}*/
	
	protected boolean verifyUserCanPlay(UserSession userSession, UserSession otherUserSession) {
		if (userSession == otherUserSession) {
			userSession.send("You cannot accept your own seeks.");
			return false;
		}
		
		final String myUsername = userSession.getUser().getUserName();
		final String otherPlayerUsername = otherUserSession.getUser().getUserName();
		
		//
		// FICS does the noplay check before the censor check.
		//
		
		if (otherUserSession.getUser().isOnList(PersonalList.noplay, myUsername)) {
			userSession.send(String.format("You are on %s's noplay list.", otherPlayerUsername));
			return false;
		}
		
		if (otherUserSession.getUser().isOnList(PersonalList.censor, myUsername)) {
			userSession.send(String.format("%s is censoring you.", otherPlayerUsername));
			return false;
		}
		
		return true;
	}
}
