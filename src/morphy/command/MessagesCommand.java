/*
 *   Morphy Open Source Chess Server
 *   Copyright (C) 2008-2011  http://code.google.com/p/morphy-chess-server/
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import morphy.service.DBConnectionService;
import morphy.user.UserSession;
import morphy.utils.john.TimeZoneUtils;

public class MessagesCommand extends AbstractCommand {

	public MessagesCommand() {
		super("messages");
	}
	
	public void process(String arguments, UserSession userSession) {		
		/*  Messages:
			1. ...
			2. ...
		 */
		
		/*  mess wmahan
			wmahan has no messages from you.
			
			Messages from wmahan:
			31. ...
		 */
		
		/*  MAd has no messages from you.
       		You have no messages from MAd.
        */
		
		/* 	mess 41
			You have no message 41.
		*/
		
		/*  -- FICS --
		    mess 5,7,10
			'5,7,10' is not a valid handle.
		 */
		
		String tzone = userSession.getUser().getUserVars().getVariables().get("tzone");
		/*tzone = TimeZoneUtils.getAbbreviation(TimeZoneUtils.getTimeZone(tzone));*/
		if (tzone.equals("SERVER")) tzone = TimeZoneUtils.getAbbreviation(TimeZone.getDefault(),new java.util.Date());
		
		arguments = arguments.trim();
		String query = "SELECT m.`id`,u.`username`,m.`message`,CONVERT_TZ(m.`timestamp`,'UTC','SYSTEM'),m.`read` " + 
		"FROM `messages` m INNER JOIN `users` u ON (u.`id` = m.`from_user_id`) " +
		"WHERE m.`to_user_id` = '" + userSession.getUser().getDBID() + "' " +
		"ORDER BY m.`id` ASC";
		
		if (arguments.equals("")) {
			// show all messages
			printMessages(query, userSession, tzone, null);
		} else {
			// Usage: messages [user_name [message_body]] | n[-m]]
		
			if (arguments.matches("\\w{3,17}")) {
				// show all messages from a username
				printMessages(query, userSession, tzone, getMessageIdsFromUsername(arguments, userSession.getUser().getDBID()));
			} else {
				// show messages with an optional range (eg: messages 5 | messages 5-10 | messages 5,7,9)
				// possible: add comma-separated message numbers list support
				// if we want to get a little advanced, we can mix all of the above, eg: messages 5-10,11,20
				// NOTE: The ideas described in the above three comments are now implemented in the core. 
				
				// another possible feature (that I will NOT build into the core at this point in time):
				// filtering with date ranges. for example: messages after "8/5/11" or messages before "12/31/11"
				
				try {
					List<Integer> list = new ArrayList<Integer>();
					String[] range = arguments.split(",");
					for(int i=0;i<range.length;i++) {
						// expand a range, eg 1-5 = [1,2,3,4,5]
						List<Integer> tmp = expandRange(range[i]);
						for(Integer myint : tmp) {
							// loop through the range and add to the list (ensuring no duplicates)
							if (!list.contains(myint)) list.add(myint);
						}
					}
					// sort the list
					java.util.Collections.sort(list);
					// temp debug: /* userSession.send(list.toString()); */ 
					printMessages(query, userSession, tzone, list);
				} catch(NumberFormatException e) {
					userSession.send("'" + arguments + "' is not a valid handle.");
					return;
				}
			}
		}
	}
	
	/**
	 * Executes a query and sends (in a single prompt) all
	 * messages with ids filtered by the <tt>filteredIds</tt> param.
	 * @param query
	 * @param userSession UserSession instance to send to
	 * @param filteredIds List of message ids we want to show. If empty or null, show all.
	 */
	private void printMessages(String query,UserSession userSession,String tzone,List<Integer> filteredIds) {
		int rownum = 1;
		StringBuilder b = new StringBuilder();
		
		boolean filterIds = filteredIds != null && filteredIds.size() > 0;
		// list of ids to mark as read in the database
		List<Integer> ids = new ArrayList<Integer>();
		try {
			ResultSet rs = DBConnectionService.getInstance().getDBConnection().executeQueryWithRS(query);
			int numMessages = 0;
			
			while(rs.next()) {
				if (filterIds && !filteredIds.contains(rownum)) { rownum++; continue; }
				boolean read = rs.getBoolean(5); if (!read) { ids.add(rs.getInt(1)); }
				b.append(formatMessage(rownum, rs.getString(2), rs.getTimestamp(4).getTime(), tzone, rs.getString(3), rs.getBoolean(5))+"\n");
				rownum++;
				numMessages++;
			}
			if (numMessages == 0) {
				userSession.send("You have no messages.");
				return;
			}
			userSession.send(b.toString());
			
			// mark these messages as read
			// only if they aren't already marked as read
			if (ids.size() != 0) { 
				DBConnectionService.getInstance().getDBConnection().executeQuery("UPDATE `messages` SET `read` = '1' WHERE " + formatIdListForQuery("id",ids)); 
			}
			return;
		} catch(SQLException e) {
			e.printStackTrace(System.err);
		}
	}
	
	/** Returns a list of message ids from <tt>username</tt> to the user with the specified id (<tt>myUserId</tt>). */
	private List<Integer> getMessageIdsFromUsername(String username,int myUserId) {
		String query = "SELECT u1.`username` FROM `messages` m INNER JOIN `users` u1 ON (u1.`id` = m.`from_user_id`) WHERE m.to_user_id = '" + myUserId + "'";
		ResultSet rs = DBConnectionService.getInstance().getDBConnection().executeQueryWithRS(query);
		
		// allocate variables
		List<Integer> ids = new ArrayList<Integer>();
		int rownum = 0;
		try {
			// loop through the result set
			while(rs.next()) {
				rownum++;
				if (username.equalsIgnoreCase(rs.getString(1))) ids.add(new Integer(rownum));
			}
		} catch(SQLException e) {
			e.printStackTrace(System.err);
		}
		return ids;
	}
	
	/** Returns part of a query in format <i>`<tt>columnName</tt>` IN (...)</i><br />
	 * Where the contents of <i>...</i> are filled in with param <tt>arr</tt>.<br />
	 * This will usually be appended to the end of a WHERE clause in a query.
	 * @param columnName Column name of the field in the database
	 * @param arr List of elements that <tt>columnName</tt> should be contained in.
	 * @return
	 * */
	protected static String formatIdListForQuery(String columnName,List<Integer> arr) {
		StringBuilder b = new StringBuilder();
		b.append("`" + columnName + "` IN (");
		for(int i=0;i<arr.size();i++) {
			b.append(arr.get(i) + "" + ((i != arr.size()-1)?",":""));
			//b.append("`" + columnName + "` = '" + arr.get(i) + "'");
			//if (i != arr.size()-1) b.append(" || ");
		}
		b.append(")");
		return b.toString();
	}
	
	protected static String formatIdListForQuery(String columnName,int[] arr) {
		StringBuilder b = new StringBuilder();
		b.append("`" + columnName + "` IN (");
		for(int i=0;i<arr.length;i++) {
			b.append(arr[i] + "" + (i!=arr.length-1?",":""));
		}
		b.append(")");
		return b.toString();
	}
	
	/** This is only called in the <tt>printMessages()</tt> method. */
	private String formatMessage(int messno,String username,long date,String tzone,String message,boolean read) {
		final java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEE MMM dd, HH:mm z yyyy");
		TimeZone tz = TimeZoneUtils.getTimeZone(tzone);
		sdf.setTimeZone(tz);
		// note that this deviates from FICS slightly be adding the (previously unread) text to the string.
		// this will problably be changed (removed) in the future.
		return String.format("%d. %s at %s" + (!read?" (previously unread)":"") + ": %s",messno,username,sdf.format(date),message);
	}
	
	/** Expands a range from format (for example) 1-3 to a List containing the elements [1,2,3]<br />
	 * If <tt>str</tt> is not a range in the above format and is a single number all by itself (eg 1),
	 * then a List object will still be returned, but will only contain this number.<br /><br />
	 * This is protected (and static) because it is used in the ClearmessagesCommand class as well.<br />
	 * Maybe this should be moved to some sort of utility class in the future. */
	protected static List<Integer> expandRange(String str) {
		List<Integer> list = new ArrayList<Integer>();
		
		if (!str.contains("-") && str.matches("[0-9]+")) {
			list.add(Integer.parseInt(str));
			return list;
		}
		
		if (str.matches("[0-9]+\\-[0-9]+")) {
			String[] arr = str.split("-");
			int from = Integer.parseInt(arr[0]);
			int to = Integer.parseInt(arr[1]);
			for(int i=from;i<=to;i++) {
				list.add(new Integer(i));
			}
		}
		return list;
	}
}
