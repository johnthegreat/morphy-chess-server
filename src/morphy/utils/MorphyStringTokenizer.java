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
package morphy.utils;

/**
 * It works just like java.util.StringTokenizer with some differences. You can
 * specify if blocks of delimiters are eaten. For instance:
 * "test1 test2 test3  test4" will return: "test1" "test2" "test3" "" "test4" if
 * not eating blocks of delimiters and it will return "test1" "test2" "test3"
 * "test4" if isEatingBlocksOfDelimiters.
 * 
 * You can also change the delimiters between calls by invoking
 * changeDelimiters.
 * 
 * You can also obtain what is left to tokenize by calling getWhatsLeft() You
 * can also obtain the number of current index its on in the string passed into
 * the constructor with getCurrentCharIndex.
 */
public class MorphyStringTokenizer {

	private int currentIndex = 0;

	private String delimiters;

	private boolean isEatingBlocksOfDelimiters = false;

	private String source;

	public MorphyStringTokenizer(String string, String delimiters) {
		source = string;
		this.delimiters = delimiters;
	}

	public MorphyStringTokenizer(String string, String delimiters,
			boolean isEatingBlocksOfDelimiters) {
		source = string;
		this.delimiters = delimiters;
		this.isEatingBlocksOfDelimiters = isEatingBlocksOfDelimiters;
	}

	public void changeDelimiters(String newDelimiters) {
		synchronized (this) {
			delimiters = newDelimiters;
		}
	}

	public int getCurrentIndex() {
		return currentIndex;
	}

	public String getWhatsLeft() {
		if (isEmpty()) {
			return "";
		} else {
			return source.substring(currentIndex);
		}
	}

	public boolean hasMoreTokens() {
		synchronized (this) {
			if (isEmpty()) {
				return false;
			} else {
				if (isEatingBlocksOfDelimiters()) {
					trimStartingDelimiters();
				}
				return !isEmpty();
			}
		}
	}

	public int indexInWhatsLeft(char token) {
		return source.indexOf(token, currentIndex);
	}

	public boolean isEatingBlocksOfDelimiters() {
		return isEatingBlocksOfDelimiters;
	}

	/**
	 * Returns null if there is nothing left.
	 */
	public String nextToken() {
		String result = null;
		synchronized (this) {
			if (isEmpty()) {
				return null;
			} else {
				if (isEatingBlocksOfDelimiters()) {
					trimStartingDelimiters();
				}

				int nearestDelimeter = -1;
				for (int i = 0; i < delimiters.length(); i++) {
					int delimiter = source.indexOf(delimiters.charAt(i),
							currentIndex);
					if (nearestDelimeter == -1 || delimiter != -1
							&& delimiter < nearestDelimeter) {
						nearestDelimeter = delimiter;
					}
				}

				if (nearestDelimeter == -1) {
					result = source.substring(currentIndex);
					currentIndex = source.length();
				} else {
					result = source.substring(currentIndex, nearestDelimeter);
					currentIndex = nearestDelimeter + 1;
					if (isEatingBlocksOfDelimiters()) {
						// Now trim all the delimiters that are at the begining
						// of
						// source.
						trimStartingDelimiters();
					}
				}
			}
		}
		return result;
	}

	/**
	 * Returns null if there is nothing left.
	 */
	public String peek() {
		String result = null;
		if (isEmpty()) {
			return null;
		} else {
			int cachedCurrentIndex = currentIndex;
			if (isEatingBlocksOfDelimiters()) {
				trimStartingDelimiters();
			}

			int nearestDelimeter = -1;
			for (int i = 0; i < delimiters.length(); i++) {
				int delimiter = source.indexOf(delimiters.charAt(i),
						currentIndex);
				if (nearestDelimeter == -1 || delimiter != -1
						&& delimiter < nearestDelimeter) {
					nearestDelimeter = delimiter;
				}
			}

			if (nearestDelimeter == -1) {
				result = source.substring(currentIndex);
			} else {
				result = source.substring(currentIndex, nearestDelimeter);
			}

			currentIndex = cachedCurrentIndex;
		}
		return result;

	}

	public void setCurrentIndex(int currentIndex) {
		this.currentIndex = currentIndex;
	}

	public void setEatingBlocksOfDelimiters(boolean isEatingBlocksOfDelimiters) {
		this.isEatingBlocksOfDelimiters = isEatingBlocksOfDelimiters;
	}

	public String substringSource(int start, int end) {
		return source.substring(start, end);
	}

	private boolean isEmpty() {
		return currentIndex >= source.length();
	}

	private void trimStartingDelimiters() {
		while (!isEmpty()
				&& delimiters.indexOf(source.charAt(currentIndex)) != -1) {
			currentIndex++;
		}
	}

}
