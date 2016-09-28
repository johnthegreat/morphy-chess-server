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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import morphy.Morphy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DatabaseConnection {
	protected static final Log LOG = LogFactory.getLog(DatabaseConnection.class);
	
	private enum DBType { MySQL,Derby; }
	
	private DBType type;
	private Connection c;
	
	public DatabaseConnection() {
		this("localhost","morphyics","root","abcdef");
	}
	
	public DatabaseConnection(String address, String db, String username, String password) {
		try {
			DBType type = DBType.MySQL;
			this.type = type;
			
			String driver = "";
			if (type == DBType.MySQL) driver = "com.mysql.jdbc.Driver";
			if (type == DBType.Derby) driver = "org.apache.derby.jdbc.EmbeddedDriver";
			Class.forName(driver).newInstance();
			
			String connectionString = "";
			if (type == DBType.MySQL) connectionString = "jdbc:mysql://" + address + "/" + db + "?user=" + username + "&password=" + password + "";
			if (type == DBType.Derby) connectionString = "jdbc:derby:MorphyICSDB;";
			Connection conn = DriverManager.getConnection(connectionString);

			this.c = conn;
		} catch(Exception e) {
			if (LOG.isErrorEnabled()) {
				LOG.error(e);
			}
		}
	}
	
	/**
	 * 
	 * @return
	 * @throws SQLException 
	 */
	public Statement getStatement() throws SQLException {
		return c.createStatement();
	}
	
	public Connection getConnection() {
		return c;
	}
	
	public java.sql.ResultSet executeQueryWithRS(String query) {
		try {
			if (LOG.isInfoEnabled()) {
				LOG.info("Executed query: " + query);
			}
			Statement s = getConnection().createStatement();
			s.execute(query);
			return s.getResultSet();
		} catch(SQLException se) {
			Morphy.getInstance().onError(se);
			return null;
		}
	}
	
	/**
	 * Shorthand for getConnection().getStatement().execute(query).
	 * You can NOT retrieve ResultSets using this method, so they can only be INSERTs, etc.
	 */
	public boolean executeQuery(String query) {
		try {
			if (LOG.isInfoEnabled()) {
				LOG.info("Executed query: " + query);
			}
			getConnection().createStatement().execute(query);
			return true;
		} catch(SQLException se) {
			if (LOG.isErrorEnabled()) {
				LOG.error(se);
				se.printStackTrace(System.err);
			}
			return false;
		}
	}
	
	public String[] getArray(java.sql.ResultSet r,int columnIndex) {
		try {
			if (columnIndex == 0) columnIndex = 1;
			
			java.util.List<String> arr = new java.util.ArrayList<String>();
			while(r.next()) {
				arr.add(r.getString(columnIndex));
			}
			return arr.toArray(new String[arr.size()]);
		} catch(SQLException e) {
			e.printStackTrace(System.err);
		}
		return null;
	}
	
	public void closeConnection() {
		if (LOG.isInfoEnabled()) {
			LOG.info("closeConnection() called");
		}
		
		try { 
			if (type == DBType.Derby)
				DriverManager.getConnection("jdbc:derby:;shutdown=true");
		} catch(Exception e) {
			// shutting down derby always throws an exception, even if sucessful.
			if (LOG.isInfoEnabled()) {
				LOG.info("Derby engine shutdown successful.");
			}
		}
		
		try {
			if (type == DBType.MySQL) {
				c.close();
			}
		} catch(SQLException e) {
			if (LOG.isErrorEnabled()) {
				LOG.error(e);
			}
		}
	}
}
