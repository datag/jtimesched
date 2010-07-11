import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.ArrayList;

import javax.swing.JOptionPane;

public class JTimeSchedApp {
	static public final String APP_VERSION = "0.2";
	static public final String DATA_PATH = "data/";
	static public final String IMAGES_PATH = DATA_PATH + "img/";
	static public final String PRJ_FILE = "jTimeSched.projects";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String lockFile = "jTimeSched.lock";
		if (!JTimeSchedApp.lockInstance(lockFile)) {
			JOptionPane.showMessageDialog(null,
					"There is already an instance of jTimeSched running. " +
					"It's not possible to run multiple instances.\n\n" +
					"If there's no other instance running, try to delete the lock-file '" + lockFile + "'.",
					"Another running instance detected",
					JOptionPane.ERROR_MESSAGE);
			
			System.exit(1);
		}
		
		new JTimeSchedFrame().setVisible(true);
		
		
	}
	
	
	private static boolean lockInstance(final String lockFile) {
		try {
			final File file = new File(lockFile);
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
							System.err.println("Unable to remove lock file: " + lockFile + " " + e.getMessage());
						}
					}
				});
				return true;
			}
		} catch (Exception e) {
			System.err.println("Unable to create and/or lock file: " + lockFile + " " + e.getMessage());
		}
		return false;
	}


	static public ArrayList<Project> loadProjects() throws Exception {
		ArrayList<Project> arPrj = null;
		
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream(JTimeSchedApp.PRJ_FILE);
			in = new ObjectInputStream(fis);
			arPrj = (ArrayList<Project>) in.readObject();
			in.close();
			
			//System.out.println(arPrj);
		} catch(IOException ex) {
			throw ex;
		} catch(ClassNotFoundException ex) {
			throw ex;
		}
		
		return arPrj;
	}
	
	static public void saveProjects(ArrayList<Project> arPrj) throws Exception {
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try	{
			fos = new FileOutputStream(JTimeSchedApp.PRJ_FILE);
			out = new ObjectOutputStream(fos);
			out.writeObject(arPrj);
			out.close();
		} catch(IOException ex) {
			throw ex;
		}
	}
}
