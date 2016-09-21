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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import morphy.Morphy;
import morphy.service.DBConnectionService;
import morphy.utils.john.DBConnection;

public class User {
	public static final int MAX_LIST_SIZE = 50;

	protected String userName;
	protected UserLevel userLevel;
	protected PlayerType playerType;
	protected UserVars userVars = new UserVars(this);
	protected Formula formula;
	protected boolean isRegistered;
	
	// the following variables are stored in the db and need to be filled in.
	protected String email;
	protected long registeredSince;
	protected int dbid;
	
	private Map<PersonalList,List<String>> personalLists;
	private Map<PersonalList,Integer> personalListDBIDs;
	
	public User() {
		setUserInfoLists(new HashMap<UserInfoList,List<String>>());
		for(UserInfoList u : UserInfoList.values()) {
			getUserInfoLists().put(u,new ArrayList<String>());
		}
		setLists(new HashMap<PersonalList,List<String>>());
		for(PersonalList u : PersonalList.values()) {
			getLists().put(u,new ArrayList<String>());
		}
	}
	
	private void loadFromDB() {
		DBConnection c = DBConnectionService.getInstance().getDBConnection();
		java.sql.ResultSet r = c.executeQueryWithRS("SELECT `email`,UNIX_TIMESTAMP(`registeredSince`) FROM `users` WHERE `username` = '" + getUserName() + "'");
		try {
			if (r.next()) {
				setEmail(r.getString(1));
				long millis = Long.parseLong(r.getString(2)+"000");
				setRegisteredSince(millis);
			}
		} catch(java.sql.SQLException e) {
			Morphy.getInstance().onError("Error reading user info from database in User.loadFromDB()",e);
		}
	}
	
	void setLists(Map<PersonalList, List<String>> lists) {
		this.personalLists = lists;
	}
	public Map<PersonalList, List<String>> getLists() {
		return personalLists;
	}

	private Map<UserInfoList,List<String>> userInfoLists;
	
	void setUserInfoLists(Map<UserInfoList,List<String>> userInfoLists) {
		this.userInfoLists = userInfoLists;
	}
	
	public Map<UserInfoList,List<String>> getUserInfoLists() {
		return userInfoLists;
	}

	/**
	 * Returns if userName is on list.
	 * @param list
	 * @param userName
	 * @return
	 */
	public boolean isOnList(PersonalList list,String userName) {
		List<String> myList = getLists().get(list);
		if (myList == null) return false;
		if (myList.contains(userName)) return true;
		return false;
	}


	public PlayerType getPlayerType() {
		return playerType;
	}

	public UserLevel getUserLevel() {
		return userLevel;
	}

	public String getUserName() {
		return userName;
	}

	public UserVars getUserVars() {
		return userVars;
	}

	public void setPlayerType(PlayerType playerType) {
		this.playerType = playerType;
	}

	public void setUserLevel(UserLevel userLevel) {
		this.userLevel = userLevel;
	}

	public void setUserName(String userName) {
		this.userName = userName;
		
		loadFromDB();
	}

	public void setUserVars(UserVars userVars) {
		this.userVars = userVars;
	}

	public boolean isRegistered() {
		return isRegistered;
	}

	public void setRegistered(boolean isRegistered) {
		this.isRegistered = isRegistered;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public long getRegisteredSince() {
		return registeredSince;
	}

	public void setRegisteredSince(long registeredSince) {
		this.registeredSince = registeredSince;
	}

	public Formula getFormula() {
		return formula;
	}

	public void setFormula(Formula formula) {
		this.formula = formula;
	}

	public int getDBID() {
		return dbid;
	}

	public void setDBID(int dbid) {
		this.dbid = dbid;
	}

	public void setPersonalListDBIDs(Map<PersonalList,Integer> personalListDBIDs) {
		this.personalListDBIDs = personalListDBIDs;
	}

	public Map<PersonalList,Integer> getPersonalListDBIDs() {
		return personalListDBIDs;
	}
}
