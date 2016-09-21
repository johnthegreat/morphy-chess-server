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

import morphy.command.IVariablesCommand.ivars;
import morphy.user.UserSession;

public class ISetCommand extends AbstractCommand {
	public ISetCommand() {
		super("iset");
	}

	public void process(String arguments, UserSession userSession) {
		final int pos = arguments.indexOf(" ");
		if (pos == -1) { userSession.send(getContext().getUsage()); return; }
		
		String setWhat = arguments.substring(0,pos).trim();
		String value = arguments.substring(pos).trim();
		
		ivars[] arr = ivars.values();
		java.util.Arrays.sort(arr);
		
		ivars[] matches = findAllMatches(arr,setWhat);
		if (matches.length > 0) {
			if (matches.length > 1) {
				StringBuilder errmess = new StringBuilder("Ambiguous ivariable \"" + setWhat + "\". Matches: ");
				for(int i=0;i<matches.length;i++) {
					ivars v = matches[i];
					errmess.append(v.name());
					if (i != matches.length-1)
						errmess.append(" ");
				}
				userSession.send(errmess.toString());
				return;
			} else {
				setWhat = matches[0].name();
			}
			
			if (value.equalsIgnoreCase("true") || value.equals("1") || value.equalsIgnoreCase("false") || value.equals("0")) {
				// this value is valid.
				
				if (value.equalsIgnoreCase("true"))
					value = "1";
				if (value.equalsIgnoreCase("false"))
					value = "0";
				
//				if (userSession.getUser().getUserVars().getIVariables().get("lock").equals("1")) {
//					userSession.send("Cannot alter: Interface setting locked.");
//					return;
//				}
				// set the variable here.
				userSession.getUser().getUserVars().getIVariables().put(setWhat,value);
				
				userSession.send(setWhat + " " + (value.equals("1")?"set":"unset") + ".");
				
			}
			
		} else {
			userSession.send( String.format("No such ivariable \"%s\".", value ) );
		}
	}
	
	private ivars[] findAllMatches(ivars[] myivars,String varname) {
		java.util.List<ivars> arr = new java.util.ArrayList<ivars>();
		
		for(ivars v : myivars) {
			if (v.name().startsWith(varname))
				arr.add(v);
		}
		
		return arr.toArray(new ivars[arr.size()]);
	}
}
