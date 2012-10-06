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
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;


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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
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
	private static Image trayDefaultImage;
	private static Image trayRunningImage;
	private MenuItem itemToggleProject;
	
	private ProjectTable tblSched;
	private JLabel lblOverall;
	private JTextField tfHighlight;
	
	private static final int LOGAREA_HEIGHT = 100;
	private JScrollPane spLog;
	private JTextArea tfLog = new JTextArea();
	private JToggleButton btnLogToggle;
	
	private ArrayList<Project> arPrj = new ArrayList<Project>();
	private Project currentProject;
	
	private Timer saveTimer;
	
	private boolean initiallyVisible = true;
	
	public JTimeSchedFrame() {
		super("jTimeSched");
		
		JTimeSchedFrame.trayDefaultImage = JTimeSchedFrame.getImage("jtimesched-inactive.png");
		JTimeSchedFrame.trayRunningImage = JTimeSchedFrame.getImage("jtimesched-active.png");
		
		// FIXME: native size
		this.setIconImage(JTimeSchedFrame.trayDefaultImage);
		this.setPreferredSize(new Dimension(600, 200));
		this.setMinimumSize(new Dimension(520, 150));
		
		
		// create tray-icon and set default close-behavior
		if (this.setupTrayIcon())
			this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		else
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		// add handler for GUI log
		JTimeSchedApp.getLogger().addHandler(new JTimeSchedGUILogHandler(this.tfLog));
		
		
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
			
			
			// create a backup of project-file
			// Note: Path.copyTo() of NIO is only available in >=JDK7
			FileInputStream fis = null;
	    	FileOutputStream fos = null;
			try {
				fis  = new FileInputStream(file);
		    	fos = new FileOutputStream(new File(JTimeSchedApp.PRJ_FILE + ".backup"));
		    	
		        byte[] buf = new byte[1024];
		        int i = 0;
		        while ((i = fis.read(buf)) != -1) {
		            fos.write(buf, 0, i);
		        }
		        fis.close();
		        fos.close();
			} catch (Exception e) {
		    	e.printStackTrace();
		    	JTimeSchedApp.getLogger().warning("Unable to create backup of project file.");
		    }
		}
		
		
		// create model an associate data
		ProjectTableModel tstm = new ProjectTableModel(this.arPrj);
		
		// create table
		this.tblSched = new ProjectTable(this, tstm);
		
		// listen on table-clicks
		this.tblSched.addMouseListener(new TimeSchedTableMouseListener());
		this.tblSched.getTableHeader().addMouseListener(new TimeSchedTableHeaderMouseListener());
		
		this.tblSched.addKeyListener(new TimeSchedTableKeyListener());
		
		// add table to a scroll-pane
		JScrollPane spSched = new JScrollPane(this.tblSched);
		this.add(spSched, BorderLayout.CENTER);

		
		
		// bottom panel
		JPanel panelBottom = new JPanel();
		panelBottom.setLayout(new BoxLayout(panelBottom, BoxLayout.LINE_AXIS));
		JButton btnAdd = new JButton("Add project", JTimeSchedFrame.getImageIcon("project-add.png"));
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
		panelBottom.add(Box.createRigidArea(new Dimension(5, 0)));
		
		// log toggle button
		this.btnLogToggle = new JToggleButton(JTimeSchedFrame.getImageIcon("log-toggle.png"));
		this.btnLogToggle.setToolTipText("toggle log area");
		this.btnLogToggle.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Boolean isVisible = spLog.isVisible();
				setSize(getWidth(), getHeight() + JTimeSchedFrame.LOGAREA_HEIGHT * (isVisible ? -1 : 1));
				spLog.setVisible(!isVisible);
				spLog.doLayout();
				doLayout();
			}}
		);
		panelBottom.add(this.btnLogToggle);
		
		
		// logging area
		this.tfLog.setEditable(false);
		//this.tfLog.setFont(this.tfLog.getFont().deriveFont(10.0f));
		this.spLog = new JScrollPane(this.tfLog);
		this.spLog.setVisible(false);
		this.spLog.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this.spLog.setPreferredSize(new Dimension(100 /* ignored */, JTimeSchedFrame.LOGAREA_HEIGHT));
		
		
		// the whole bottom panel
		JPanel panelBottomAll = new JPanel(new BorderLayout());
		panelBottomAll.add(panelBottom, BorderLayout.NORTH);
		panelBottomAll.add(this.spLog, BorderLayout.SOUTH);
		this.add(panelBottomAll, BorderLayout.SOUTH);
		
		
		// load settings
		file = new File(JTimeSchedApp.SETTINGS_FILE);
		if (file.isFile()) {
			try {
				this.loadSettings();
			} catch (Exception e) {
				JTimeSchedApp.getLogger().warning("Error loading settings, running with defaults.");
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
		this.tblSched.requestFocusInWindow();
		this.setVisible(this.initiallyVisible);
	}
	
	public static URL getImageResource(String image) {
		return JTimeSchedFrame.class.getResource("/" + JTimeSchedApp.IMAGES_PATH + image);
	}
	
	public static Image getImage(String image) {
		return Toolkit.getDefaultToolkit().getImage(JTimeSchedFrame.getImageResource(image));
	}
	
	public static ImageIcon getImageIcon(String image) {
		return new ImageIcon(JTimeSchedFrame.getImageResource(image));
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
		
		// current image
		Image currentIcon = (running) ? JTimeSchedFrame.trayRunningImage : JTimeSchedFrame.trayDefaultImage;
		
		
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
			
			// escape ampersand-character on windows
			if (System.getProperty("os.name").startsWith("Windows")) {
				strTray = strTray.replaceAll("&", "&&&");
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
	
	protected void updateTrayCurrentProject() {
		if (this.currentProject == null) {
			itemToggleProject.setLabel("Toggle project");
			itemToggleProject.setEnabled(false);
		} else {
			this.itemToggleProject.setLabel(
					(this.currentProject.isRunning()?"Pause":"Start") +
					" \""+this.currentProject.getTitle()+"\"");
			itemToggleProject.setEnabled(true);
		}
	}
	
	public void handleStartPause(Project prj) {
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
			
			this.currentProject = prj;
			this.updateTrayCurrentProject();
			
		} catch (ProjectException ex) {
			ex.printStackTrace();
		}
		
		// update table
		this.updateGUI();
	}
	
	
	public void handleDelete(ProjectTableModel tstm, Project prj, int modelRow) {
//		int response = JOptionPane.showConfirmDialog(
//				this,
//				"Remove project \"" + prj.getTitle() + "\" from list?",
//				"Remove project?",
//				JOptionPane.YES_NO_OPTION);
//		
//		if (response != JOptionPane.YES_OPTION)
//			return;
		
		if (this.currentProject == prj)
			this.currentProject = null;
		
		tstm.removeProject(modelRow);
		
		this.updateTrayCurrentProject();
		this.updateStatsLabel();
	}
	
	
	public void handleNewButton() {
		Project prj = new Project("New project");
		
		ProjectTableModel tstm = (ProjectTableModel)this.tblSched.getModel();
		tstm.addProject(prj);
		
		
		// get recently added row (view index)
		int viewRow = this.tblSched.convertRowIndexToView(tstm.getRowCount() - 1);
		int viewColumn = this.tblSched.convertColumnIndexToView(ProjectTableModel.COLUMN_TITLE);
		
		// start editing cell
		this.tblSched.editCellAt(viewRow, viewColumn);
		
		// scroll to row/cell
		this.tblSched.changeSelection(viewRow, viewColumn, false, false);

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
				JTimeSchedApp.getLogger().info(String.format("Resetting project '%s' (previous time: %s; checked: %s)",
						p.getTitle(),
						ProjectTime.formatSeconds(p.getSecondsToday()),
						(p.isChecked() ? "yes" : "no")));
				
				p.resetToday();			// reset time today
				p.setChecked(false);	// uncheck project
			}
		}
	}
	
	
	public boolean setupTrayIcon() {
		if (SystemTray.isSupported()) {
			SystemTray tray = SystemTray.getSystemTray();
			
			ActionListener aboutListener = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JOptionPane.showMessageDialog(null,
							"<html><big>jTimeSched</big><br/>Version " +
								JTimeSchedApp.APP_VERSION + "<br/><br/>" +
								"written by Dominik D. Geyer<br/>" +
								"&lt;code@dominik-geyer.de&gt;<br/><br/>" +
								"released under the GPLv3 license</html>",
							"About jTimeSched",
							JOptionPane.INFORMATION_MESSAGE,
							new ImageIcon(JTimeSchedFrame.trayRunningImage));
				}
			};

			ActionListener exitListener = new ActionListener() {
				@Override
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
			
			ActionListener toggleProjectListener = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (JTimeSchedFrame.this.currentProject != null) {
						handleStartPause(JTimeSchedFrame.this.currentProject);
					}
				}
			};


			PopupMenu popup = new PopupMenu();

			MenuItem itemAbout = new MenuItem("About...");
			itemAbout.addActionListener(aboutListener);
			popup.add(itemAbout);

			popup.addSeparator();
			
			itemToggleProject = new MenuItem("Toggle project");
			itemToggleProject.addActionListener(toggleProjectListener);
			itemToggleProject.setEnabled(false);
			popup.add(itemToggleProject);

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
			
			Boolean logVisible = in.readBoolean();
			this.spLog.setVisible(logVisible);
			this.btnLogToggle.setSelected(logVisible);
			
			int sortColumn = in.readInt();
			boolean sortAsc = in.readBoolean();
			List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
			sortKeys.add(new RowSorter.SortKey(sortColumn, sortAsc ? SortOrder.ASCENDING : SortOrder.DESCENDING));
			this.tblSched.getRowSorter().setSortKeys(sortKeys);
		} catch(IOException ex) {
			throw ex;
		} catch(ClassNotFoundException ex) {
			throw ex;
		} finally {
			in.close();
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
			out.writeBoolean(this.spLog.isVisible());
			
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
			if (tblSched.getRowCount() == 0)
				return;
			
			int selRow = tblSched.rowAtPoint(e.getPoint());
			int selColumn = tblSched.columnAtPoint(e.getPoint());
			
			if (selRow == -1 || selColumn == -1)
				return;
			
			int row = tblSched.convertRowIndexToModel(selRow);
			int column = tblSched.convertColumnIndexToModel(selColumn);
			
			ProjectTableModel tstm = (ProjectTableModel) tblSched.getModel();
			Project prj = tstm.getProjectAt(row);
			
			int button = e.getButton();
			if (button == MouseEvent.BUTTON1) {	// left button
				switch (column) {
				case ProjectTableModel.COLUMN_ACTION_DELETE:
					if (e.getClickCount() == 2)
						handleDelete(tstm, prj, row);
					break;
				case ProjectTableModel.COLUMN_ACTION_STARTPAUSE:
					handleStartPause(prj);
					break;
				}
			} else if (button == MouseEvent.BUTTON2) {	// middle button
				handleStartPause(prj);
			} else if (button == MouseEvent.BUTTON3) {	// right button
				switch (column) {
				case ProjectTableModel.COLUMN_TIMEOVERALL:
				case ProjectTableModel.COLUMN_TIMETODAY:
					String input = JOptionPane.showInputDialog(JTimeSchedFrame.this,
							"Enter new quota for time " +
								(column == ProjectTableModel.COLUMN_TIMEOVERALL ? "overall" : "today") + ":",
							ProjectTime.formatSeconds(
									(column == ProjectTableModel.COLUMN_TIMEOVERALL) ? prj.getQuotaOverall() : prj.getQuotaToday()));
					
					if (input != null) {
						int newSeconds = 0;
						try {
							if (!input.isEmpty())
								newSeconds = ProjectTime.parseSeconds(input);
							
							if (column == ProjectTableModel.COLUMN_TIMEOVERALL)
								prj.setQuotaOverall(newSeconds);
							else
								prj.setQuotaToday(newSeconds);
							
							tstm.fireTableRowsUpdated(row, row);
						} catch (ParseException pe) {
							JOptionPane.showMessageDialog(JTimeSchedFrame.this,
									"Invalid seconds-string, keeping previous value.",
									"Invalid input",
									JOptionPane.ERROR_MESSAGE);
						}
					}
					break;
				case ProjectTableModel.COLUMN_TITLE:
					NotesDialog dialog = new NotesDialog(JTimeSchedFrame.this, prj.getNotes());
					dialog.setVisible(true);
					
					if (dialog.isConfirmed()) {
						prj.setNotes(dialog.getInputText());
					}
					
					break;
				}
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
			
			JPopupMenu popup = new JPopupMenu();
			
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
				
				JMenuItem itemCheck = new JMenuItem("Check all");
				itemCheck.addActionListener(new CheckActionListener(true));
				JMenuItem itemUncheck = new JMenuItem("Uncheck all");
				itemUncheck.addActionListener(new CheckActionListener(false));
				
				popup.add(itemCheck);
				popup.add(itemUncheck);
				popup.show(e.getComponent(), e.getX(), e.getY());

				break;

			case ProjectTableModel.COLUMN_ACTION_DELETE:
				JMenuItem itemDelete = new JMenuItem("Delete all");
				itemDelete.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						ProjectTableModel ptm = (ProjectTableModel) JTimeSchedFrame.this.tblSched.getModel();
						
						// make use of table model's removeProject method
						while (ptm.getRowCount() > 0) {
							ptm.removeProject(0);
						}
						JTimeSchedFrame.this.currentProject = null;
						
						JTimeSchedFrame.this.updateStatsLabel();
						JTimeSchedFrame.this.updateTrayCurrentProject();
					}
				});
				
				popup.add(itemDelete);
				popup.show(e.getComponent(), e.getX(), e.getY());
				
				break;
				
			case ProjectTableModel.COLUMN_COLOR:
				JMenuItem itemClear = new JMenuItem("Clear all");
				itemClear.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						for (Project p: arPrj) {
							p.setColor(null);
						}
						updateSchedTable();
					}
				});
				
				popup.add(itemClear);
				popup.show(e.getComponent(), e.getX(), e.getY());

				break;
			}
		}
	}
	
	class TimeSchedTableKeyListener implements KeyListener {
		@Override
		public void keyPressed(KeyEvent e) {
			int keyCode = e.getKeyCode();
			
			if (keyCode == KeyEvent.VK_INSERT) {
				handleNewButton();
				e.consume();
				return;
			}
			
			int selRow = tblSched.getSelectedRow();
			if (selRow == -1)
				return;
			
			int row = tblSched.convertRowIndexToModel(selRow);
			
			ProjectTableModel ptm = (ProjectTableModel) tblSched.getModel();
			Project p = ptm.getProjectAt(row);
			
			switch (keyCode) {
			case KeyEvent.VK_SPACE:
				handleStartPause(p);
				e.consume();
				break;
			case KeyEvent.VK_DELETE:
				handleDelete(ptm, p, row);
				e.consume();
				break;
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {}

		@Override
		public void keyTyped(KeyEvent e) {}
	}
	
	class JTimeSchedGUILogHandler extends Handler {
		private JTextArea logArea;
		
		public JTimeSchedGUILogHandler(JTextArea ta) {
			this.logArea = ta;
		}
		
		@Override
		public void close() throws SecurityException {}

		@Override
		public void flush() {}

		@Override
		public void publish(LogRecord lr) {
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			String line = String.format("%s %s%n",
					sdf.format(new Date(lr.getMillis())),
					lr.getMessage());
			this.logArea.append(line);
		}
	}
}
