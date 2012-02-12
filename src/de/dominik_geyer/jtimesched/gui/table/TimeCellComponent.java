package de.dominik_geyer.jtimesched.gui.table;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import de.dominik_geyer.jtimesched.project.ProjectTime;

@SuppressWarnings("serial")
public class TimeCellComponent extends JLabel {
	private static final Color BAR_COLOR = new Color(80, 200, 80);
	private static final Color BAR_COLOR_OVERDUE = new Color(200, 50, 50);
	private static final int BAR_HEIGHT = 3;
	private static final int BAR_THRESHOLD = 1;
	
	private static final Color HASQUOTA_COLOR = new Color(50, 50, 200);
	private static final int HASQUOTA_HEIGHT = 3;
	
	private int time;
	private int quota;
	
	public TimeCellComponent(int time, int quota) {
		this.setOpaque(true);
		
		this.time = time;
		this.quota = quota;
		
		String text = ProjectTime.formatSeconds(time);
		this.setHorizontalAlignment(SwingConstants.RIGHT);
		this.setText(text);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if (this.quota > 0) {
			Color color = (this.time < this.quota)
				? TimeCellComponent.BAR_COLOR
				: TimeCellComponent.BAR_COLOR_OVERDUE;
			int width = (this.time < this.quota)
				? this.getWidth() * this.time / this.quota
				: this.getWidth();
			
			if (width > TimeCellComponent.BAR_THRESHOLD) {
				g.setColor(color);
				g.fillRect(0, 0, width, TimeCellComponent.BAR_HEIGHT);
			} else {
				// indicate quota if bar not yet visible
				g.setColor(TimeCellComponent.HASQUOTA_COLOR);
				g.fillRect(0, this.getHeight() - TimeCellComponent.HASQUOTA_HEIGHT - 1, TimeCellComponent.HASQUOTA_HEIGHT, TimeCellComponent.HASQUOTA_HEIGHT);
			}
		}
	}
}
