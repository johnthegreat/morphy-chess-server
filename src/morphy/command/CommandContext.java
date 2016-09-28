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
package morphy.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import morphy.Morphy;
import morphy.properties.PreferenceKeys;
import morphy.user.UserLevel;
import morphy.utils.MorphyStringTokenizer;
import morphy.utils.ResourceUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CommandContext {
	protected static Log LOG = LogFactory.getLog(CommandContext.class);

	protected String name;
	protected String[] aliases;
	protected String[] seeAlso;
	protected String usage;
	protected String lastModifiedBy;
	protected String lastModifiedDate;
	protected String help;
	protected UserLevel userLevel;

	public CommandContext(String commandFileName) {
		BufferedReader reader = null;
		try {

			File commandFilesDir = Morphy.getInstance().getMorphyFileProvider().getCommandFilesDirectory();
			String filePath = String.format("%s/%s.txt",commandFilesDir.getAbsolutePath(), commandFileName);
			reader = new BufferedReader(new FileReader(new File(filePath)));

			StringBuilder helpContent = new StringBuilder(1200);
			String lineTerminator = Morphy.getInstance().getMorphyPreferences().getString(
					PreferenceKeys.SocketConnectionLineDelimiter);

			boolean isParsingContent = false;
			String currentLine = null;

			while ((currentLine = reader.readLine()) != null) {
				if (isParsingContent) {
					helpContent.append(currentLine + lineTerminator);
				} else {
					if (StringUtils.startsWithIgnoreCase(currentLine, "Name:")) {
						name = currentLine.substring(5).trim().toLowerCase();
					} else if (StringUtils.startsWithIgnoreCase(currentLine,
							"UserLevel:")) {
						userLevel = UserLevel.valueOf(currentLine.substring(10)
								.trim());
					} else if (StringUtils.startsWithIgnoreCase(currentLine,
							"Usage:")) {
						usage = currentLine.substring(6).trim();
					} else if (StringUtils.startsWithIgnoreCase(currentLine,
							"Aliases:")) {
						String content = currentLine.substring(8).trim();
						MorphyStringTokenizer tok = new MorphyStringTokenizer(
								content, " ");
						List<String> aliasesList = new ArrayList<String>(10);
						while (tok.hasMoreTokens()) {
							aliasesList.add(tok.nextToken());
						}
						aliases = aliasesList.toArray(new String[0]);

					} else if (StringUtils.startsWithIgnoreCase(currentLine,
							"SeeAlso:")
							|| StringUtils.startsWithIgnoreCase(currentLine,
									"See Also:")) {
						String content = currentLine.substring(8).trim();
						MorphyStringTokenizer tok = new MorphyStringTokenizer(
								content, " ");
						List<String> seeAlsoList = new ArrayList<String>(10);
						while (tok.hasMoreTokens()) {
							seeAlsoList.add(tok.nextToken());
						}
						aliases = seeAlsoList.toArray(new String[0]);
					} else if (StringUtils.startsWithIgnoreCase(currentLine,
							"LastModifiedBy:")) {
						lastModifiedBy = currentLine.substring(15).trim();
					} else if (StringUtils.startsWithIgnoreCase(currentLine,
							"LastModifiedDate:")) {
						lastModifiedDate = currentLine.substring(17).trim();
					} else if (StringUtils.startsWithIgnoreCase(currentLine,
							"Help:")) {
						isParsingContent = true;
						helpContent.append(currentLine.substring(5));
						continue;
					} else {
						LOG
								.warn("Encountered command header without a known keyword "
										+ currentLine);
					}
				}
			}
			help = helpContent.toString();

			if (StringUtils.isBlank(getName())) {
				throw new IllegalArgumentException(
						"Could not find Name: header in command. "
								+ commandFileName);
			}
			if (userLevel == null) {
				throw new IllegalArgumentException(
						"Could not find UserLevel: header in command. "
								+ commandFileName);
			}
		} catch (Throwable t) {
			if (LOG.isErrorEnabled())
				LOG.error("Error reading help file: " + commandFileName, t);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (Throwable t) {
				}
			}
		}
	}

	public String[] getAliases() {
		return aliases;
	}

	public String getHelp() {
		return help;
	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public String getLastModifiedDate() {
		return lastModifiedDate;
	}

	public String getName() {
		return name;
	}

	public String[] getSeeAlso() {
		return seeAlso;
	}

	public String getUsage() {
		return usage;
	}

	public UserLevel getUserLevel() {
		return userLevel;
	}

	public void setAliases(String[] aliases) {
		this.aliases = aliases;
	}

	public void setHelp(String help) {
		this.help = help;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public void setLastModifiedDate(String lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSeeAlso(String[] seeAlso) {
		this.seeAlso = seeAlso;
	}

	public void setUsage(String usage) {
		this.usage = usage;
	}

	public void setUserLevel(UserLevel userLevel) {
		this.userLevel = userLevel;
	}
}
