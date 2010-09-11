package de.dominik_geyer.jtimesched.gui;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.RowSorter.SortKey;


import de.dominik_geyer.jtimesched.JTimeSchedApp;
import de.dominik_geyer.jtimesched.gui.table.ProjectTable;
import de.dominik_geyer.jtimesched.project.Project;
import de.dominik_geyer.jtimesched.project.ProjectException;
import de.dominik_geyer.jtimesched.project.ProjectSerializer;
import de.dominik_geyer.jtimesched.project.ProjectTableModel;
import de.dominik_geyer.jtimesched.project.ProjectTime;


@SuppressWarnings("serial")
public class JTimeSchedFrame extends JFrame {
	
	
	private TrayIcon trayIcon;
	private boolean runningState = false;
	private static final Image trayDefaultImage = Toolkit.getDefaultToolkit().getImage(JTimeSchedApp.IMAGES_PATH + "jtimesched-inactive.png");
	private static final Image trayRunningImage = Toolkit.getDefaultToolkit().getImage(JTimeSchedApp.IMAGES_PATH + "jtimesched-active.png");
	
	private ProjectTable tblSched;
	private JLabel lblOverall;
	private JTextField tfHighlight;
	
	private ArrayList<Project> arPrj = new ArrayList<Project>();
	private Timer saveTimer;
	
	private boolean initiallyVisible = true;
	
	public JTimeSchedFrame() {
		super("jTimeSched (" + JTimeSchedApp.APP_VERSION + ")");
		
		this.setIconImage(JTimeSchedFrame.trayDefaultImage);
		this.setPreferredSize(new Dimension(600, 200));
		this.setMinimumSize(new Dimension(480, 150));
		
		
		// create tray-icon and set default close-behavior
		if (this.setupTrayIcon())
			this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		else
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		// load project-file
		File file = new File(JTimeSchedApp.PRJ_FILE);
		if (file.isFile()) {
			try {
				ProjectSerializer ps = new ProjectSerializer(JTimeSchedApp.PRJ_FILE);
				this.arPrj = ps.readXml();
				
				// check all projects for a today-time reset
				checkResetToday();
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this,
						"An error occurred while loading the project data:\n" +
						e.getMessage() + "\n\n" +
						"Please correct or remove the file '" + JTimeSchedApp.PRJ_FILE + "'. " +
						"JTimeSched will quit now to avoid data corruption.",
						"Error loading project data",
						JOptionPane.ERROR_MESSAGE);
				
				System.exit(1);
			}
		}
		
		
		// create model an associate data
		ProjectTableModel tstm = new ProjectTableModel(this.arPrj);
		
		// create table
		this.tblSched = new ProjectTable(this, tstm);
		
		// listen on table-clicks
		this.tblSched.addMouseListener(new TimeSchedTableMouseListener());
		this.tblSched.getTableHeader().addMouseListener(new TimeSchedTableHeaderMouseListener());
		

		// add table to a scroll-pane
		JScrollPane spSched = new JScrollPane(this.tblSched);
		this.add(spSched, BorderLayout.CENTER);

		
		
