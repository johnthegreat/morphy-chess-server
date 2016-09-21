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
package morphy.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import morphy.properties.PreferenceKeys;
import morphy.utils.john.DBConnection;

public class DBConnectionService implements Service {
	protected static Log LOG = LogFactory.getLog(DBConnectionService.class);

	private static DBConnectionService service = new DBConnectionService();
	public static DBConnectionService getInstance() {
		return service;
	}
	
	private DBConnection Connection;
	
	public DBConnectionService() {
		if (LOG.isInfoEnabled()) {
			LOG.info("Initialized DBConnectionService.");
		}
		
		PreferenceService preferenceService = PreferenceService.getInstance();
		
		DBConnection c = new DBConnection(preferenceService.getString(PreferenceKeys.DatabaseHostAddress),preferenceService.getString(PreferenceKeys.DatabaseName),preferenceService.getString(PreferenceKeys.DatabaseUsername),preferenceService.getString(PreferenceKeys.DatabasePassword));
		Connection = c;
	}
	
	public DBConnection getDBConnection() {
		return Connection;
	}
	
	public void dispose() {
		//Connection.closeConnection();
		
		if (LOG.isInfoEnabled()) {
			LOG.info("DBConnectionService disposed.");
		}
	}

}
