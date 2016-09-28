/*
 *   Morphy Open Source Chess Server
 *   Copyright (C) 2008-2010, 2016 http://code.google.com/p/morphy-chess-server/
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
package morphy;

import java.io.File;
import java.util.TimeZone;

import morphy.properties.MorphyPreferences;
import morphy.service.ChannelService;
import morphy.service.CommandService;
import morphy.service.DatabaseConnectionService;
import morphy.service.GameService;
import morphy.service.RequestService;
import morphy.service.ServerListManagerService;
import morphy.service.Service;
import morphy.service.SocketConnectionService;
import morphy.service.ThreadService;
import morphy.service.UserService;
import morphy.utils.MorphyFileProvider;
import morphy.utils.john.TimeZoneUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

public class Morphy {
	private MorphyPreferences morphyPreferences;
	private MorphyFileProvider morphyFileProvider;
	
	protected static Log LOG = LogFactory.getLog(Morphy.class);
	protected long upsince;
	
	static {
		String log4jConfigPath = "log4j.properties";
		// Forces log4j to check for changes to its properties file and reload
		// them every 5 seconds.
		// This must always be called before any other code or it will not work.
		PropertyConfigurator.configureAndWatch(log4jConfigPath, 5000);
		System.out.println("Configured: " + log4jConfigPath);
	}

	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Please specify path to configuration file (morphy.properties).");
			System.exit(1);
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				getInstance().shutdown();
			}
		});
		
		String filePath = args[0];
		File configFile = ensureConfigFileExistsAndReadable(filePath);
		getInstance().init(configFile);
	}
	
	private static File ensureConfigFileExistsAndReadable(String filePath) {
		File configFile = new File(filePath);
		if (!configFile.exists()) {
			System.err.println("Configuration file does not exist.");
			System.exit(1);
		}
		
		if (!configFile.canRead()) {
			System.err.println("Unable to read configuration file.");
			System.exit(1);
		}
		return configFile;
	}

	protected Service[] services;

	private static Morphy singletonInstance = new Morphy();

	public static Morphy getInstance() {
		return singletonInstance;
	}

	private boolean isShutdown = false;

	private Morphy() {
	}

	public boolean isShutdown() {
		return isShutdown;
	}

	public void onError(String message) {
		LOG.error(message);
	}

	public void onError(String message, Throwable t) {
		LOG.error(message, t);
	}

	public void onError(Throwable t) {
		LOG.error("", t);
	}

	public void shutdown() {
		if (!isShutdown) {
			LOG.info("Initiating shutdown.");
			isShutdown = true;
			if (services != null) {
				for (int i = 0; i < services.length; i++) {
					try {
						services[i].dispose();
					} catch (Throwable t) {
						LOG.error("Error shutting down service", t);
					}
				}
			}
			LOG.info("Shut down Morphy.");
			System.exit(0);
		}
	}
	
	protected MorphyPreferences loadMorphyConfiguration(File configFile) throws Exception {
		MorphyPreferences morphyPreferences = new MorphyPreferences(configFile);
		return morphyPreferences;
	}
	
	protected MorphyFileProvider loadMorphyFileProvider(MorphyPreferences morphyPreferences) throws Exception {
		MorphyFileProvider morphyFileProvider = new MorphyFileProvider();
		morphyFileProvider.configure(morphyPreferences);
		return morphyFileProvider;
	}

	private void init(File configFile) {
		if (LOG.isInfoEnabled()) {
			LOG.info("Initializing Morphy");
		}
		
		upsince = System.currentTimeMillis();
		
		try {
			System.out.println(String.format("Attempting to load configuration from path: %s", configFile.getAbsolutePath()));

			this.morphyPreferences = this.loadMorphyConfiguration(configFile);
			this.morphyFileProvider = this.loadMorphyFileProvider(this.morphyPreferences);
		} catch (Exception e) {
			if (LOG.isErrorEnabled()) {
				LOG.error("Unable to load configuration due to exception.",e);
			}
			shutdown();
		}
		
		Thread t = new Thread() {
			public void run() {
				if (LOG.isInfoEnabled()) {
					LOG.info("Server located in timezone " + TimeZoneUtils.getAbbreviation(TimeZone.getDefault()));
				}
			}
		};
		t.start();
		
		services = new Service[] { DatabaseConnectionService.getInstance(),
				ThreadService.getInstance(), CommandService.getInstance(),
				SocketConnectionService.getInstance(),
				ChannelService.getInstance(), UserService.getInstance(),
				ServerListManagerService.getInstance(),GameService.getInstance(),RequestService.getInstance() };
	}

	public long getUpSinceTime() {
		return upsince;
	}
	
	public MorphyPreferences getMorphyPreferences() {
		return morphyPreferences;
	}
	
	public MorphyFileProvider getMorphyFileProvider() {
		return morphyFileProvider;
	}
}
