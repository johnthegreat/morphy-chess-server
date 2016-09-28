package morphy.utils;

import morphy.properties.MorphyPreferences;
import morphy.properties.PreferenceKeys;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by John on 09/27/2016.
 */
public class MorphyFileProvider {
	protected static final Log LOG = LogFactory.getLog(MorphyFileProvider.class);
	
	private File MORPHY_CONFIG_FILE = null;
	private File USER_DIRECTORY = null;
	private File RESOURCES_DIR = null;
	private File COMMAND_FILES_DIR = null;
	private File SCREEN_FILES_DIR = null;
	
	public void configure(MorphyPreferences morphyPreferences) throws Exception {
		MORPHY_CONFIG_FILE = morphyPreferences.getConfigFilePath();
		
		String userDirectoryPath = morphyPreferences.getString(PreferenceKeys.FilesMorphyMainDir);
		if (LOG.isInfoEnabled()) {
			LOG.info(String.format("Using %s for User Directory",userDirectoryPath));
		}
		USER_DIRECTORY = new File(userDirectoryPath);
		if (!USER_DIRECTORY.exists() || !USER_DIRECTORY.canRead()) {
			throw new Exception("User Directory must exist and be readable");
		}
		
		String resourcesDirectoryPath = morphyPreferences.getString(PreferenceKeys.FilesMorphyResourcesDir);
		if (LOG.isInfoEnabled()) {
			LOG.info(String.format("Using %s for Resources Directory",resourcesDirectoryPath));
		}
		RESOURCES_DIR = new File(resourcesDirectoryPath);
		if (!RESOURCES_DIR.exists() || !RESOURCES_DIR.canRead()) {
			throw new Exception("Resources Directory must exist and be readable");
		}
		
		String commandFilesDirectoryPath = morphyPreferences.getString(PreferenceKeys.FilesMorphyCommandFilesDir);
		if (LOG.isInfoEnabled()) {
			LOG.info(String.format("Using %s for Command Files Directory",commandFilesDirectoryPath));
		}
		COMMAND_FILES_DIR = new File(commandFilesDirectoryPath);
		if (!COMMAND_FILES_DIR.exists() || !COMMAND_FILES_DIR.canRead()) {
			throw new Exception("Command Files Directory must exist and be readable");
		}
		
		String screenFilesDirectoryPath = morphyPreferences.getString(PreferenceKeys.FilesMorphyScreenFilesDir);
		if (LOG.isInfoEnabled()) {
			LOG.info(String.format("Using %s for Screen Files Directory",screenFilesDirectoryPath));
		}
		SCREEN_FILES_DIR = new File(screenFilesDirectoryPath);
		if (!SCREEN_FILES_DIR.exists() || !SCREEN_FILES_DIR.canRead()) {
			throw new Exception("Screen Files Directory must exist and be readable");
		}
	}
	
	public File getMorphyConfigFile() {
		return MORPHY_CONFIG_FILE;
	}
	
	public File getUserDirectory() {
		return USER_DIRECTORY;
	}
	
	public File getResourcesDirectory() {
		return RESOURCES_DIR;
	}
	
	public File getCommandFilesDirectory() {
		return COMMAND_FILES_DIR;
	}
	
	public File getScreenFilesDirectory() {
		return SCREEN_FILES_DIR;
	}
}
