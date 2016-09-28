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
package morphy.service;

import java.io.File;
import java.util.TreeMap;

import morphy.Morphy;
import morphy.utils.FileUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ScreenService {
	public static enum Screen {
		Login, Logout, SuccessfulLogin
	}

	protected static Log LOG = LogFactory.getLog(ScreenService.class);

	private static final ScreenService singletonInstance = new ScreenService();;

	public static ScreenService getInstance() {
		return singletonInstance;
	}

	TreeMap<Screen, String> screenToMessage = new TreeMap<Screen, String>();

	private ScreenService() {
		File screenFilesDirectory = Morphy.getInstance().getMorphyFileProvider().getScreenFilesDirectory();
		
		screenToMessage.put(Screen.Login, FileUtils.fileAsString(screenFilesDirectory.getAbsolutePath() + "/login.txt").replaceAll("\\n","\r\n"));
		screenToMessage.put(Screen.Logout, FileUtils.fileAsString(screenFilesDirectory.getAbsolutePath() + "/logout.txt"));
		screenToMessage.put(Screen.SuccessfulLogin, FileUtils.fileAsString(screenFilesDirectory.getAbsolutePath() +  "/successfulLogin.txt"));
	}

	public void dispose() {
		screenToMessage.clear();
	}

	public String getScreen(Screen screen) {
		return screenToMessage.get(screen);
	}
}
