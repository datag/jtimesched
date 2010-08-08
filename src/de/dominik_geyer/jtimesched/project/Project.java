package de.dominik_geyer.jtimesched.project;

import java.awt.Color;
import java.util.Date;


public class Project {
	private String title;
	private Date timeCreated;
	private Color color;
	private boolean checked;
	
	private int secondsOverall;
	private int secondsToday;
	
	private boolean running;
	private Date timeStart;
	
	public Project() {
		this("project");
	}
	
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
	
	public Date getTimeStart() {
		return timeStart;
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

	void setTimeCreated(Date timeCreated) {
		this.timeCreated = timeCreated;
	}

	void setRunning(boolean running) {
		this.running = running;
	}

	void setTimeStart(Date timeStart) {
		this.timeStart = timeStart;
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
	
	public void resetOverall() {
		this.secondsOverall = 0;
	}
	
	public void resetToday() {
		this.secondsToday = 0;
		
		// reset time-started
		this.timeStart = new Date();
	}
	
	@Override
	public String toString() {
		return String.format("Project [title=%s, running=%s, secondsOverall=%d, secondsToday=%d, checked=%s]",
				title,
				(running) ? "yes" : "no",
				secondsOverall, secondsToday,
				(checked) ? "yes" : "no");
	}
}
