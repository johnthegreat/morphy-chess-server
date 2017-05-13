/*
 *   Morphy Open Source Chess Server
 *   Copyright (C) 2008-2011, 2017  http://code.google.com/p/morphy-chess-server/
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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;

import morphy.command.VariablesCommand.variables;
import morphy.game.request.MatchRequest;
import morphy.game.request.PartnershipRequest;
import morphy.game.request.Request;
import morphy.game.style.StyleInterface;
import morphy.service.RequestService;
import morphy.user.UserInfoList;
import morphy.user.UserSession;
import morphy.utils.john.TimeZoneUtils;

public class SetCommand extends AbstractCommand {
	public class BadValueException extends Exception {
		private static final long serialVersionUID = 1L;
		
		private String varname;
		
		public BadValueException(String variable) {
			this.varname = variable;
		}
		
		@Override
		public String getMessage() {
			return "Bad value given for variable \"" + varname + "\".";
		}
	}
	
	public SetCommand() {
		super("Set");
	}

	public void process(String arguments, UserSession userSession) {
		int pos = arguments.indexOf(" ");
		if (!arguments.startsWith("busy") && pos == -1) { userSession.send(getContext().getUsage()); return; }
		if (arguments.equals("busy")) { pos = arguments.length(); }
		
		String setWhat = arguments.substring(0,pos).trim();
		String message = arguments.substring(pos).trim();
		
		variables[] varr = variables.values();
		java.util.Arrays.sort(varr);
		
		variables[] v = findAllMatches(varr,setWhat);
		//System.out.println(java.util.Arrays.toString(v));
		
		if (StringUtils.isNumeric(setWhat)) {
			// finger notes
			int val = Integer.parseInt(setWhat);
			
			if (val >= 1 && val <= UserInfoList.MAX_NOTES) {
				List<String> notes = userSession.getUser().getUserInfoLists().get(UserInfoList.notes);
				if (notes == null) {
					notes = new ArrayList<String>(UserInfoList.MAX_NOTES);
					userSession.getUser().getUserInfoLists().put(UserInfoList.notes,notes);
				}
				
				if (notes.size() == 0) {
					while(notes.size() < 10) {
						notes.add("");
					}
				}
				
				notes.set(val-1,message);
				
				//Plan line 6 changed to 'hi'.
				//Plan line 6 cleared. 
				String returnmess = (message.equals("") ? 
						"Plan line " + val + " cleared." : 
						"Plan line " + val + " changed to '" + message + "'.");
				userSession.send(returnmess);
				return;
			} else {
				userSession.send("No such variable \"" + val + "\".");
				return;
			}
		} else if (setWhat.matches("f[1-9]")) {
			// var notes
		} else if (v.length > 0) {
			
			// variables
			if (v.length > 1) {
				StringBuilder errmess = new StringBuilder("Ambiguous variable \"" + setWhat + "\". Matches: ");
				for(int i=0;i<v.length;i++) {
					variables elem = v[i];
					errmess.append(elem.name());
					if (i != v.length-1)
						errmess.append(" ");
				}
				userSession.send(errmess.toString());
				return;
			}
			
			if (v.length == 1) {
				variables var = v[0];
				 
				if (var.getType().equals(Integer.class) && !StringUtils.isNumeric(message)) {
					if (message.equalsIgnoreCase("true")) message = "1"; else
					if (message.equalsIgnoreCase("false")) message = "0"; else {
						userSession.send("Bad value given for variable \"" + var.getName() + "\".");
						return;
					}
				}
				
				// no validation for string inputs...
				morphy.user.UserVars uv = userSession.getUser().getUserVars();
				
				// TODO finish this.
				String returnmessage = "";
				String setOrUnset = (message.equals("1")?"set":"unset");
				String nowOrNot = (message.equals("1")?"now":"not");
				String onOrOff = (message.equals("1")?"on":"off");
				try {
					switch(var) {
						case time: 
							if (!StringUtils.isNumeric(message)) throw new BadValueException("time");
							returnmessage = "Default time set to " + message + ".";
							break;
						case inc:
							if (!StringUtils.isNumeric(message)) throw new BadValueException("time");
							returnmessage = "Default increment set to " + message + ".";
							break;
						case rated:
							if (!is0or1(message)) throw new BadValueException("rated");
							if (!userSession.getUser().isRegistered()) 
								returnmessage = "Unregistered users cannot change their rated status.";
							else
								returnmessage = "Games will be " + (message.equals("1")?"rated":"unrated")+ " by default.";
							break;
						case open: 
							if (!is0or1(message)) throw new BadValueException("open");
							returnmessage = "You are " + (message.equals("1")?"now open to receive":"no longer receiving") + " match requests.";
							break;
						case tourney: 
							if (!is0or1(message)) throw new BadValueException("tourney");
							returnmessage = "Your tournament variable is " + (message.equals("1")?"now":"no longer") + " set.";
							break;
						case provshow: 
							if (!is0or1(message)) throw new BadValueException("provshow");
							returnmessage = "Provisional or inactive ratings will now" + (message.equals("0")?" not":"") + " be marked.";
							break;
						case autoflag: 
							if (!is0or1(message)) throw new BadValueException("autoflag");
							returnmessage = "Auto-flagging " + (message.equals("1")?"enabled":"disabled") + ".";
							break;
						case minmovetime: 
							if (!is0or1(message)) throw new BadValueException("minmovetime");
							returnmessage = "You will" + (message.equals("1")?"":" not") + " request minimum move time when games start.";
							break;
						case myprivate:
							if (!is0or1(message)) throw new BadValueException("private");
							returnmessage = "Your games will" + (message.equals("1")?"":" not") + " be private.";
							break;
						case jprivate: 
							if (!userSession.getUser().isRegistered())
								returnmessage = "Only registered players may keep a journal.";
							else
								returnmessage = "Your journal will" + (message.equals("1")?"":" not")+ " be private.";
							
							if (!is0or1(message)) throw new BadValueException("jprivate");
							break;
						case automail:
							if (!userSession.getUser().isRegistered())
								returnmessage = "Unregistered players may not have games mailed.";
							else
								returnmessage = "";
							
							if (!is0or1(message)) throw new BadValueException("automail");
							break;
						case pgn: 
							if (!is0or1(message)) throw new BadValueException("pgn");
							returnmessage = "Games will now be mailed to you in " + (message.equals("1")?"PGN":"server format") + ".";
							break;
						case mailmess:
							if (!userSession.getUser().isRegistered())
								returnmessage = "Unregistered players may not receive messages.";
							else
								returnmessage = "";
							if (!is0or1(message)) throw new BadValueException("mailmess");
							break;
						case messreply:
							if (!userSession.getUser().isRegistered())
								returnmessage = "Unregistered players cannot use this variable.";
							else
								returnmessage = "";
							if (!is0or1(message)) throw new BadValueException("messreply");
							break;
						case unobserve:
							if (message.equals("1")) returnmessage = "You will now only auto unobserve on matches.";
							if (message.equals("2")) returnmessage = "You will now only auto unobserve on examine.";
							if (message.equals("3")) returnmessage = "You will not auto unobserve.";
							if (!message.matches("[1|2|3]")) throw new BadValueException("unobserve");
							break;
						case shout: 
							if (!is0or1(message)) throw new BadValueException("shout");
							returnmessage = "You will " + nowOrNot + " hear shouts.";
							break;
						case cshout: 
							if (!is0or1(message)) throw new BadValueException("cshout");
							returnmessage = "You will " + nowOrNot + " hear cshouts.";
							break;
						case kibitz: 
							if (!is0or1(message)) throw new BadValueException("kibitz");
							returnmessage = "You will " + nowOrNot + " hear kibitzes.";
							break;
						case kiblevel:
							if (Integer.parseInt(message) < 0 || Integer.parseInt(message) >= 10000) throw new BadValueException("kiblevel");
							returnmessage = "Kibitz level now set to " + message + ".";
							break;
						case tell:
							if (!is0or1(message)) throw new BadValueException("tell");
							
							if (message.equals("0")) returnmessage = "You will not hear direct tells from unregistered users.";
							if (message.equals("1")) returnmessage = "You will now hear direct tells from all users.";
							break;
						case ctell:
							if (!is0or1(message)) throw new BadValueException("ctell");
							
							if (message.equals("0")) returnmessage = "You will not hear channel tells from unregistered users.";
							if (message.equals("1")) returnmessage = "You will now hear channel tells from all users.";
							break;
						case chanoff:
							if (!is0or1(message)) throw new BadValueException("chanoff");
							returnmessage = "You will " + nowOrNot + " hear channel tells.";
							break;
						case silence:
							if (!is0or1(message)) throw new BadValueException("silence");
							returnmessage = "You will now" + (message.equals("0")?" not":"") + " play games in silence.";
							break;
						case echo:
							if (!is0or1(message)) throw new BadValueException("echo");
							returnmessage = "You will now"  + (message.equals("0")?" not":"") + " hear communications echoed.";
							break;
						case pin:
							if (!is0or1(message)) throw new BadValueException("pin");
							returnmessage = "You will " + nowOrNot + " hear logins/logouts.";
							break;
						case notifiedby:
							if (!is0or1(message)) throw new BadValueException("notifiedby");
							returnmessage = "You will " + nowOrNot + " hear if people notify you, but you don't notify them.";
							break;
						case availinfo:
							if (!is0or1(message)) throw new BadValueException("availinfo");
							returnmessage = "You will" + (message.equals("0")?" not":"") + " receive info on who is available to play.";
							break;
						case seek:
							if (!is0or1(message)) throw new BadValueException("seek");
							returnmessage = "You will " + nowOrNot + " see seek ads.";
							break;
						case bugopen:
							if (!is0or1(message)) throw new BadValueException("bugopen");
							returnmessage = "You are " + nowOrNot + " open for bughouse.";
							break;
						case tolerance:
							if (!message.matches("[1|2|3|4|5]")) throw new BadValueException("tolerance");
							returnmessage = "Tolerance level set to " + message + ".";
							break;
						case bell:
							if (!is0or1(message)) throw new BadValueException("bell");
							returnmessage = "Bell " + onOrOff + ".";
							break;
						case availmin:
						case availmax:
							if (!is0or1(message)) throw new BadValueException(var.name());
							
							if (Integer.parseInt(uv.getVariables().get("availmin")) > Integer.parseInt(uv.getVariables().get("availmax")))
								returnmessage = "You can't set availmin to more than availmax.";
							else returnmessage = "You will be notified of availability with blitz ratings " + uv.getVariables().get("availmin") + " - " + uv.getVariables().get("availmax") + ".";
							break;
						case gin:
							if (!is0or1(message)) throw new BadValueException("gin");
							returnmessage = "You will " + nowOrNot + " hear game results.";
							break;
						case showownseek: 
							if (!is0or1(message)) throw new BadValueException("showownseek");
							returnmessage = "You will " + nowOrNot + " see your own seeks.";
							break;
						case examine: 
							if (!is0or1(message)) throw new BadValueException("examine");
							returnmessage = "You will now" + (message.equals("0")?" not":"") + " enter examine mode after a game.";
							break;
						case noescape:
							if (!is0or1(message)) throw new BadValueException("noescape");
							returnmessage = "You will" + (message.equals("0")?" not":"") + " request noescape when games start.";
							break;
						case style:
							if (!StringUtils.isNumeric(message)) throw new BadValueException("style");
							int style = Integer.parseInt(message);
							if (style < 1 || style > 13) throw new BadValueException("style");
							returnmessage = "Style " + message + " set.";
							break;
						case flip: 
							if (!is0or1(message)) throw new BadValueException("flip");
							returnmessage = "Flip " + onOrOff + ".";
							break;
						case highlight: 
							if (Integer.parseInt(message) < 1 || Integer.parseInt(message) > 15) throw new BadValueException("highlight");
							returnmessage = "Highlight is now style " + message + ".";
							break;
						case width: 
							if (Integer.parseInt(message) < 32 || Integer.parseInt(message) > 240) throw new BadValueException("width");
								returnmessage = "Width set to " + message + ".";
							break;
						case height:
							if (Integer.parseInt(message) < 5 || Integer.parseInt(message) > 240) throw new BadValueException("width");
								returnmessage = "Height set to " + message + ".";
							break;
						case ptime:
							if (!is0or1(message)) throw new BadValueException("ptime");
							if (message.equals("0")) returnmessage = "Your prompt will now not show the time.";
							if (message.equals("1")) returnmessage = "Your prompt will now show the time (SERVER).";
							break;
						case tzone: {
							message = message.toUpperCase();
							TimeZone tz = TimeZoneUtils.getTimeZone(message);
							if (message.equals("GMT") && tz.equals(TimeZone.getTimeZone("GMT")))
								returnmessage = "Invalid timezone - see 'help timezones'";
							returnmessage = "Timezone set to " + message.toUpperCase() + " (GMT ...).";
							break;
						}
						case lang: {
							String[] arr = getDistinctLocalesByLanguage();
							java.util.Arrays.sort(arr);
							
							int index = java.util.Arrays.binarySearch(arr,message);
							if (index != Math.abs(index)) { 
								throw new BadValueException("lang");
							} else {
								returnmessage = "Language set to " + message + ".";
							}
							break;
						}
						case notakeback:
							if (!is0or1(message)) throw new BadValueException("notakeback");
							returnmessage = "You will " + nowOrNot + " allow takebacks.";
							break;
						case myinterface: returnmessage = ""; break;
						case prompt: returnmessage = "Prompt set to \"" + message + "\""; break;
						case busy: {
							if (message.equals("")) {
								returnmessage = "Your \"busy\" string was cleared.";
								break;
							} else {
								returnmessage = "Your \"busy\" string was set to \"" + message + "\".";
								break;
							}
						}
						
						default:
							returnmessage = var.getName() + " " + setOrUnset + ".";
					}
					
					uv.update(setWhat.toLowerCase(),message);
					
					if (var == variables.style) {
						int style = Integer.parseInt(message);
						StyleInterface si = null;
						si = getStyle(style);
						if (si == null) {
							userSession.send("That style is not available at this time, please try again later.");
						} else {
							userSession.getUser().getUserVars().setStyle(si);
						}
					}
					
					RequestService rq = RequestService.getInstance();
					if (var == VariablesCommand.variables.open && message.equals("0")) {
						final String myUsername = userSession.getUser().getUserName();
						
						List<Request> list = rq.findAllToRequestsByType(userSession,MatchRequest.class);
						if (list != null) {
							for(Request r : list) {
								r.getFrom().send(String.format("%s, whom you were challenging, has become unavailable for matches.\nChallenge to %s withdrawn.",myUsername,myUsername));
								rq.removeRequest(r);
								userSession.send(String.format("Challenge from %s removed.",r.getFrom().getUser().getUserName()));
							}
						}
					}
					
					if (var == variables.bugopen && message.equals("0")) {
						final String myUsername = userSession.getUser().getUserName();
						
						List<Request> list = rq.findAllToRequestsByType(userSession,PartnershipRequest.class);
						for(Request r : list) {
							r.getFrom().send(String.format("%s, whom you were offering a partnership with, has become unavailable for bughouse.\n" +
									"Partnership offer to %s withdrawn.",myUsername,myUsername));
							rq.removeRequest(r);
							userSession.send(String.format("Partnership offer from %s removed.",r.getFrom().getUser().getUserName()));
						}
					}
					
					userSession.send(returnmessage);
				
				} catch(BadValueException e) {
					userSession.send(e.getMessage());
				}
			}
		} else {
			userSession.send( String.format("No such variable \"%s\".", setWhat ) );
		}
	}
	
	/** Uses reflection  */
	private StyleInterface getStyle(int styleNum) {
		try {
			Class<?> myClass = Class.forName("morphy.game.style.Style" + styleNum);
			return (StyleInterface) myClass.getMethod("getSingletonInstance").invoke(null);
		} catch (ClassNotFoundException e) {
			e.printStackTrace(System.err);
		} catch (IllegalAccessException e) {
			e.printStackTrace(System.err);
		} catch (IllegalArgumentException e) {
			e.printStackTrace(System.err);
		} catch (SecurityException e) {
			e.printStackTrace(System.err);
		} catch (InvocationTargetException e) {
			e.printStackTrace(System.err);
		} catch (NoSuchMethodException e) {
			e.printStackTrace(System.err);
		}
		return null;
	}
	
	private variables[] findAllMatches(variables[] vars,String varname) {
		java.util.List<variables> arr = new java.util.ArrayList<variables>();
		
		for(variables v : vars) {
			String tmp = v.name();
			if (tmp.indexOf("my") == 0) tmp = tmp.substring(2);
			if (tmp.startsWith(varname)) {
				arr.add(v);
			}
		}
		
		return arr.toArray(new variables[arr.size()]);
	}
	
	private String[] getDistinctLocalesByLanguage() {
		List<String> list = new ArrayList<String>();
		Locale[] arr = Locale.getAvailableLocales();
		for(Locale l : arr) {
			String v = l.getDisplayLanguage();
			if (!list.contains(v))
				list.add(v);
		}
		return list.toArray(new String[list.size()]);
	}
	
	protected boolean is0or1(String in) {
		return in.equals("1") || in.equals("0");
	}
}
