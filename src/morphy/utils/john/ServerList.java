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
package morphy.utils.john;

import morphy.user.UserLevel;

public class ServerList implements Comparable<ServerList> {
	public static enum CompareBy { Name,Public };
	private static CompareBy compareBy = CompareBy.Name;
	public static void setCompareBy(CompareBy col) { compareBy = col; }
	
	public enum ListType {
		IPAddress,Username,Integer,String;
	}
	
	private String name;
	private UserLevel permissions;
	private String tag;
	private ListType type;
	private boolean isPublic;
	private boolean isConsideredStaff;

	/**
	 * Creates a new ServerList object.
	 * @param name Name of the list
	 * @param permissions Permissions required to modify the list
	 * @param listType
	 * @param tag Tag to be given to the player to show list status, eg (SR)
	 * @param isPublic Whether this list is open to the public for viewing.
	 */
	public ServerList(String name, UserLevel permissions, ListType listType,
			String tag,boolean isPublic) {
		setName(name);
		setPermissions(permissions);
		setType(listType);
		setTag(tag);
		setPublic(isPublic);
	}
	
	public ServerList(String name, UserLevel permissions, ListType listType,
			String tag,boolean isPublic,boolean isConsideredStaff) {
		this(name,permissions,listType,tag,isPublic);
		setConsideredStaff(false);
	}

	protected void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	protected void setPermissions(UserLevel permissions) {
		this.permissions = permissions;
	}

	public UserLevel getPermissions() {
		return permissions;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getTag() {
		return tag;
	}

	protected void setType(ListType type) {
		this.type = type;
	}

	public ListType getType() {
		return type;
	}

	protected void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}

	public boolean isPublic() {
		return isPublic;
	}
	
	public void setConsideredStaff(boolean isConsideredStaff) {
		this.isConsideredStaff = isConsideredStaff;
	}

	public boolean isConsideredStaff() {
		return isConsideredStaff;
	}

	/** If compareBy = Public, Public lists will come before Private lists.<br />
	 * If compareBy = Name, Lists will be sorted in alphabetical order. */
	public int compareTo(ServerList o) {
		if (compareBy == CompareBy.Name) {
			return name.compareTo(o.name);
		} else {
			return new Boolean(o.isPublic).compareTo(isPublic);
		}
	}
}