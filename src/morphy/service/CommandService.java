/*
 *   Morphy Open Source Chess Server
 *   Copyright (C) 2008-2011, 2016-2017  http://code.google.com/p/morphy-chess-server/
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
package morphy.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import chesspresso.Chess;
import morphy.command.*;
import morphy.command.admin.AHelpCommand;
import morphy.command.admin.AddCommentCommand;
import morphy.command.admin.AddPlayerCommand;
import morphy.command.admin.AdminCommand;
import morphy.command.admin.AnnounceCommand;
import morphy.command.admin.AnnunregCommand;
import morphy.command.admin.NukeCommand;
import morphy.command.admin.ShutdownCommand;
import morphy.game.ExaminedGame;
import morphy.game.Game;
import morphy.game.Move;
import morphy.game.ProcessGameMoveHelper;
import morphy.move.parser.ChesspressoMoveParser;
import morphy.move.parser.NotationParser;
import morphy.move.parser.MoveParseResult;
import morphy.user.SocketChannelUserSession;
import morphy.utils.MorphyStringUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import chesspresso.move.IllegalMoveException;

public class CommandService implements Service {
	protected static Log LOG = LogFactory.getLog(CommandService.class);
	protected static Pattern listAliasPattern = Pattern
			.compile("(\\+|-|=)(fm|im|gm|wfm|wim|wgm|blind|teams|computer|" +
					"tm|ca|sr|td|censor|gnotify|noplay|" +
					"notify|channel|idlenotify)");
	
	
	/** Returns lists with a given prefix (case insensitive).<br />
	 * Returns a zero-length array if none found. */
	private String[] completeListAlias(String prefix) {
		prefix = prefix.toLowerCase();
		String[] lists = { "fm","im","gm","wfm","wim","wgm","blind","teams",
				"computer","tm","ca","sr","td","censor","gnotify","noplay",
				"notify","channel","idlenotify","admin" };
		
		List<String> found = new ArrayList<String>();
		for(String item : lists) {
			if (item.startsWith(prefix)) {
				found.add(item);
			}
		}
		return found.toArray(new String[found.size()]);
	}
	
	private static final Class<?>[] socketCommandsClasses = {
		// please try to maintain this list in alphabetical order
		
		AbortCommand.class,
		AcceptCommand.class,
		AddCommentCommand.class,
	 	AddListCommand.class,
	 	AddPlayerCommand.class,
		AdminCommand.class,
		AHelpCommand.class,
		AllObserversCommand.class,
		AnnounceCommand.class,
		AnnunregCommand.class,
	
		BclockCommand.class,
		BnameCommand.class,
		BratingCommand.class,
		BugWhoCommand.class,
		
		ClearmessagesCommand.class,
		
		DateCommand.class,
		DeclineCommand.class,
		
		ExamineCommand.class,
		
		FingerCommand.class,
		
		GamesCommand.class,
		GetgameCommand.class,
		
		HandlesCommand.class,
		HelpCommand.class,
		
		InchannelCommand.class,
		ISetCommand.class,
		ItShoutCommand.class,
		IVariablesCommand.class,
		
		LLoginsCommand.class,
		LoginsCommand.class,
		
		MatchCommand.class,
		MessageCommand.class,
		MessagesCommand.class,
		MexamineCommand.class,
		MovesCommand.class,
		
		NewsCommand.class,
		NukeCommand.class,
		
		ObserveCommand.class,
		
		PartnerCommand.class,
		PauseCommand.class,
		PendingCommand.class,
		PlayCommand.class,
		
		QtellCommand.class,
		QuitCommand.class,
		
		RefreshCommand.class,
		RemoveListCommand.class,
			
		SeekCommand.class,
		SetCommand.class,
		ShoutCommand.class,
		ShowListCommand.class,
		ShutdownCommand.class,
		SoughtCommand.class,
		SRCommand.class,
		SummonCommand.class,
		
		TellCommand.class,
		
		UnexamineCommand.class,
		UnseekCommand.class,
		UptimeCommand.class,
		
		VariablesCommand.class,
		
		WclockCommand.class,
		WhoCommand.class,
		WithdrawCommand.class,
		WnameCommand.class,
		WratingCommand.class,
		
		ZNotifyCommand.class
	};

	protected List<Command> commands = new ArrayList<Command>(100);
	protected Map<String, Command> firstWordToCommandMap = new TreeMap<String, Command>();

	private static final CommandService singletonInstance = new CommandService();

	public static CommandService getInstance() {
		return singletonInstance;
	}

	private CommandService() {
		long startTime = System.currentTimeMillis();

		for (Class<?> clazz : socketCommandsClasses) {
			try {
				Command command = (Command) clazz.newInstance();
				commands.add(command);
			} catch (Throwable t) {
				if (LOG.isErrorEnabled())
					LOG.error("Error inializing SocketCommand: "
								+ clazz.getName(), t);
			}
		}
		Collections.sort(commands);

		for (Command command : commands) {
			String name = command.getContext().getName().toLowerCase();
			firstWordToCommandMap.put(command.getContext().getName(), command);
			for (String alias : command.getContext().getAliases()) {
				firstWordToCommandMap.put(alias, command);
			}
			if (name.length() > 1) {
				for (int i = command.getContext().getName().length() - 2; i >= 0; i--) {
					String shortcut = command.getContext().getName()
							.toLowerCase().substring(0, i);
					boolean isUnique = true;
					for (Command commandToCheck : commands) {
						if (commandToCheck != command) {
							String nameToCompare = commandToCheck.getContext()
									.getName().toLowerCase();
							if (StringUtils.startsWithIgnoreCase(nameToCompare,
									shortcut)) {
								isUnique = false;
								break;
							}
						}
					}
					if (isUnique) {
						firstWordToCommandMap.put(shortcut, command);
					} else {
						break;
					}
				}
			}
		}

		if (LOG.isInfoEnabled()) {
			LOG.info("Initialized Command Service " + commands.size()
					+ " commands " + firstWordToCommandMap.values().size()
					+ " mappings " + (System.currentTimeMillis() - startTime)
					+ "ms");
		}
		
		//debug();
		
	}
	
	
	
	public void processCommandAndCheckAliases(String command,SocketChannelUserSession userSession) {
		command = command.trim();
		
		//
		// CHECK FOR A GAME MOVE
		//
		
		if (command.equals("0-0")) {
			command = "O-O";
		} else if (command.equals("0-0-0")) {
			command = "O-O-O";
		}
		
		boolean isMove = Move.isValidSAN(command);
		// think there is a bug in the isValidSAN, where "+" is considered valid.
		if (isMove && command.startsWith("+")) {
			isMove = false;
		}
		if (isMove) {
			ProcessGameMoveHelper.processGameMove(userSession, command);
			return;
		}
		
		//
		// PROCESS EVERYTHING ELSE
		//
		
		String keyword = null;
		String content = null;
		
		int spaceIndex = command.indexOf(' ');
		if (spaceIndex == -1) {
			keyword = command.toLowerCase();
			content = "";
		} else {
			keyword = command.substring(0, spaceIndex).toLowerCase();
			content = command.substring(spaceIndex + 1);
		}
		
		//Matcher m = listAliasPattern.matcher(keyword);
		//if (m.matches()) {
		if (keyword.startsWith("+") || keyword.startsWith("-") || keyword.startsWith("=")) {
			String what = regexHelper(""+keyword.charAt(0));
			String whatlist = keyword.substring(1);
			if (whatlist.equals("") && what != null) {
				if (what.equals("addlist")) { userSession.send(new AddListCommand().getContext().getUsage()); return; }
				if (what.equals("removelist")) { userSession.send(new RemoveListCommand().getContext().getUsage()); return; }
				if (what.equals("showlist")) {
					// all lists are shown to the user
					//boolean isAdmin = UserService.getInstance().isAdmin(userSession.getUser().getUserName());
					new ShowListCommand().process("", userSession);
					return;
				}
			}
			
			// find possible matches
			String[] possible = completeListAlias(whatlist);
			if (possible.length > 1) {
				userSession.send("Ambiguous list - matches: " + MorphyStringUtils.toDelimitedString(possible,", ") + ".");
				return;
			} else if (possible.length == 0) {
				userSession.send("\"" + whatlist + "\" does not match any list name.");
				return;
			} else if (possible.length == 1) {
				whatlist = possible[0];
			}
			
			String s = what + " " + whatlist + " " + content;
			processCommand(s,userSession);
		} else if (keyword.equals("=") && content.equals("")) { 
			processCommand("showlist",userSession);
		} else if (keyword.equals(".")) {
			morphy.user.UserSession sess = userSession.getLastPersonToldTo();
			if (sess != null) {
				processCommand("tell " + sess.getUser().getUserName() + " " + content,userSession);
			} else { userSession.send("I don't know who to say that to."); return; }
		} else if (keyword.equals(",")) {
			morphy.channel.Channel c = userSession.getLastChannelToldTo();
			if (c != null) {
				processCommand("tell " + c.getNumber() + " " + content,userSession);
			} else { userSession.send("I don't know who to say that to."); return; }
		} else if (keyword.equals(":")) {
			// shout
		} else if (keyword.equals(";")) {
			// ptell to bughouse partner
		} else {
			processCommand(command,userSession);
		}
	}

	/**
	 * Assists processCommandAndCheckAliases method.
	 */
	private String regexHelper(String s) {
		if (s.equals("+"))
			return "addlist";
		if (s.equals("="))
			return "showlist";
		if (s.equals("-"))
			return "removelist";
		return null;
	}
	
	public void dispose() {
		commands.clear();
		firstWordToCommandMap.clear();

		if (LOG.isInfoEnabled()) {
			LOG.info("Command disposed.");
		}
	}

	public Command getCommand(String keyword) {
		keyword = keyword.toLowerCase();
		return firstWordToCommandMap.get(keyword);
	}

	public Command[] getCommands() {
		return commands.toArray(new Command[commands.size()]);
	}

	public void processCommand(String command,
			SocketChannelUserSession userSession) {
		command = command.trim();
		
		String keyword = null;
		String content = null;

		int spaceIndex = command.indexOf(' ');
		if (spaceIndex == -1) {
			keyword = command.toLowerCase();
			content = "";
		} else {
			keyword = command.substring(0, spaceIndex).toLowerCase();
			content = command.substring(spaceIndex + 1);
		}

		Command socketCommand = firstWordToCommandMap.get(keyword);

		if (socketCommand == null) {
			userSession.send(keyword + ": Command not found.");
		} else if (socketCommand.willProcess(userSession)) {
			if (keyword.equals("q") || keyword.equals("qu") || keyword.equals("qui")) {
				userSession.send("" + UserService.getInstance().getTags(userSession.getUser().getUserName()) + 
						" tells you: The command 'quit' cannot be abbreviated.");
				return;
			} else {
				socketCommand.process(content, userSession);
			}
		} else {
			userSession.send(keyword + ": Command not found.");
		}
	}
	
	/*public String showLists(boolean isAdmin) {
		ServerListManagerService s = ServerListManagerService.getInstance();
		List<ServerList> list = s.getLists();
		ServerList.setCompareBy(ServerList.CompareBy.Name);
		java.util.Collections.sort(list);
		StringBuilder b = new StringBuilder("Lists:\n\n");
		for(ServerList sl : list) {
			if (sl.isPublic()) {
				String listName = sl.getName().toLowerCase();
				if (listName.equals("admin")) continue;
				b.append(String.format("%-20s %s\n",listName,"is PUBLIC"));
			}
			// if they're not public, then they're administrative lists
		}
		
		PersonalList[] personalLists = PersonalList.sortByName();
		for(PersonalList pl : personalLists) {
			b.append(String.format("%-20s %s\n",pl.name(),"is PRIVATE"));
		}
		
		int len = b.length();
		b = b.deleteCharAt(len-1);
		return b.toString();
	}*/
	
	public void debug() {
		java.util.Set<String> keys = firstWordToCommandMap.keySet();
		for(String s : keys) {
			System.out.println(s);
		}
	}
}
