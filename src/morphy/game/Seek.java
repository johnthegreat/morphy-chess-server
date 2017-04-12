/*
 *   Morphy Open Source Chess Server
 *   Copyright (C) 2016-2017  http://code.google.com/p/morphy-chess-server/
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

import morphy.game.params.SeekParams;
import morphy.user.UserSession;

/**
 * Created by John on 09/23/2016.
 */
public class Seek {
	public static class SeekRatingRange {
		private int fromRating;
		private int toRating;
		
		//
		// CONSTRUCTORS
		//
		
		public SeekRatingRange(int fromRating,int toRating) {
			this.fromRating = fromRating;
			this.toRating = toRating;
		}
		
		//
		// PUBLIC INTERFACE METHODS
		//
		
		// this method is a good candidate for a unit test.
		public boolean doesRatingMatch(int ratingToTest) {
			return this.fromRating > ratingToTest && ratingToTest < this.toRating;
		}
		
		//
		// GETTERS / SETTERS
		//
		
		public int getFromRating() {
			return fromRating;
		}
		
		public int getToRating() {
			return toRating;
		}
		
		public void setFromRating(int fromRating) {
			this.fromRating = fromRating;
		}
		
		public void setToRating(int toRating) {
			this.toRating = toRating;
		}
	}
	
	private int seekIndex;
	
	private UserSession userSession;
	
//	private int seekTimeMinutes;
//	private int seekIncSeconds;
//	private boolean rated;
//	private Variant variant;
	
	private SeekParams seekParams;
	private boolean useManual;
	private boolean useFormula;
	private SeekRatingRange ratingRange;
	
	public Seek() {
		seekParams = new SeekParams();
	}
	
	
	//
	// SETTERS
	//

	public void setUserSession(UserSession userSession) {
		this.userSession = userSession;
	}
	
	public void setSeekIndex(int seekIndex) {
		this.seekIndex = seekIndex;
	}
	
	public void setUseManual(boolean useManual) {
		this.useManual = useManual;
	}

	public void setUseFormula(boolean useFormula) {
		this.useFormula = useFormula;
	}
	
	public void setRatingRange(SeekRatingRange ratingRange) {
		this.ratingRange = ratingRange;
	}
	
	//
	// GETTERS
	//
	
	public UserSession getUserSession() {
		return userSession;
	}
	
	public int getSeekIndex() {
		return seekIndex;
	}
	
	public boolean isUseManual() {
		return useManual;
	}

	public boolean isUseFormula() {
		return useFormula;
	}
	
	public SeekParams getSeekParams() {
		return seekParams;
	}
	
	public SeekRatingRange getRatingRange() {
		return ratingRange;
	}
}
