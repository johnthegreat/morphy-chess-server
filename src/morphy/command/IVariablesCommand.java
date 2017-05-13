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

import morphy.service.UserService;
import morphy.user.UserSession;
import morphy.utils.MorphyStringUtils;

import java.util.Map;

public class IVariablesCommand extends AbstractCommand {
	public static enum ivars {
		compressmove,defprompt,lock,ms,
		seekremove,startpos,block,gameinfo,
		pendinfo,graph,seekinfo,extascii,
		showserver,nohighlight,vthighlight,pin,
		pinginfo,boardinfo,extuserinfo,audiochat,
		seekca,showownseek,premove,smartmove,
		movecase,nowrap,allresults,
		singleboard,
		suicide,crazyhouse,losers,wildcastle,
		fr,atomic,
		xml
	}
	
	public IVariablesCommand() {
		super("IVariables");
	}

	public void process(String arguments, UserSession userSession) {
		String[] args = arguments.split(" ");
		if (args.length != 1) {
			userSession.send(getContext().getUsage());
			return;
		}
		
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
			userSession.send(String.format("Ambiguous handle \"%s\". Matches: %s",userName, MorphyStringUtils.toDelimitedString(matches," ")));
			return;
		} else if (matches.length == 1) {
			userName = matches[0];
		}
		
		if (!UserService.getInstance().isLoggedIn(userName)) {
			userSession.send(String.format("%s is not logged in.",userName));
			return;
		}
		
		// i was wondering if we should have some additional functionality
		// by perhaps having a variable name as a second argument, and then
		// only return that variable's value. i will leave this out of
		// implementation for now, since FICS does not support this.
		
		UserSession personQueried = UserService.getInstance().getUserSession(userName);
		//personQueried.getUser().getUserVars().
		
		StringBuilder builder = new StringBuilder(900);
		
		Map<String,String> ivs = personQueried.getUser().getUserVars().getIVariables();
		
		builder.append(String.format("Interface variable settings of %s:\n\n",personQueried.getUser().getUserName()));

		builder.append(String.format("compressmove=%d     defprompt=%d        lock=%d             ms=%d\n",toInt(ivs.get("compressmove")),toInt(ivs.get("defprompt")),toInt(ivs.get("lock")),toInt(ivs.get("ms"))));
		builder.append(String.format("seekremove=%d       startpos=%d         block=%d            gameinfo=%d\n",toInt(ivs.get("seekremove")),toInt(ivs.get("startpos")),toInt(ivs.get("block")),toInt(ivs.get("gameinfo"))));
		builder.append(String.format("pendinfo=%d         graph=%d            seekinfo=%d         extascii=%d\n",toInt(ivs.get("pendinfo")),toInt(ivs.get("graph")),toInt(ivs.get("seekinfo")),toInt(ivs.get("extascii"))));
		builder.append(String.format("showserver=%d       nohighlight=%d      vthighlight=%d      pin=%d\n",toInt(ivs.get("showserver")),toInt(ivs.get("nohighlight")),toInt(ivs.get("vthighlight")),toInt(ivs.get("pin"))));
		builder.append(String.format("pinginfo=%d         boardinfo=%d        extuserinfo=%d      audiochat=%d\n",toInt(ivs.get("pinginfo")),toInt(ivs.get("boardinfo")),toInt(ivs.get("extuserinfo")),toInt(ivs.get("audiochat"))));
		builder.append(String.format("seekca=%d           showownseek=%d      premove=%d          smartmove=%d\n",toInt(ivs.get("seekca")),toInt(ivs.get("showownseek")),toInt(ivs.get("premove")),toInt(ivs.get("smartmove"))));
		builder.append(String.format("movecase=%d         nowrap=%d           allresults=%d\n",toInt(ivs.get("movecase")),toInt(ivs.get("nowrap")),toInt(ivs.get("allresults"))));
		builder.append(String.format("singleboard=%d\n",toInt(ivs.get("singleboard"))));
		builder.append(String.format("suicide=%d          crazyhouse=%d       losers=%d           wildcastle=%d\n",toInt(ivs.get("suicide")),toInt(ivs.get("crazyhouse")),toInt(ivs.get("losers")),toInt(ivs.get("wildcastle"))));
		builder.append(String.format("fr=%d               atomic=%d\n",toInt(ivs.get("fr")),toInt(ivs.get("atomic"))));
		builder.append(String.format("xml=%d\n",toInt(ivs.get("xml"))));

		userSession.send(builder.toString());
	}
	
	private int toInt(String str) {
		return str != null ? Integer.parseInt(str) : 0;
	}
}
