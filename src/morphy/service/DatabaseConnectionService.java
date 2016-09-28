/*
 *   Morphy Open Source Chess Server
 *   Copyright (C) 2008-2011, 2016  http://code.google.com/p/morphy-chess-server/
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

import morphy.utils.john.DatabaseConnection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import morphy.Morphy;
import morphy.properties.MorphyPreferences;
import morphy.properties.PreferenceKeys;

public class DatabaseConnectionService implements Service {
	protected static final Log LOG = LogFactory.getLog(DatabaseConnectionService.class);

	private static DatabaseConnectionService service = new DatabaseConnectionService();
	public static DatabaseConnectionService getInstance() {
		return service;
	}
	
	private DatabaseConnection Connection;
	
	public DatabaseConnectionService() {
		if (LOG.isInfoEnabled()) {
			LOG.info("Initialized DatabaseConnectionService.");
		}
		
		MorphyPreferences morphyPreferences = Morphy.getInstance().getMorphyPreferences();
		
		DatabaseConnection c = new DatabaseConnection(
				morphyPreferences.getString(PreferenceKeys.DatabaseHostAddress),
				morphyPreferences.getString(PreferenceKeys.DatabaseName),
				morphyPreferences.getString(PreferenceKeys.DatabaseUsername),
				morphyPreferences.getString(PreferenceKeys.DatabasePassword));
		Connection = c;
	}
	
	public DatabaseConnection getDBConnection() {
		return Connection;
	}
	
	public void dispose() {
		//Connection.closeConnection();
		
		if (LOG.isInfoEnabled()) {
			LOG.info("DatabaseConnectionService disposed.");
		}
	}

}
