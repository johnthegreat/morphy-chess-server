/*
 *   Morphy Open Source Chess Server
 *   Copyright (C) 2008-2010  http://code.google.com/p/morphy-chess-server/
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
		
		// i was wondering if we should have some additional functionality
		// by perhaps having a variable name as a second argument, and then
		// only return that variable's value. i will leave this out of
		// implementation for now, since FICS does not support this.
		
		String userName = args[0];
		
		if (userName.equals(""))
			userName = userSession.getUser().getUserName();
		
		UserSession personQueried = UserService.getInstance().getUserSession(userName);
		//personQueried.getUser().getUserVars().
		
		StringBuilder builder = new StringBuilder(900);
		
		builder.append("Interface variable settings of " + personQueried.getUser().getUserName() + ":\n\n");

		builder.append("compressmove=0     defprompt=0        lock=0             ms=1\n");
		builder.append("seekremove=0       startpos=1         block=0            gameinfo=1\n");
		builder.append("pendinfo=0         graph=0            seekinfo=0         extascii=0\n");
		builder.append("showserver=0       nohighlight=0      vthighlight=0      pin=0\n");
		builder.append("pinginfo=0         boardinfo=0        extuserinfo=0      audiochat=0\n");
		builder.append("seekca=0           showownseek=0      premove=1          smartmove=0\n");
		builder.append("movecase=0         nowrap=0           allresults=0\n");
		builder.append("singleboard=0\n");
		builder.append("suicide=0          crazyhouse=0       losers=0           wildcastle=0\n");
		builder.append("fr=0               atomic=0\n");
		builder.append("xml=0\n");

		userSession.send(builder.toString());
	}
}
