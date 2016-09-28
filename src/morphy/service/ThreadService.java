/*
 *   Morphy Open Source Chess Server
 *   Copyright (C) 2008-2010, 2016  http://code.google.com/p/morphy-chess-server/
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

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Date;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import morphy.Morphy;
import morphy.properties.MorphyPreferences;
import morphy.properties.PreferenceKeys;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ThreadService implements Service {
	protected static final class RunnableExceptionDecorator implements Runnable {
		protected Runnable runnable;

		public RunnableExceptionDecorator(Runnable runnable) {
			this.runnable = runnable;
		}

		public void run() {
			try {
				runnable.run();
			} catch (Throwable t) {
				if (LOG.isErrorEnabled())
					LOG.error(
						"Error in ThreadService Runnable.", t);
			}
		}

	}

	protected static final Log LOG = LogFactory.getLog(ThreadService.class);

	public String THREAD_DUMP_FILE_PATH = null;

	private static final ThreadService instance = new ThreadService();

	public static ThreadService getInstance() {
		return instance;
	}

	/**
	 * Dumps stack traces of all threads to threaddump.txt.
	 */
	public void threadDump() {
		LOG
				.error("All threads are in use. Logging the thread stack trace to threaddump.txt and exiting.");
		final ThreadMXBean threads = ManagementFactory.getThreadMXBean();
		long[] threadIds = threads.getAllThreadIds();
		PrintWriter printWriter = null;
		try {
			printWriter = new PrintWriter(new FileWriter(THREAD_DUMP_FILE_PATH,
					false));
			printWriter.println("Morphy ThreadService initiated dump "
					+ new Date());
			for (long threadId : threadIds) {
				ThreadInfo threadInfo = threads.getThreadInfo(threadId, 10);
				printWriter.println("Thread " + threadInfo.getThreadName()
						+ " Block time:" + threadInfo.getBlockedTime()
						+ " Block count:" + threadInfo.getBlockedCount()
						+ " Lock name:" + threadInfo.getLockName()
						+ " Waited Count:" + threadInfo.getWaitedCount()
						+ " Waited Time:" + threadInfo.getWaitedTime()
						+ " Is Suspended:" + threadInfo.isSuspended());
				StackTraceElement[] stackTrace = threadInfo.getStackTrace();
				for (StackTraceElement element : stackTrace) {
					printWriter.println(element);
				}

			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (printWriter != null) {
				try {
					printWriter.flush();
					printWriter.close();
				} catch (Exception e2) {
				}
			}
		}
	}

	ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(200);

	protected boolean isDisposed = false;

	private ThreadService() {
		MorphyPreferences morphyPreferences = Morphy.getInstance().getMorphyPreferences();
		
		THREAD_DUMP_FILE_PATH = Morphy.getInstance().getMorphyFileProvider()
				.getUserDirectory()
				+ "/logs/threaddump_" + System.currentTimeMillis() + ".txt";
		
		executor.setCorePoolSize(morphyPreferences.getInt(
				PreferenceKeys.ThreadServiceCoreThreads));
		executor.setMaximumPoolSize(morphyPreferences.getInt(
				PreferenceKeys.ThreadServiceMaxThreads));
		executor.setKeepAliveTime(morphyPreferences.getInt(
				PreferenceKeys.ThreadServiceKeepAlive), TimeUnit.SECONDS);
		executor.prestartAllCoreThreads();
		if (LOG.isInfoEnabled()) {
			LOG.info("Initialized ThreadService");
		}
	}

	public void dispose() {
		executor.shutdownNow();
		isDisposed = true;
	}

	public ScheduledThreadPoolExecutor getExecutor() {
		return executor;
	}

	/**
	 * Executes a runnable asynch in a controlled way. Exceptions are monitored
	 * and displayed if they occur.
	 */
	public void run(Runnable runnable) {
		if (!Morphy.getInstance().isShutdown()) {
			try {
				executor.execute(new RunnableExceptionDecorator(runnable));
			} catch (RejectedExecutionException rej) {
				if (!Morphy.getInstance().isShutdown()) {
					LOG.error("Error executing runnable: ", rej);
					threadDump();
					if (LOG.isErrorEnabled())
						LOG.error(
							"ThreadService has no more threads. A thread dump can be found at "
									+ THREAD_DUMP_FILE_PATH, rej);
				}
			}
		} else {
			if (LOG.isInfoEnabled()) {
				LOG.info("Vetoing runnable in ThreadService."
						+ runnable);
			}
		}
	}

	/**
	 * Runs the runnable one time after a delay. Exceptions are monitored and
	 * displayed if they occur.
	 * 
	 * @param delay
	 *            Delay in milliseconds
	 * @param runnable
	 *            The runnable.
	 * @return The Future, may return null if there was an error scheduling the
	 *         Runnable or if execution was vetoed.
	 */
	public Future<?> scheduleOneShot(long delay, Runnable runnable) {
		if (!Morphy.getInstance().isShutdown()) {
			try {
				return executor.schedule(new RunnableExceptionDecorator(
						runnable), delay, TimeUnit.MILLISECONDS);
			} catch (RejectedExecutionException rej) {
				if (!Morphy.getInstance().isShutdown()) {
					LOG.error("Error executing runnable in scheduleOneShot: ",
							rej);
					threadDump();
					if (LOG.isErrorEnabled())
						LOG.error(
							"ThreadService has no more threads. A thread dump can be found at "
									+ THREAD_DUMP_FILE_PATH);
				}
				return null;
			}
		} else {
			LOG.info("Vetoing runnable " + runnable + " because Morphy is shutdown.");
			return null;
		}
	}
}