		// bottom panel
		JPanel panelBottom = new JPanel();
		panelBottom.setLayout(new BoxLayout(panelBottom, BoxLayout.LINE_AXIS));
		JButton btnAdd = new JButton("Add project", new ImageIcon(JTimeSchedApp.IMAGES_PATH + "project-add.png"));
		btnAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				handleNewButton();
			}
		});
		panelBottom.add(btnAdd);
		
		
		// bottom panel
		panelBottom.add(Box.createRigidArea(new Dimension(10, 0)));
		panelBottom.add(Box.createHorizontalGlue());
		this.lblOverall = new JLabel("", SwingConstants.CENTER);
		this.lblOverall.setFont(this.lblOverall.getFont().deriveFont(Font.PLAIN));
		panelBottom.add(this.lblOverall);
		panelBottom.add(Box.createHorizontalGlue());
		panelBottom.add(Box.createRigidArea(new Dimension(10, 0)));
		
		
		// highlight editbox
		this.tfHighlight = new JTextField(6);
		this.tfHighlight.setToolTipText("highlight expression");
		this.tfHighlight.addFocusListener(new FocusListener(){
			@Override
			public void focusGained(FocusEvent fe) {
				tfHighlight.selectAll();
			}

			@Override
			public void focusLost(FocusEvent fe) {}
		});
		this.tfHighlight.addKeyListener(new KeyListener(){
			@Override
			public void keyPressed(KeyEvent ke) {}

			@Override
			public void keyReleased(KeyEvent ke) {
				tblSched.setHighlightString(tfHighlight.getText());
				tblSched.repaint();
			}

			@Override
			public void keyTyped(KeyEvent ke) {}
		});
		
		Dimension sizeTf = new Dimension(100, this.tfHighlight.getMinimumSize().height);
		this.tfHighlight.setMaximumSize(sizeTf);
		this.tfHighlight.setMaximumSize(sizeTf);
		panelBottom.add(this.tfHighlight);
		
		this.add(panelBottom, BorderLayout.SOUTH);
		
		
		
		// load settings
		file = new File(JTimeSchedApp.SETTINGS_FILE);
		if (file.isFile()) {
			try {
				this.loadSettings();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		// setup projects-save timer, interval 60 seconds
		saveTimer = new Timer(60*1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				saveProjects();
			}
		});
		
		saveTimer.setRepeats(true);
		saveTimer.start();
		
		
		// setup GUI update timer
		Timer timer = new Timer(1*1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				updateGUI();
			}
		});
		
		//timer.setInitialDelay(0);
		timer.setRepeats(true);
		timer.start();
		
		
		// initially refresh GUI values
		// Note: we need to do the initial update before the pack(), so
		// timer.setInitialDelay(0) for the update timer isn't enough
		this.updateGUI();
		
		
		this.pack();
		
		
		this.setVisible(this.initiallyVisible);
	}
	
	
	protected void updateGUI() {
		this.updateSchedTable();
		this.updateStatsLabel();
		this.updateAppIcons();
	}
	
	protected void updateAppIcons() {
		boolean running = false;
		Project runningProject = null;
		
		for (Project p: this.arPrj) {
			if (p.isRunning()) {
				running = true;
				runningProject = p;
				break;
			}
		}		
		
		
		Image currentIcon;
		if (running)
			currentIcon = JTimeSchedFrame.trayRunningImage;
		else
			currentIcon = JTimeSchedFrame.trayDefaultImage;
		
		
		// update frame-icon
		if (this.runningState != running) {
				this.setIconImage(currentIcon);
		}
		
		
		// update system-tray
		if (SystemTray.isSupported()) {
			String strTray = "jTimeSched";
			
			if (running) {
				strTray += String.format(" - %s %s",
						runningProject.getTitle(),
						ProjectTime.formatSeconds(runningProject.getSecondsToday()));
			}
			
			this.trayIcon.setToolTip(strTray);
			
			
			// only update tray-icon on change
			if (this.runningState != running) {
				this.trayIcon.setImage(currentIcon);
			}
		}
		
		this.runningState = running;
	}


	protected void updateSchedTable() {
		ProjectTableModel tstm = (ProjectTableModel)tblSched.getModel();

		int rowCount = tstm.getRowCount();
		if (rowCount > 0)
			tstm.fireTableRowsUpdated(0, rowCount -1);
	}
	
	protected void updateStatsLabel() {
		int projectCount = this.arPrj.size();
		
		// bottom stats label
		String strStats = ""/*"no projects"*/;
		if (projectCount > 0) {
			int timeOverall = 0;
			int timeToday = 0;
			for (Project p: this.arPrj) {
				timeOverall += p.getSecondsOverall();
				timeToday += p.getSecondsToday();
			}

			strStats = String.format("%d project%s | %s overall | %s today",
					projectCount,
					(projectCount == 1) ? "" : "s",
							ProjectTime.formatSeconds(timeOverall),
							ProjectTime.formatSeconds(timeToday));
		}

		this.lblOverall.setText(strStats);
	}
	
	
	public void handleStartPause(ProjectTableModel tstm, Project prj, int row, int column) {
		JTimeSchedApp.getLogger().info(String.format("%s project '%s' (time overall: %s, time today: %s)",
				(prj.isRunning()) ? "Pausing" : "Starting",
				prj.getTitle(),
				ProjectTime.formatSeconds(prj.getSecondsOverall()),
				ProjectTime.formatSeconds(prj.getSecondsToday())));
		
		try {
			if (prj.isRunning()) {
				prj.pause();
			} else {
				// pause all other projects
				for (Project p: this.arPrj) {
					if (p.isRunning()) {
						p.pause();
					}
				}
				
				// set project to run-state
				prj.start();
			}
		} catch (ProjectException ex) {
			ex.printStackTrace();
		}
		
		// update table
		this.updateGUI();
	}
	
	
	public void handleDelete(ProjectTableModel tstm, Project prj, int row, int column) {
//		int response = JOptionPane.showConfirmDialog(
//				this,
//				"Remove project \"" + prj.getTitle() + "\" from list?",
//				"Remove project?",
//				JOptionPane.YES_NO_OPTION);
//		
//		if (response != JOptionPane.YES_OPTION)
//			return;
		
		tstm.removeProject(row);
		
		this.updateStatsLabel();
	}
	
	
	public void handleNewButton() {
		Project prj = new Project("New project");
		
		ProjectTableModel tstm = (ProjectTableModel)this.tblSched.getModel();
		tstm.addProject(prj);
		
		
		// get recently added row (view index)
		int row = this.tblSched.convertRowIndexToView(tstm.getRowCount() - 1);
		
		// start editing cell
		this.tblSched.editCellAt(row, ProjectTableModel.COLUMN_TITLE);
		
		// scroll to row/cell
		this.tblSched.changeSelection(row, ProjectTableModel.COLUMN_TITLE, false, false);

		// select all text if component is a textfield
		Component ec = this.tblSched.getEditorComponent();
		if (ec != null) {
			if (ec instanceof JTextField) {
				JTextField tf = (JTextField) ec;
				tf.selectAll();
			}
			
			// set input focus on edit-cell
			ec.requestFocusInWindow();
		}
		
		this.updateStatsLabel();
	}
	
	
	protected void checkResetToday() {
		for (Project p: this.arPrj) {
			Date currentTime = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("y-MMM-d");
			String strCurrentDay = sdf.format(currentTime);
			String strStartDay = sdf.format(p.getTimeStart());
			
			if (!strCurrentDay.equals(strStartDay)) {
				JTimeSchedApp.getLogger().info(String.format("Resetting time today for project '%s' (previous time: %s)",
						p.getTitle(),
						ProjectTime.formatSeconds(p.getSecondsToday())));
				
				p.resetToday();
			}
		}
	}
	
	
	public boolean setupTrayIcon() {
		if (SystemTray.isSupported()) {
			SystemTray tray = SystemTray.getSystemTray();
			
			ActionListener aboutListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					JOptionPane.showMessageDialog(null,
							"jTimeSched\nVersion " +
								JTimeSchedApp.APP_VERSION + "\n\n" +
								"written by Dominik D. Geyer\n" +
								"<devel@dominik-geyer.de>\n\n" +
								"released under the GPLv3 license",
							"About jTimeSched",
							JOptionPane.INFORMATION_MESSAGE,
							new ImageIcon(JTimeSchedFrame.trayDefaultImage));
				}
			};

			ActionListener exitListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
					// save projects
					saveTimer.stop();
					saveProjects();
					
					// store settings
					try {
						JTimeSchedFrame.this.saveSettings();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					
					JTimeSchedFrame.this.setVisible(false);
					JTimeSchedFrame.this.dispose();
					
					if (SystemTray.isSupported())
						SystemTray.getSystemTray().remove(JTimeSchedFrame.this.trayIcon);
					
					System.exit(0);
				}
			};



			PopupMenu popup = new PopupMenu();

			MenuItem itemAbout = new MenuItem("About...");
			itemAbout.addActionListener(aboutListener);
			popup.add(itemAbout);

			popup.addSeparator();

			MenuItem itemExit = new MenuItem("Exit");
			itemExit.addActionListener(exitListener);
			popup.add(itemExit);


			trayIcon = new TrayIcon(JTimeSchedFrame.trayDefaultImage, "jTimeSched", popup);

			ActionListener actionListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//		            trayIcon.displayMessage("Action Event", 
					//		                "An Action Event Has Been Performed!",
					//		                TrayIcon.MessageType.INFO);
					
					
					// FIXME: bring to front if not foreground-window
					// isActive() doesn't work on MS-Windows because click
					// into tray steels the focus.
					if (!isVisible() /*|| !isActive()*/)
						setVisible(true);
					else
						setVisible(false);
				}
			};

			trayIcon.setImageAutoSize(true);
			trayIcon.addActionListener(actionListener);
			//trayIcon.addMouseListener(mouseListener);

			try {
				tray.add(trayIcon);
			} catch (AWTException e) {
				System.err.println("TrayIcon could not be added.");
				return false;
			}
			
			return true;
		} else {
			//  System Tray is not supported
			System.err.println("TrayIcon is not supported.");
			return false;
		}

	}
	
	public void saveProjects() {
		try {
			ProjectSerializer ps = new ProjectSerializer(JTimeSchedApp.PRJ_FILE);
			ps.writeXml(JTimeSchedFrame.this.arPrj);
		} catch (Exception e) {
			JTimeSchedApp.getLogger().severe("Error saving project file: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void loadSettings() throws Exception {
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream(JTimeSchedApp.SETTINGS_FILE);
			in = new ObjectInputStream(fis);
			
			/*String appVersion =*/ in.readUTF();	// app-version
			
			Dimension size = (Dimension) in.readObject();
			this.setSize(size);
			this.setPreferredSize(size);
			
			this.setLocation((Point) in.readObject());
			this.initiallyVisible = in.readBoolean();
			
			int sortColumn = in.readInt();
			boolean sortAsc = in.readBoolean();
			List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
			sortKeys.add(new RowSorter.SortKey(sortColumn, sortAsc ? SortOrder.ASCENDING : SortOrder.DESCENDING));
			this.tblSched.getRowSorter().setSortKeys(sortKeys);
			
			in.close();
		} catch(IOException ex) {
			throw ex;
		} catch(ClassNotFoundException ex) {
			throw ex;
		}
	}
	
	
	public void saveSettings() throws Exception {
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try	{
			fos = new FileOutputStream(JTimeSchedApp.SETTINGS_FILE);
			out = new ObjectOutputStream(fos);
			
			out.writeUTF(JTimeSchedApp.APP_VERSION);
			out.writeObject(this.getSize());
			out.writeObject(this.getLocation());
			out.writeBoolean(this.isVisible());
			
			List<? extends SortKey> sortKeys =  this.tblSched.getRowSorter().getSortKeys();
			RowSorter.SortKey sortKey = sortKeys.get(0);
			out.writeInt(sortKey.getColumn());
			boolean sortAsc = (sortKey.getSortOrder() == SortOrder.ASCENDING) ? true : false;
			out.writeBoolean(sortAsc);
			
			out.close();
		} catch(IOException ex) {
			throw ex;
		}
	}
	
	
	class TimeSchedTableMouseListener extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			//int selRow = tblSched.getSelectedRow();  // detects click into empty space -> not wanted
			int selRow = tblSched.rowAtPoint(e.getPoint());
			
			
			if (selRow == -1)
				return;
			
			int row = tblSched.convertRowIndexToModel(selRow);
			int column = tblSched.convertColumnIndexToModel(tblSched.getSelectedColumn());
			
			//System.out.println("clicked cell: " + row + ":" + column);
			
			ProjectTableModel tstm = (ProjectTableModel) tblSched.getModel();
			
			if (tblSched.getRowCount() == 0 || tblSched.getSelectedRow() == -1)
				return;
			
			Project prj = tstm.getProjectAt(row);
			System.out.println(prj);
			
			switch (column) {
			case ProjectTableModel.COLUMN_ACTION_DELETE:
				if (e.getClickCount() == 2)
					handleDelete(tstm, prj, row, column);
				break;
			case ProjectTableModel.COLUMN_ACTION_STARTPAUSE:
				handleStartPause(tstm, prj, row, column);
				break;
			}
		}
	}
	
	class TimeSchedTableHeaderMouseListener extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			this.showPopup(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			this.showPopup(e);
		}

		private void showPopup(MouseEvent e) {
			if (!e.isPopupTrigger())
				return;

			Point p = e.getPoint();
			int selColumn = tblSched.getTableHeader().columnAtPoint(p);
			int column = tblSched.convertColumnIndexToModel(selColumn);

			switch (column) {
			case ProjectTableModel.COLUMN_CHECK:
				class CheckActionListener implements ActionListener {
					private boolean check;
					
					public CheckActionListener(boolean check) {
						this.check = check;
					}
					
					@Override
					public void actionPerformed(ActionEvent e) {
						for (Project p: arPrj) {
							p.setChecked(this.check);
						}
						updateSchedTable();
					}
				};
				
				JPopupMenu popup = new JPopupMenu();
				JMenuItem itemCheck = new JMenuItem("Check all");
				itemCheck.addActionListener(new CheckActionListener(true));
				JMenuItem itemUncheck = new JMenuItem("Uncheck all");
				itemUncheck.addActionListener(new CheckActionListener(false));
				
				popup.add(itemCheck);
				popup.add(itemUncheck);
				popup.show(e.getComponent(), e.getX(), e.getY());

				break;
			}
		}
	}

}
