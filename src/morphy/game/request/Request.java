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
package morphy.game.request;

import morphy.user.UserSession;

public interface Request {
	public void acceptAction();
	public void declineAction();
	public void setRequestNumber(int i);
	public int getRequestNumber();
	public UserSession getFrom();
	public UserSession getTo();
	public String getRequestIdentifier();
	public void setExtraInfo(String extraInfo);
	public String getExtraInfo();
}
