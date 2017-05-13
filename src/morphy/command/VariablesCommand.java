/*
 *   Morphy Open Source Chess Server
 *   Copyright (C) 2008-2010, 2017  http://code.google.com/p/morphy-chess-server/
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

import org.apache.commons.lang.StringUtils;

import morphy.service.UserService;
import morphy.user.UserSession;
import morphy.utils.MorphyStringUtils;

public class VariablesCommand extends AbstractCommand {
	public VariablesCommand() {
		super("Variables");
	}

	public void process(String arguments, UserSession userSession) {
		String[] args = arguments.split(" ");
		if (args.length != 1) {
			userSession.send(getContext().getUsage());
			return;
		}
		
		// i was wondering if we should have some additional functionality
		// by perhaps having a variable name as a second argument, and then
		// only return that variable's value. i will leave this out of
		// implementation for now, since FICS does not support this.
		
		String userName = args[0];
		
		if (userName.equals("")) {
			userName = userSession.getUser().getUserName();
		}
		
		if (userName.length() < 2) {
			userSession.send("You must provide at least 2 characters of the name.");
			return;
		}
		
		String[] matches = UserService.getInstance().completeHandle(userName);
		if (matches.length > 1) {
			userSession.send(String.format("Ambiguous handle \"%s\". Matches: %s",userName,MorphyStringUtils.toDelimitedString(matches," ")));
			return;
		} else if (matches.length == 1) {
			userName = matches[0];
		}
		
		UserSession personQueried = UserService.getInstance().getUserSession(userName);
		if (!UserService.getInstance().isValidUsername(userName)) {
			userSession.send("There is no player matching the name " + userName + ".");
			return;
		}
		
		java.util.HashMap<String,String> variables = personQueried.getUser().getUserVars().getVariables();
		
		StringBuilder builder = new StringBuilder(900);
		
		builder.append("Variable settings of " + personQueried.getUser().getUserName() + ":\n\n");
		builder.append(
				String.format("time=%-4d    private=%-4d    shout=%-4d         pin=%-4d           style=%d\n"		,
						toInt(variables.get("time")),toInt(variables.get("private")),toInt(variables.get("shout")),toInt(variables.get("pin")),toInt(variables.get("style"))));
		builder.append(
				String.format("inc=%-4d     jprivate=%-4d   cshout=%-4d        notifiedby=%-4d    flip=%d\n"		,
						toInt(variables.get("inc")),toInt(variables.get("jprivate")),toInt(variables.get("cshout")),toInt(variables.get("notifiedby")),toInt(variables.get("flip"))));
		builder.append(
				String.format("rated=%-4d   kibitz=%-4d     availinfo=%-4d     highlight=%-4d\n"					,
						toInt(variables.get("rated")),toInt(variables.get("kibitz")),toInt(variables.get("availinfo")),toInt(variables.get("highlight"))));
		builder.append(
				String.format("open=%-4d    automail=%-4d   kiblevel=%-4d      availmin=%-4d      bell=%d\n"		,
						toInt(variables.get("open")),toInt(variables.get("automail")),toInt(variables.get("kiblevel")),toInt(variables.get("availmin")),toInt(variables.get("bell"))));
		builder.append(
				String.format("             pgn=%-4d        tell=%-4d          availmax=%-4d      width=%d\n"		,
						toInt(variables.get("pgn")),toInt(variables.get("tell")),toInt(variables.get("availmax")),toInt(variables.get("width"))));
		builder.append(
				String.format("bugopen=%-4d                 ctell=%-4d         gin=%-4d           height=%d\n"		,
						toInt(variables.get("bugopen")),toInt(variables.get("ctell")),toInt(variables.get("gin")),toInt(variables.get("height")))); 
		builder.append(
				String.format("             mailmess=%-4d                      seek=%-4d          ptime=%d\n"		,
						toInt(variables.get("mailmess")),toInt(variables.get("seek")),toInt(variables.get("ptime"))));
		builder.append(
				String.format("tourney=%-4d messreply=%-4d  chanoff=%-4d       showownseek=%-4d   tzone=%s\n"		,
						toInt(variables.get("tourney")),toInt(variables.get("messreply")),toInt(variables.get("chanoff")),toInt(variables.get("showownseek")),variables.get("tzone")));
		builder.append(
				String.format("provshow=%-4d                silence=%-4d                          Lang=%s\n"		,
						toInt(variables.get("provshow")),toInt(variables.get("silence")),StringUtils.upperCase(variables.get("lang").substring(0,1)) + variables.get("lang").substring(1)));
		builder.append(
				String.format("autoflag=%-4dunobserve=%-4d  echo=%-4d          examine=%-4d\n"						,
						toInt(variables.get("autoflag")),toInt(variables.get("unobserve")),toInt(variables.get("echo")),toInt(variables.get("examine"))));
		builder.append(
				String.format("minmovetime=%-4d             tolerance=%-4d     noescape=%-4d      notakeback=%d\n\n",
						toInt(variables.get("minmovetime")),toInt(variables.get("tolerance")),toInt(variables.get("noescape")),toInt(variables.get("notakeback"))));
		builder.append(String.format("Prompt: %s\n",variables.get("prompt")));
		
		String v = variables.get("interface");
		if (!v.equals("NULL"))
			builder.append(String.format("Interface: \"%s\"",v));
		
		userSession.send(builder.toString());
	}
	
	private int toInt(String s) {
		return Integer.parseInt(s);
	}
	
	public static enum variables {
		myinterface("interface", String.class), myprivate("private",
				String.class), time, inc, rated, open, bugopen, tourney, provshow, autoflag, minmovetime, prompt ("prompt",String.class), jprivate, kibitz, automail, pgn, mailmess, messreply, unobserve, shout, cshout, availinfo, kiblevel, tell, ctell, chanoff, silence, echo, tolerance, pin, notifiedby, highlight, availmin, availmax, gin, seek, showownseek, examine, noescape, style, flip, bell, width, height, ptime, tzone(
				"tzone", String.class), lang("Lang", String.class), notakeback, busy ("busy",String.class);

		variables() {
			this.name = this.name();
			this.type = Integer.class;
		}

		variables(String name, Class<?> type) {
			this.name = name;
			this.type = type;
		}

		private String name;
		private Class<?> type;

		public String getName() {
			return name;
		}

		public Class<?> getType() {
			return type;
		}
	}
}
