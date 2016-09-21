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
package morphy.service;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import morphy.Morphy;
import morphy.channel.Channel;
import morphy.properties.PreferenceKeys;
import morphy.service.ScreenService.Screen;
import morphy.user.PersonalList;
import morphy.user.PlayerType;
import morphy.user.SocketChannelUserSession;
import morphy.user.User;
import morphy.user.UserLevel;
import morphy.user.UserSession;
import morphy.user.UserVars;
import morphy.utils.BufferUtils;
import morphy.utils.MorphyStringUtils;
import morphy.utils.SocketUtils;
import morphy.utils.john.DBConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SocketConnectionService implements Service {
	protected static Log LOG = LogFactory.getLog(SocketConnectionService.class);
	private static final SocketConnectionService singletonInstance = new SocketConnectionService();

	public static SocketConnectionService getInstance() {
		return singletonInstance;
	}

	protected ServerSocketChannel serverSocketChannel;
	protected Selector serverSocketSelector;
	protected int maxCommunicationSizeBytes;
	protected Thread selectionThread = null;

	protected Map<Socket, SocketChannelUserSession> socketToSession = new HashMap<Socket, SocketChannelUserSession>();
	protected Map<Socket, StringBuilder> socketInputForCmd = new HashMap<Socket, StringBuilder>();
	
	protected Runnable selectSocketRunnable = new Runnable() {
		public void run() {
			try {
				while (true) {
					if (Morphy.getInstance().isShutdown()) {
						return;
					}
					serverSocketSelector.select();
					Set<SelectionKey> keys = serverSocketSelector
							.selectedKeys();

					// if (LOG.isInfoEnabled()) {
					// LOG.info("Selected " + keys.size() + " keys.");
					// }

					Iterator<SelectionKey> i = keys.iterator();

					while (i.hasNext()) {
						SelectionKey key = i.next();
						i.remove();

						try {
							if (key.isAcceptable()) {
								final SocketChannel channel = serverSocketChannel
										.accept();
								channel.configureBlocking(false);
								channel.register(serverSocketSelector,
										SelectionKey.OP_READ);

								ThreadService.getInstance().run(new Runnable() {
									public void run() {
										onNewChannel(channel);
									}
								});
							}
							if (key.isReadable()) {
								final SocketChannel channel = (SocketChannel) key
										.channel();
								ThreadService.getInstance().run(new Runnable() {
									public void run() {
										onNewInput(channel);
									}
								});
							}
						} catch (CancelledKeyException e) {
							// logging the user out now.
						}
					}
				}
			} catch (Throwable t) {
				if (LOG.isErrorEnabled())
					LOG
							.error(
									"Error reading selector in SocketConnectionService",
									t);
			}
		}
	};

	private SocketConnectionService() {
		try {
			maxCommunicationSizeBytes = PreferenceService
					.getInstance()
					.getInt(
							PreferenceKeys.SocketConnectionServiceMaxCommunicationBytes);
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			
			/*Object obj = PreferenceService.getInstance().getProperty(PreferenceKeys.SocketConnectionServicePorts.toString());
			System.out.println(obj.getClass());
			if (obj instanceof java.util.ArrayList) {
				//System.out.println((java.util.ArrayList<String>)obj);
				String[] arr = ((java.util.ArrayList<String>)obj).toArray(new String[0]);
				System.out.println(java.util.Arrays.toString(arr));
				//serverSocketChannel.socket().
				for(int i=0;i<arr.length;i++) {
					serverSocketChannel.socket().bind( 
							new java.net.InetSocketAddress( Integer.parseInt(arr[i]) ));
					if (LOG.isInfoEnabled()) {
						LOG.info("Listening on port " + arr[i]);
					}
				}
			} else {
				if (LOG.isInfoEnabled()) {
					serverSocketChannel.socket().bind( new java.net.InetSocketAddress( 5000 ));
					LOG.info("LOAD CONFIG FAILED - Listening on port 5000");
				}
			}*/
			
			serverSocketChannel.socket().bind( new java.net.InetSocketAddress( PreferenceService.getInstance().getInt(PreferenceKeys.SocketConnectionServicePorts.toString()) ));
			serverSocketSelector = Selector.open();
			serverSocketChannel.register(serverSocketSelector,
					SelectionKey.OP_ACCEPT);

			selectionThread = new Thread(selectSocketRunnable);
			selectionThread.setPriority(Thread.MAX_PRIORITY);
			selectionThread.start();

			LOG.info("Initialized Socket Connection Service host:"
					+ serverSocketChannel.socket().getInetAddress() + " "
					+ serverSocketChannel.socket().getLocalPort());
		} catch (Throwable t) {
			if (LOG.isErrorEnabled())
				LOG.error("Error initializing SocketConnectionService", t);
		}
	}

	public void dispose() {
		try {
			serverSocketChannel.close();
		} catch (Throwable t) {
			if (LOG.isErrorEnabled())
				LOG.error("Error disposing SocketConnectionService", t);
		}
	}

	public String formatMessage(UserSession userSession, String message) {
		// In the future this might adjust width and handle nowrap.
		return MorphyStringUtils.formatServerMessage(message);
	}

	public void removeUserSession(SocketChannelUserSession session) {
		disposeSocketChannel(session.getChannel());
	}

	protected void disposeSocketChannel(SocketChannel channel) {
		if (channel.isConnected()) {
			try {
				channel.close();
			} catch (Throwable t) {
			}
		}
		socketToSession.remove(channel.socket());
	}

	protected void handleLoginPromptText(SocketChannelUserSession userSession,
			String message) {
		sendWithoutPrompt("\n",userSession);
		
		String name = message;
		if (name.equalsIgnoreCase("g"))
			name = "guest";
		
		if (name.matches("\\w{3,17}")) {
			if (LOG.isInfoEnabled()) {
				LOG.info("name=" + name);
			}

			UserService instance = UserService.getInstance();
			
			boolean isGuest = false;
			
			if (name.equalsIgnoreCase("g") || name.equalsIgnoreCase("guest")) {
				do {
					name = instance.generateAnonymousHandle();
				} while (instance.isLoggedIn(name));

//				userSession
//						.send("Logging you in as \""
//								+ name
//								+ "\"; you may use this name to play unrated games.\n"
//								+ "(After logging in, do \"help register\" for more info on how to register.)\n\n"
//								+ "" + "Press return to enter the server as \""
//								+ name + "\":\n");
			}
			
			isGuest = !instance.isRegistered(name);
			
			if (isGuest) {
				sendWithoutPrompt("\"" + name + "\" is not a registered name. You may use this name to play unrated games.\n" +
						"(After logging in, do \"help register\" for more info on how to register.)\n\n" +
						"" +
						"Press return to enter the server as \"" + name + "\":",userSession);
				//return;
			} else {
				sendWithoutPrompt("\""
								+ name
								+ "\" is a registered name.  If it is yours, type the password.\n"
								+ "If not, just hit return to try another name.\n\n"
								+ "" + "password: ",userSession);
				/*try { userSession.getChannel().configureBlocking(true); } catch(Exception e) { e.printStackTrace(System.err); }*/
				try {
					DBConnection conn = DBConnectionService.getInstance().getDBConnection();
					java.sql.Statement s = conn.getStatement();
					s.execute("SELECT `password` FROM `users` WHERE `username` = '" + name + "'");
					java.sql.ResultSet r = s.getResultSet();
					if (r.next()) {
						String actualpass = r.getString(1);
						
						if (!actualpass.equals(actualpass)) {
							userSession.send("**** Invalid password! ****\n\n" +
									"If you cannot remember your password, please log in with \"g\" and ask for help\n" +
									"in channel 4. Type \"tell 4 I've forgotten my password\". If that is not\n" +
									"possible, please email: support@freechess.org\n\n" +
									"\tIf you are not a registered player, enter guest or a unique ID.\n" +
									"\t\t(If your return key does not work, use cntrl-J)\n\n");
							//userSession.disconnect();
						}
					}
				} catch(java.sql.SQLException e) {
					e.printStackTrace(System.err);
				}
			}

			boolean isLoggedIn = instance.isLoggedIn(name);
			if (isLoggedIn) {
				/* this code in this logic block should be commented out to support multiple-login. */
				userSession.send(name + " is already logged in - kicking them out.");
				
				UserSession sess = instance.getUserSession(name);
				sess.send("**** " + name + " has arrived - you can't both be logged in. ****");
				sess.disconnect();
				isLoggedIn = !isLoggedIn;
			}
			
			userSession.getUser().setUserName(name);
			userSession.getUser().setPlayerType(PlayerType.Human);
			userSession.getUser().setUserLevel(isGuest?UserLevel.Guest:UserLevel.Player);
			userSession.getUser().setRegistered(!isGuest);
			userSession.getUser().setUserVars(new morphy.user.UserVars(userSession.getUser()));
			if (isGuest) {
				userSession.getUser().getUserVars().getVariables().put("rated", "0");
			}
			userSession.setHasLoggedIn(true);
			if (!isLoggedIn) {
				instance.addLoggedInUser(userSession);
			} else {
				// This code is used for multiple-login.
				/*SocketChannelUserSession sess = (SocketChannelUserSession) instance.getUserSession(name);
				sess.addUserOnMultipleLogins(userSession);
				userSession.addParentOnMultipleLogins(sess);*/
			}
			userSession.getUser().setDBID(instance.getDBID(name));

			boolean isHeadAdmin = false;

			if (!isGuest) {
				DBConnection conn = DBConnectionService.getInstance().getDBConnection();
				
				String query = "SELECT pl.`name`,pe.`value` FROM personallist pl INNER JOIN personallist_entry pe ON (pe.personallist_id = pl.id) WHERE pl.user_id = '" + userSession.getUser().getDBID() + "'";
				java.sql.ResultSet rs = conn.executeQueryWithRS(query);
				try {
					while(rs.next()) {
						PersonalList pl = PersonalList.valueOf(rs.getString(1));
						String val = rs.getString(2);
						userSession.getUser().getLists().get(pl).add(val);
						if (pl == PersonalList.channel) {
							int channelNum = Integer.parseInt(val);
							Channel c = ChannelService.getInstance().getChannel(channelNum);
							if (c != null) {
								c.addListener(userSession);
							}
						}
					}
				} catch(SQLException e) { Morphy.getInstance().onError(e); }
				
				Map<PersonalList,Integer> map = new HashMap<PersonalList,Integer>();
				query = "SELECT `name`,`id` FROM `personallist` WHERE `user_id` = '" + userSession.getUser().getDBID() + "'";
				rs = conn.executeQueryWithRS(query);
				try {
					while(rs.next()) {
						map.put(PersonalList.valueOf(rs.getString(1)),rs.getInt(2));
					}
				} catch(SQLException e) { Morphy.getInstance().onError(e); }
				userSession.getUser().setPersonalListDBIDs(map);
				
				conn.executeQuery("UPDATE `users` SET `lastlogin` = CURRENT_TIMESTAMP, `ipaddress` = '"
								+ SocketUtils.getIpAddress(userSession.getChannel().socket()) + "' WHERE `username` = '" + name + "'");
				ResultSet r = conn.executeQueryWithRS("SELECT `adminLevel` FROM `users` WHERE `username` = '" + name + "'");
				try {
					if (r.next()) {
						String level = r.getString(1);
						UserLevel val = UserLevel.valueOf(level);
						userSession.getUser().setUserLevel(val);
						if (val == UserLevel.Admin
								|| val == UserLevel.SuperAdmin
								|| val == UserLevel.HeadAdmin) {
							ServerListManagerService s = ServerListManagerService
									.getInstance();
							s.getElements().get(s.getList("admin")).add(
									name);
						}

						if (val == UserLevel.HeadAdmin) {
							isHeadAdmin = true;
						}
					}
				} catch (SQLException e) {
					if (LOG.isErrorEnabled()) {
						LOG
								.error("Unable to set user level from database for name \""
										+ name + "\"");
						LOG.error(e);
					}
				}
			}

			StringBuilder loginMessage = new StringBuilder(200);
			loginMessage.append(formatMessage(userSession,
					"**** Starting FICS session as " 
							+ instance.getTags(name) + " ****\n"));
			if (isHeadAdmin)
				loginMessage.append("\n  ** LOGGED IN AS HEAD ADMIN **\n");
			loginMessage.append(ScreenService.getInstance().getScreen(
					Screen.SuccessfulLogin));
			userSession.send(loginMessage.toString());
			
//			query = "SELECT DISTINCT u.username FROM `morphyics`.`personallist` pl INNER JOIN users u ON (pl.user_id = u.id) WHERE pl.`name` = 'notify'";
//			rs = dbcs.getDBConnection().executeQueryWithRS(query);
//			try {
//				UserService us = UserService.getInstance();
//				while(rs.next()) {
//					String username = rs.getString(1);
//					UserSession sess = us.getUserSession(username);
//					sess.send("Notification: " + name + " has arrived.");
//				} 
//			} catch(SQLException e) { Morphy.getInstance().onError(e); }
			
			UserSession[] sessions = UserService.getInstance().fetchAllUsersWithVariable("pin","1");
			for(UserSession s : sessions) {
				UserLevel adminLevel = s.getUser().getUserLevel();
				
				if (adminLevel == UserLevel.Admin || adminLevel == UserLevel.SuperAdmin || adminLevel == UserLevel.HeadAdmin) {
					s.send(String.format("[%s (%s: %s) has connected.]",
								userSession.getUser().getUserName(),
								!isGuest?"R":"U",
								SocketUtils.getIpAddress(userSession.getChannel().socket())));
				} else {
					s.send(String.format("[%s has connected.]",userSession.getUser().getUserName()));
				}
			}
			
			DBConnectionService dbcs = DBConnectionService.getInstance();
			
			java.util.List<String> arrivalNotedBy = new java.util.ArrayList<String>(10);
			// this query gets all usernames with this player on their notify list.
			String query = "SELECT u.username FROM personallist pl INNER JOIN personallist_entry ple ON (pl.id = ple.personallist_id) INNER JOIN users u ON (u.id = pl.user_id) WHERE pl.`name` = 'notify' && ple.`value` LIKE '" + userSession.getUser().getUserName() + "';";
			ResultSet rs = dbcs.getDBConnection().executeQueryWithRS(query);
			try {
				UserService us = UserService.getInstance();
				while(rs.next()) {
					String username = rs.getString(1);
					UserSession sess = us.getUserSession(username);
					if (sess != null) {
						UserVars uv = sess.getUser().getUserVars();
						boolean highlight = uv.getVariables().get("highlight").equals("1");
						if (sess != null && sess.isConnected()) {
							sess.send("Notification: " + (highlight?((char)27)+"[7m":"") + name + (highlight?((char)27)+"[0m":"") + " has arrived.");
							arrivalNotedBy.add(sess.getUser().getUserName());
						}
					}
				}
			} catch(SQLException e) { Morphy.getInstance().onError(e); }
			if (arrivalNotedBy.size() > 0) {
				userSession.send("Your arrival was noted by: " + MorphyStringUtils
						.toDelimitedString(arrivalNotedBy.toArray(new String[arrivalNotedBy.size()])," "));
			}
			
			query = "SELECT ple.`value` FROM personallist pl INNER JOIN personallist_entry ple ON (pl.id = ple.personallist_id) WHERE pl.user_id = " + userSession.getUser().getDBID() + " && pl.`name` = 'notify'"; // get this player's notify list
			rs = dbcs.getDBConnection().executeQueryWithRS(query);
			try {
				UserService us = UserService.getInstance();
				while(rs.next()) {
					String username = rs.getString(1);
					if (arrivalNotedBy.contains(username)) continue;
					UserSession sess = us.getUserSession(username);
					if (sess == null) continue;
					UserVars uv = sess.getUser().getUserVars();
					boolean highlight = uv.getVariables().get("highlight").equals("1");
					if (sess != null && sess.isConnected()) sess.send("Notification: " + (highlight?((char)27)+"[7m":"") + name + (highlight?((char)27)+"[0m":"") + " has arrived and isn't on your notify list.");
				}
			} catch(SQLException e) { Morphy.getInstance().onError(e); }
			// Notification: ChannelBot has arrived and isn't on your notify list.
		} else {
			sendWithoutPrompt("Invalid user name: " + message + " Good Bye.\n",
					userSession);
			userSession.disconnect();
		}
	}

	protected String readMessage(SocketChannel channel) {
		try {
			ByteBuffer buffer = ByteBuffer.allocate(maxCommunicationSizeBytes);
			int charsRead = -1;
			try {
				charsRead = channel.read(buffer);
			} catch (IOException cce) {
				channel.close();
				if (LOG.isInfoEnabled()) {
					LOG.info("Closed channel " + channel);
				}
			}
			if (charsRead == -1) {
				return null;
			} else if (charsRead > 0) {
				buffer.flip();
				Charset charset = Charset
						.forName(PreferenceService
								.getInstance()
								.getString(
										PreferenceKeys.SocketConnectionServiceCharEncoding));
				CharsetDecoder decoder = charset.newDecoder();
				CharBuffer charBuffer = decoder.decode(buffer);
				return charBuffer.toString();
			} else {
				return "";
			}
		} catch (Throwable t) {
			if (LOG.isErrorEnabled())
				LOG.error("Error reading SocketChannel "
						+ channel.socket().getLocalAddress(), t);
			return null;
		}
	}

	protected void sendWithoutPrompt(String message,
			SocketChannelUserSession session) {
		try {
			if (session.isConnected()) {
				ByteBuffer buffer = BufferUtils.createBuffer(formatMessage(
						session, message));
				session.getChannel().write(buffer);
			} else {
				if (LOG.isInfoEnabled()) {
					LOG.info("Tried to send message to a logged off user "
							+ session.getUser().getUserName() + " " + message);
				}
				if (LOG.isInfoEnabled())
					LOG.info("ession.disconnect(); called");
				session.disconnect();
			}
		} catch (Throwable t) {
			if (LOG.isErrorEnabled())
				LOG.error("Error sending message to user "
						+ session.getUser().getUserName() + " " + message, t);
			session.disconnect();
		}
	}

	private void onNewChannel(SocketChannel channel) {
		if (LOG.isInfoEnabled()) {
			LOG.info("onNewChannel();");
		}

		try {
			SocketChannelUserSession session = new SocketChannelUserSession(
					new User(), channel);
			socketToSession.put(channel.socket(), session);

//			ByteBuffer buffer = BufferUtils.createBuffer(ScreenService
//					.getInstance().getScreen(Screen.Login));
//			channel.write(buffer);
			sendWithoutPrompt(ScreenService.getInstance().getScreen(Screen.Login),session);

			if (LOG.isInfoEnabled()) {
				LOG.info("Received socket connection "
						+ channel.socket().getInetAddress());
			}
		} catch (Throwable t) {
			if (LOG.isErrorEnabled()) {
				LOG.error("Error writing to SocketChannel "
						+ channel.socket().getInetAddress(), t);
			}

			disposeSocketChannel(channel);
		}
	}

	private void onNewInput(SocketChannel channel) {
		if (!channel.isOpen()) return;
		
		try {
			if (channel.isConnected()) {
				SocketChannelUserSession session = socketToSession.get(channel
						.socket());

				if (session == null) {
					if (LOG.isErrorEnabled()) {
						LOG.error("Received a read on a socket not being managed. This is likely a bug.");
					}

					disposeSocketChannel(channel);
				} else {
					synchronized (session.getInputBuffer()) {
						String message = readMessage(channel);
						if (message == null && channel.isOpen()) {
							session.disconnect();
						} else if (message == null) {
							session.disconnect();
						} else if (message.length() > 0) {
							/*if (!socketInputForCmd.containsKey(channel.socket())) {
								socketInputForCmd.put(channel.socket(), new StringBuilder());
							}
							int c = (int)message.charAt(0);
							if (c != 10 && c != 13) {
								socketInputForCmd.get(channel.socket()).append(message);
								//LOG.info(c);
							} else {
								message = socketInputForCmd.get(channel.socket()).toString();
								socketInputForCmd.put(channel.socket(), new StringBuilder()); 
								LOG.info("Read: "
										+ session.getUser().getUserName() + " \""
										+ message + "\"");
							}
							LOG.info(c + " " + socketInputForCmd.get(channel.socket()));*/
							
							boolean expandAliases = true; 
							if (message.startsWith("$$")) {
								message = message.substring(2);
								expandAliases = false;
							} else {
								session.touchLastReceivedTime();
								session.getUser().getUserVars().getVariables().put("busy","");
								expandAliases = true;
							}
							
							session.getInputBuffer().append(message);
							if (session.getInputBuffer().indexOf("\n") != -1) {
								LOG.info("Read: "
									+ session.getUser().getUserName() + " \""
									+ message + "\"");
							}
							int carrageReturnIndex = -1;
							while ((carrageReturnIndex = session
									.getInputBuffer().indexOf("\n")) != -1) {
								
								String command = session.getInputBuffer()
										.substring(0, carrageReturnIndex)
										.trim();
								session.getInputBuffer().delete(0,
										carrageReturnIndex + 1);

								if (command.equals("") || command.equals("\n")
										|| command.equals("\n\r")) {
									session.send("");
								} else if (!session.hasLoggedIn()) {
									handleLoginPromptText(session, command);
								} else {
									if (expandAliases) {
										CommandService.getInstance()
											.processCommandAndCheckAliases(command, session);
									} else {
										CommandService.getInstance()
											.processCommand(command, session);
									}
								}
							}
						}
					}
				}
			}
		} catch (Throwable t) {
			if (LOG.isErrorEnabled()) {
				LOG.error(
						"Error reading socket channel or processing command ",
						t);
			}

		}
	}
}
