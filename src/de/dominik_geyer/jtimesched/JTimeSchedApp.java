/* jTimeSched - A simple and lightweight time tracking tool
 * Copyright (C) 2010-2012 Dominik D. Geyer <dominik.geyer@gmail.com>
 * See LICENSE.txt for details.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.dominik_geyer.jtimesched;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import de.dominik_geyer.jtimesched.gui.JTimeSchedFrame;
import de.dominik_geyer.jtimesched.misc.PlainTextFormatter;

/**
 * Main class of the application.
 */
public class JTimeSchedApp {
	static public final String DATA_PATH = "data/";
	static public final String IMAGES_PATH = DATA_PATH + "img/";
	static public final String CONF_PATH = "conf/";
	static public final String PRJ_FILE = CONF_PATH + "jTimeSched.projects";
	static public final String PRJ_FILE_BACKUP = JTimeSchedApp.PRJ_FILE + ".backup";
	static public final String SETTINGS_FILE = CONF_PATH + "jTimeSched.settings";
	static public final String LOCK_FILE = CONF_PATH + "jTimeSched.lock";
	static public final String LOG_FILE = CONF_PATH + "jTimeSched.log";
	
	static private Logger LOGGER;
	
	/**
	 * Application's entry point.
	 * 
	 * @param args Command line arguments
	 */
	public static void main(String[] args) {
		// FIXME: allow different config-path via command-line argument
		File dirConf = new File(JTimeSchedApp.CONF_PATH);
		if (!dirConf.isDirectory())
			dirConf.mkdir();
		
		// request lock
		if (!JTimeSchedApp.lockInstance()) {
			JOptionPane.showMessageDialog(null,
					"It seems that there is already a running instance of jTimeSched " +
					"using the projects-file in use.\n\n" +
					"Possible solutions:\n" +
					"1) Most likely you want to use the running instance residing in the system-tray.\n" +
					"2) Run another instance from within a different directory.\n" +
					"3) Delete the lock-file '" + JTimeSchedApp.LOCK_FILE + "' manually if it is a leftover caused by an unclean shutdown.\n\n" +
					"jTimeSched will exit now.",
					"Another running instance for projects-file detected",
					JOptionPane.WARNING_MESSAGE);
			
			System.exit(1);
		}
		
		
		// initialize logger
		JTimeSchedApp.LOGGER = Logger.getLogger("JTimeSched");
		JTimeSchedApp.LOGGER.setLevel(Level.ALL);
		
		try {
			FileHandler fh = new FileHandler(JTimeSchedApp.LOG_FILE, true);
			fh.setFormatter(new PlainTextFormatter());
			JTimeSchedApp.LOGGER.addHandler(fh);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Enable to initialize logger for file "+JTimeSchedApp.LOG_FILE);
		}
		
		
		// open main frame
		new JTimeSchedFrame();
	}

	/**
	 * Determines and returns the application's version, which is set in the Manifest file in attribute "ImplementationVersion".
	 * 
	 * @return String The application's version; if not set in Manifest or not available it returns the string "unknown"
	 */
	public static String getAppVersion()
	{
		String appVersion = Package.getPackage("de.dominik_geyer.jtimesched").getImplementationVersion();
		return (appVersion != null) ? appVersion : "unknown";
	}

	private static boolean lockInstance() {
		try {
			final File file = new File(JTimeSchedApp.LOCK_FILE);
			final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
			final FileLock fileLock = randomAccessFile.getChannel().tryLock();
			if (fileLock != null) {
				Runtime.getRuntime().addShutdownHook(new Thread() {
					public void run() {
						try {
							fileLock.release();
							randomAccessFile.close();
							file.delete();
						} catch (Exception e) {
							System.err.println("Unable to remove lock file: " + JTimeSchedApp.LOCK_FILE + " " + e.getMessage());
						}
					}
				});
				return true;
			}
		} catch (Exception e) {
			System.err.println("Unable to create and/or lock file: " + JTimeSchedApp.LOCK_FILE + " " + e.getMessage());
		}
		return false;
	}


	public static Logger getLogger() {
		return LOGGER;
	}
}
