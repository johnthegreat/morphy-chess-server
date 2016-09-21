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
package morphy.game;

import board.Board;

public interface GameInterface extends Comparable<GameInterface> {
	public Board getBoard();
	public int getGameNumber();
	public int getTime();
	public int getIncrement();
	public int getWhiteBoardStrength();
	public int getBlackBoardStrength();
	public int getWhiteClock();
	public int getBlackClock();
	public boolean isRated();
	public long getTimeGameStarted();
	public Variant getVariant();
	
	public void setGameNumber(int num);
	public void setTime(int time);
	public void setIncrement(int increment);
	public void setRated(boolean rated);
	
	public void processMoveUpdate(boolean all);
	public String processMoveUpdate(morphy.user.UserSession userSession);
}
