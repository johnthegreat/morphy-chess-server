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
package morphy;

import java.io.File;
import java.util.TimeZone;

import morphy.service.ChannelService;
import morphy.service.CommandService;
import morphy.service.DBConnectionService;
import morphy.service.GameService;
import morphy.service.PreferenceService;
import morphy.service.RequestService;
import morphy.service.ServerListManagerService;
import morphy.service.Service;
import morphy.service.SocketConnectionService;
import morphy.service.ThreadService;
import morphy.service.UserService;
import morphy.utils.john.TimeZoneUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

public class Morphy {
	public static final String RESOURCES_DIR = "resources";
	public static final String COMMAND_FILES_DIR = "commandFiles";
	public static final String SCREEN_FILES = "screenFiles";
	public static final String USER_DIRECTORY = new File(System
			.getProperty("user.home")).getAbsolutePath()
			+ "/" + ".morphy";

	static {
		// Forces log4j to check for changes to its properties file and reload
		// them every 5 seconds.
		// This must always be called before any other code or it will not work.
		PropertyConfigurator.configureAndWatch(RESOURCES_DIR
				+ "/log4j.properties", 5000);
		System.err
				.println("Configured: " + RESOURCES_DIR + "/log4j.properties");
	}

	protected static Log LOG = LogFactory.getLog(Morphy.class);
	protected long upsince;

	public static void main(String[] args) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				getInstance().shutdown();
			}
		});

		getInstance().init();
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
			for (int i = 0; i < services.length; i++) {
				try {
					services[i].dispose();
				} catch (Throwable t) {
					LOG.error("Error shutting down service", t);
				}
			}
			LOG.info("Shut down Morphy.");
			System.exit(0);
		}
	}

	private void init() {
		if (LOG.isInfoEnabled()) {
			LOG.info("Initializing Morphy");
		}
		
		System.out.println(System.getProperties());
		
		upsince = System.currentTimeMillis();
		
		Thread t = new Thread() {
			public void run() {
				if (LOG.isInfoEnabled()) {
					LOG.info("Server located in timezone " + TimeZoneUtils.getAbbreviation(TimeZone.getDefault()));
				}	
			}
		};
		t.start();
		
		services = new Service[] { DBConnectionService.getInstance(),PreferenceService.getInstance(),
				ThreadService.getInstance(), CommandService.getInstance(),
				SocketConnectionService.getInstance(),
				ChannelService.getInstance(), UserService.getInstance(),
				ServerListManagerService.getInstance(),GameService.getInstance(),RequestService.getInstance() };
	}

	public long getUpSinceTime() {
		return upsince;
	}
}
