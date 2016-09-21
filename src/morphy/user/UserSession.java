/*
 *   Morphy Open Source Chess Server
 *   Copyright (C) 2008,2009  http://code.google.com/p/morphy-chess-server/
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

public interface UserSession {
	public void disconnect();

	public Object get(UserSessionKey key);

	public Boolean getBoolean(UserSessionKey key);

	public long getIdleTimeMillis();

	public Integer getInt(UserSessionKey key);

	public long getLoginTime();

	public String getString(UserSessionKey key);

	public User getUser();

	public boolean isConnected();

	public void put(UserSessionKey key, Object object);

	public void send(String message);
}
