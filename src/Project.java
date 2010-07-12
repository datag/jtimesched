import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;


public class Project implements Serializable {
	private static final long serialVersionUID = 1061321128496296078L;
	
	
	private String title;
	private ProjectPriority priority;
	private Date timeCreated;
	
	public Date getTimeCreated() {
		return timeCreated;
	}


	private int secondsOverall;
	private int secondsToday;
	
	private boolean running;
	private Date timeStart;
	
	public Project(String name) {
		this(name, ProjectPriority.MEDIUM);
	}
	
	public Project(String name, ProjectPriority priority) {
		this.title = name;
		this.priority = priority;
		
		this.timeStart = new Date();
		this.timeCreated = new Date();
	}
	
	// debug ctor
	public Project(String name, ProjectPriority priority, Date timeCreated, Date timeStart,
			boolean running, int secondsOverall, int secondsToday) {
		this(name, priority);
		
		this.timeCreated = timeCreated;
		this.timeStart = timeStart;
		this.running = running;
		this.secondsOverall = secondsOverall;
		this.secondsToday = secondsToday;
		
		this.checkResetToday();
	}
	
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String name) {
		this.title = name;
	}

	public ProjectPriority getPriority() {
		return priority;
	}

	public void setPriority(ProjectPriority priority) {
		this.priority = priority;
	}

	public boolean isRunning() {
		return this.running;
	}
	
	protected int getElapsedSeconds() throws ProjectException {
		if (!this.isRunning())
			throw new ProjectException("Timer not running");
		
		Date currentTime = new Date();
		return (int) ((currentTime.getTime() - this.timeStart.getTime()) / 1000);
	}
	
	public void start() throws ProjectException {
		if (this.isRunning())
			throw new ProjectException("Timer already running");
		
		// save current time
		this.timeStart = new Date();
		
		this.running = true;
	}
	
	public void pause() throws ProjectException {
		if (!this.isRunning())
			throw new ProjectException("Timer not running");
		
		int secondsElapsed = this.getElapsedSeconds();
		this.secondsOverall += secondsElapsed;
		this.secondsToday += secondsElapsed;
		
		this.running = false;
	}
	
	public int getSecondsToday() {
		int seconds = this.secondsToday;
		
		if (this.isRunning())
			try {
				seconds += this.getElapsedSeconds();
			} catch (ProjectException e) {
				e.printStackTrace();
			}
		
		return seconds;
	}
	
	public int getSecondsOverall() {
		int seconds = this.secondsOverall;
		
		if (this.isRunning())
			try {
				seconds += this.getElapsedSeconds();
			} catch (ProjectException e) {
				e.printStackTrace();
			}
		
		return seconds;
	}

	protected void checkResetToday() {
		Date currentTime = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("y-MMM-d");
		String strCurrentDay = sdf.format(currentTime);
		String strStartDay = sdf.format(this.timeStart);
		
		if (!strCurrentDay.equals(strStartDay)) {
			this.resetToday();
		}
	}
	
	public void resetOverall() {
		this.secondsOverall = 0;
		
		System.out.println("Resetting overall seconds");
	}
	
	public void resetToday() {
		this.secondsToday = 0;
		
		System.out.println("Resetting today's seconds");
	}
	
	// magic VM method for serialization
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	// magic VM method for de-serialization
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		
		// now we are a "live" object again
		this.checkResetToday();
	}
	
	
	@Override
	public String toString() {
		return "Project [name=" + title + ", priority=" + priority
				+ ", running=" + running + ", timeStart=" + timeStart + "]";
	}
	
	
}
