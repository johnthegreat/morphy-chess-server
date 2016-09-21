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
package morphy.user;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;

import morphy.Morphy;
import morphy.game.style.Style12;
import morphy.game.style.StyleInterface;
import morphy.service.DBConnectionService;
import morphy.utils.john.DBConnection;

public class UserVars {
	private User user;
	private StyleInterface style;
	private HashMap<String,String> variables = new HashMap<String,String>();
	private HashMap<String,String> ivariables = new HashMap<String,String>();
	
	public UserVars(User user) {
		if (user.getUserName() == null) return;
		
		this.user = user;
		
		variables.put("busy","");
		if (!user.isRegistered()) {
			initialize();
			return;
		}
		
		loadFromDB();
		variables.put("showadmintag","1");
		variables.put("showsrtag","1");

		// if id is set, obviously a record in the db exists.
//		if (variables.get("id") == null) {
//			initialize();
//			if (user.isRegistered())
//				dumpToDB();
//		}
	}
	
	private void initialize() {
		// set defaults
		setStyle(Style12.getSingletonInstance());
		
		variables.put("time","2");
		variables.put("inc","12");
		variables.put("rated","1");
		variables.put("open","1");
		variables.put("bugopen","0");
		variables.put("tourney","0");
		variables.put("provshow","0");
		variables.put("autoflag","0");
		variables.put("minmovetime","1");
		variables.put("private","0");
		variables.put("jprivate","0");
		variables.put("automail","0");
		variables.put("pgn","0");
		variables.put("mailmess","0");
		variables.put("messreply","0");
		variables.put("unobserve","1");
		variables.put("shout","0");
		variables.put("cshout","0");
		variables.put("kibitz","1");
		variables.put("kiblevel","0");
		variables.put("tell","1");
		variables.put("ctell","1");
		variables.put("chanoff","0");
		variables.put("silence","0");
		variables.put("echo","0");
		variables.put("tolerance","1");
		variables.put("pin","0");
		variables.put("notifiedby","0");
		variables.put("availinfo","0");
		variables.put("availmin","0");
		variables.put("availmax","0");
		variables.put("gin","0");
		variables.put("seek","0");
		variables.put("showownseek","0");
		variables.put("examine","0");
		variables.put("noescape","0");
		variables.put("style","12");
		variables.put("flip","0");
		variables.put("highlight","0");
		variables.put("bell","0");
		variables.put("width","79");
		variables.put("height","24");
		variables.put("ptime","0");
		variables.put("tzone","SERVER");
		variables.put("lang","English");
		variables.put("notakeback","0");
		variables.put("prompt","fics%");
		variables.put("interface","NULL");
	
		ivariables.put("ms","1");
		ivariables.put("gameinfo","1");
		ivariables.put("lock","0");
	}
	
	public User getUser() {
		return user;
	}
	
	public void loadFromDB() {
		DBConnection conn = DBConnectionService.getInstance().getDBConnection();
		ResultSet r = conn.executeQueryWithRS("SELECT * FROM `user_vars` WHERE `user_id` = (SELECT `id` FROM `users` WHERE `username` = '" + getUser().getUserName() + "')");
		try {
			ResultSetMetaData meta = r.getMetaData();
			int count = meta.getColumnCount();
			if (r.next()) {
				for(int i=0;i<count;i++) {
					String variable = meta.getColumnName(i+1);
					String value = r.getString(i+1);
					variables.put(variable,value);
				}
			}
			
		} catch(java.sql.SQLException e) {
			Morphy.getInstance().onError("SQLException thrown in class UserVars method loadFromDB();",e);
		}
	}
	
	public void update(String variable,String value) {
		variables.put(variable,value);
		
		// variables are now only committed to database on logout.
		/*String query = "UPDATE `user_vars` SET `" + variable + "` = '" + value + "' WHERE `user_id` = (SELECT `id` FROM `users` WHERE `username` = '" + getUser().getUserName() + "')";
		DBConnection conn = new DBConnection();
		conn.executeQuery(query);
		conn.closeConnection();*/
	}
	
	public HashMap<String,String> getVariables() {
		return variables;
	}
	
	public HashMap<String,String> getIVariables() {
		return ivariables;
	}
	
	public void dumpToDB() {
		if (getUser() == null || !getUser().isRegistered()) return;
		if (getUser().getUserLevel() == UserLevel.Guest) return;
		
		HashMap<String,String> variables = getVariables();
		String[] keys = variables.keySet().toArray(new String[0]);
		String[] values = variables.values().toArray(new String[0]);
		
		String username = getUser().getUserName();
		Object query = null;
		DBConnection conn = DBConnectionService.getInstance().getDBConnection();
		ResultSet rs = conn.executeQueryWithRS("SELECT `id` FROM `users` WHERE `username` = '" + username + "'");
		int userid = 0;
		try { 
			if (rs.next()) {
				userid = rs.getInt(1);
			}
		} catch(java.sql.SQLException e) { Morphy.getInstance().onError(e); }
		StringBuilder insertcols = new StringBuilder(200);
		StringBuilder insertvals = new StringBuilder(200);
		for(int i=0;i<keys.length;i++) {
			if (keys[i].equals("busy") || 
					keys[i].equals("showadmintag") || 
					keys[i].equals("showsrtag") || 
					keys[i].equals("user_id") || 
					keys[i].equals("id")) continue;
			insertcols.append("`" + keys[i] + "`");
			if (i != keys.length-1) insertcols.append(",");
			insertvals.append("'" + values[i] + "'");
			if (i != keys.length-1) insertvals.append(",");
		}
		
		StringBuilder update = new StringBuilder(500);
		for(int i=0;i<keys.length;i++) {
			if (keys[i].equals("busy") || 
					keys[i].equals("showadmintag") || 
					keys[i].equals("showsrtag") || 
					keys[i].equals("user_id") || 
					keys[i].equals("id")) continue;
			update.append("`" + keys[i] + "` = '" + values[i] + "'");
			if (i != keys.length-1) update.append(",");
		}
		
		query = "INSERT INTO `user_vars` (`id`,`user_id`," + insertcols.toString() + ") VALUES " +
				"(NULL," + userid + "," + insertvals.toString() + ") ON DUPLICATE KEY UPDATE " + update.toString();

		conn.executeQuery(query.toString());
		
//		query = new StringBuilder("UPDATE `user_vars` SET ");
//		
//		for(int i=0;i<keys.length;i++) {
//			((StringBuilder)query).append("`" + keys[i] + "` = '" + values[i] + "'");
//			if (i != keys.length-1) ((StringBuilder)query).append(", ");
//		}
//		((StringBuilder)query).append(" WHERE user_id = (SELECT `id` FROM `users` WHERE `username` = '" + username + "')");
//
//		conn.executeQuery(query.toString());
			
	}

	public void setStyle(StyleInterface style) {
		this.style = style;
	}

	public StyleInterface getStyle() {
		return style;
	}
}
