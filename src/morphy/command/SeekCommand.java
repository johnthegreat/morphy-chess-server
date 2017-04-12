/*
 *   Morphy Open Source Chess Server
 *   Copyright (C) 2016, 2017 http://code.google.com/p/morphy-chess-server/
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
import morphy.game.params.GameParams;
import morphy.service.GameService;
import morphy.service.SeekService;
import morphy.user.SocketChannelUserSession;
import morphy.user.UserSession;
import morphy.user.UserVars;
import morphy.utils.MorphyStringTokenizer;
import sun.misc.Regexp;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by John on 09/23/2016.
 */
public class SeekCommand extends AbstractCommand {
	public SeekCommand() {
		super("seek");
	}
	
	public void process(String arguments, UserSession userSession) {
		// Your seek matches one already posted by guesttest.
		// Your seek matches one posted by guesttest.
		
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
		
		Variant variant = null;
		Seek.SeekRatingRange ratingRange = new Seek.SeekRatingRange(0,9999);
		
		List<String> otherParams = new ArrayList<String>();
		while(tokenizer.hasMoreTokens()) {
			otherParams.add(tokenizer.nextToken());
		}
		
		for(int i=0;i<otherParams.size();i++) {
			String param = otherParams.get(i).toLowerCase();
			
			//
			// Handle Color Requested
			//
			
			if (param.equals("w") || param.equals("white")) {
				seek.getSeekParams().setColorRequested(GameParams.ColorRequested.White);
				continue;
			} else if (param.equals("b") || param.equals("black")) {
				seek.getSeekParams().setColorRequested(GameParams.ColorRequested.Black);
				continue;
			}
			
			//
			// Handle Seek Flags (manual / formula)
			//
			
			// TODO: handle param 'mf' or 'fm' case
			
			if (param.equals("m") || param.equals("manual")) {
				seek.setUseManual(true);
				continue;
			}
			
			if (param.equals("f") || param.equals("formula")) {
				seek.setUseFormula(true);
				continue;
			}
			
			//
			// Handle Variant
			//
			
			if (param.equals("zh") || param.indexOf("cra") == 0) {
				variant = Variant.crazyhouse;
				continue;
			} else if (param.indexOf("bug") == 0) {
				variant = Variant.bughouse;
				continue;
			}
			
			//
			// Handle Rating Range
			//
			
			Pattern pattern = Pattern.compile("(\\d+)-(\\d+)");
			Matcher matcher = pattern.matcher(param);
			if (matcher.matches()) {
				int fromRating = Integer.parseInt(matcher.group(1));
				int toRating = Integer.parseInt(matcher.group(2));
				
				if (fromRating > toRating) {
					userSession.send("Invalid rating range specified.");
					return;
				} else {
					ratingRange.setFromRating(fromRating);
					ratingRange.setToRating(toRating);
				}
			}
		}
		
		seek.getSeekParams().setVariant(variant);
		seek.setRatingRange(ratingRange);
		
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
		
		if (variant == null) {
			// must be regular chess.
			variant = Variant.getVariantBasedOnTimeAndIncrement(seek.getSeekParams().getTime(), seek.getSeekParams().getIncrement());
		}
		seek.getSeekParams().setVariant(variant);
		
		int numPlayersWhoSawSeek = seekService.registerSeek(seek);
		int seekIndex = seek.getSeekIndex();
		messageToSendBuilder.append(String.format("Your seek has been posted with index %d.\n(%d player(s) saw the seek.)", seekIndex, numPlayersWhoSawSeek));
		
		userSession.send(messageToSendBuilder.toString());
		messageToSendBuilder = null;
		
		// check to see if this seek matches any other existing seeks
		Seek[] matchingSeeks = seekService.findMatchingSeeks(seek);
		if (matchingSeeks != null && matchingSeeks.length > 0) {
			Seek firstMatchingSeek = matchingSeeks[0];
			
			messageToSendBuilder = new StringBuilder(String.format("Your seek matches one posted by %s.\n\n", firstMatchingSeek.getUserSession().getUser().getUserName()));
			StringBuilder messageToSendOtherBuilder = new StringBuilder(String.format("Your seek matches one already posted by %s.\n\n", seek.getUserSession().getUser().getUserName()));
			seekService.tryAcceptSeek(userSession, firstMatchingSeek, messageToSendBuilder, messageToSendOtherBuilder);
		}
	}
}
