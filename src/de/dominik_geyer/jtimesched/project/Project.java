package de.dominik_geyer.jtimesched.project;

import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.dominik_geyer.jtimesched.JTimeSchedApp;
import de.dominik_geyer.jtimesched.gui.JTimeSchedFrame;


public class Project implements Serializable {
	private static final long serialVersionUID = 1061321128496296078L;
	
	private String title;
	private Date timeCreated;
	private Color color;
	private boolean checked;
	
	private int secondsOverall;
	private int secondsToday;
	
	private boolean running;
	private Date timeStart;
	
	public Project(String name) {
		this.title = name;
		this.color = null;
		
		this.timeStart = new Date();
		this.timeCreated = new Date();
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String name) {
		this.title = name;
	}
	
	public Date getTimeCreated() {
		return timeCreated;
	}
	
	public boolean isRunning() {
		return this.running;
	}
	
	public Color getColor() {
		return color;
	}
	
	public void setColor(Color color) {
		this.color = color;
	}
	
	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
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
	
	public void setSecondsOverall(int secondsOverall) {
		if (secondsOverall < 0)
			secondsOverall = 0;
		
		this.secondsOverall = secondsOverall;
	}

	public void setSecondsToday(int secondsToday) {
		if (secondsToday < 0)
			secondsToday = 0;
		
		this.secondsToday = secondsToday;
	}
	
	public void adjustSecondsToday(int secondsToday) {
		if (secondsToday < 0)
			secondsToday = 0;
		
		int secondsDelta = secondsToday - this.secondsToday;
		
		this.setSecondsOverall(this.getSecondsOverall() + secondsDelta);
		this.setSecondsToday(secondsToday);
	}

	protected void checkResetToday() {
		Date currentTime = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("y-MMM-d");
		String strCurrentDay = sdf.format(currentTime);
		String strStartDay = sdf.format(this.timeStart);
		
		if (!strCurrentDay.equals(strStartDay)) {
			// FIXME: avoid dependency of unrelated classes here
			JTimeSchedApp.getLogger().info(String.format("Resetting time today for project '%s' (previous time: %s)",
					this.getTitle(),
					JTimeSchedFrame.formatSeconds(this.getSecondsToday())));
			
			this.resetToday();
		}
	}
	
	public void resetOverall() {
		this.secondsOverall = 0;
	}
	
	public void resetToday() {
		this.secondsToday = 0;
		
		// reset time-started
		this.timeStart = new Date();
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
		return String.format("Project [title=%s, running=%s, secondsOverall=%d, secondsToday=%d]",
				title, (running) ? "yes" : "no", secondsOverall, secondsToday);
	}
}
