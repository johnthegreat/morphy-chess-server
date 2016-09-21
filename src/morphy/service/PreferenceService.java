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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

import morphy.Morphy;
import morphy.properties.PreferenceKeys;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Preference service is responsible for storing morphy preferences.
 * 
 * @author kpatil
 * 
 */
public class PreferenceService extends PropertiesConfiguration implements
		Service {

	private static final Log LOG = LogFactory.getLog(GameService.class);

	/**
	 * default preferences location , if user preference is not available then
	 * preferences will be loaded from here
	 */
	public static final String DEFAULT_PROPERTIES_FILE = "morphy.properties";

	/**
	 * location where preferences will be stored <BR>
	 * Location : <code>${user.home}/.morphy/morphy.properties</code>
	 */
	public static final String USER_PROPERTIES_FILE = Morphy.USER_DIRECTORY
			+ "/morphy.properties";

	/**
	 * Singleton instance of service.
	 */
	private static final PreferenceService singletonInstance = new PreferenceService();

	/**
	 * Factory method to get PreferenceService instance.
	 * 
	 * @return PreferenceService
	 */
	public static PreferenceService getInstance() {
		return singletonInstance;
	}

	/**
	 * Instantiates PreferenceService by loading preferences from properties
	 * file.
	 */
	private PreferenceService() {
		super();
		setAutoSave(false);
		InputStream preferencesInputStream = null;
		try {
			// try to load user preferences , if exists
			final File userPrefedences = new File(USER_PROPERTIES_FILE);
			if (userPrefedences.exists()) {
				LOG.info("Trying to load preferences from file : "
						+ USER_PROPERTIES_FILE);
				preferencesInputStream = new FileInputStream(userPrefedences);
			} else {
				// load default properties
				LOG
						.info("Trying to load preferences from classpath resource : "
								+ DEFAULT_PROPERTIES_FILE);
				preferencesInputStream = Thread.currentThread()
						.getContextClassLoader().getResourceAsStream(
								DEFAULT_PROPERTIES_FILE);
				if (preferencesInputStream == null) {
					preferencesInputStream = Thread.currentThread()
							.getContextClassLoader().getResourceAsStream(
									"/" + DEFAULT_PROPERTIES_FILE);
				}
			}

			// assuming input stream will not be null after all above steps
			load(preferencesInputStream);
		} catch (FileNotFoundException fileNotFoundException) {
			Morphy.getInstance().onError(
					"Error loading properties file: " + USER_PROPERTIES_FILE,
					fileNotFoundException);
			loadDefaults();
		} catch (ConfigurationException configurationException) {
			Morphy.getInstance()
					.onError(
							"Error loading properties file: "
									+ DEFAULT_PROPERTIES_FILE,
							configurationException);
			loadDefaults();
		} finally {
			if (preferencesInputStream != null) {
				try {
					preferencesInputStream.close();
				} catch (Throwable t) {
					// TODO : exception handling missing
				}
			}
		}
	}

	/**
	 * dispose service by saving prefernces to file.
	 */
	public void dispose() {
		save();
	}

	/**
	 * fetches preference value as int
	 * 
	 * @param key
	 *            , preference name
	 * @return int , value as int
	 */
	public int getInt(PreferenceKeys key) {
		return getInt(key.toString());
	}

	/**
	 * fetches preference value as String
	 * 
	 * @param key
	 *            , prefernce name
	 * @return int , value as int
	 */
	public String getString(PreferenceKeys key) {
		return getString(key.toString());
	}

	/**
	 * Saves preferences to local file
	 */
	@Override
	public void save() {
		FileOutputStream fileOut = null;
		try {
			save(fileOut = new FileOutputStream(USER_PROPERTIES_FILE));
		} catch (Throwable t) {
			Morphy.getInstance().onError(
					"Error saving properties file: " + USER_PROPERTIES_FILE, t);
		} finally {
			if (fileOut != null) {
				try {
					fileOut.close();
				} catch (Throwable t) {
				}
			}
		}
	}

	/**
	 * Sets preference.
	 * 
	 * @param key
	 *            , property name
	 * @param value
	 *            , property value
	 */
	public void setProperty(PreferenceKeys key, Object value) {
		super.setProperty(key.toString(), value);
	}

	/**
	 * loads default configuration
	 */
	protected void loadDefaults() {
		setProperty(PreferenceKeys.SocketConnectionServicePorts, 5000); // "23,5000"
		setProperty(PreferenceKeys.SocketConnectionServiceHost, "127.0.0.1");
		setProperty(PreferenceKeys.SocketConnectionServiceCharEncoding, "UTF-8");
		setProperty(
				PreferenceKeys.SocketConnectionServiceMaxCommunicationBytes,
				400 * 4);
		setProperty(PreferenceKeys.SocketConnectionLineDelimiter, "\n\r");

		setProperty(PreferenceKeys.ThreadServiceCoreThreads, 100);
		setProperty(PreferenceKeys.ThreadServiceMaxThreads, 1000);
		setProperty(PreferenceKeys.ThreadServiceKeepAlive, 120);

		setProperty(PreferenceKeys.ValidUserNameRegEx, "\\w{3,17}");
		
		setProperty(PreferenceKeys.DatabaseHostAddress, "127.0.0.1");
		setProperty(PreferenceKeys.DatabaseName, "morphyics");
		setProperty(PreferenceKeys.DatabaseUsername, "username");
		setProperty(PreferenceKeys.DatabasePassword, "password");
	}
}
