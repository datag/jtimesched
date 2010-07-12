import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;



public class JTimeSchedFrame extends JFrame {
	private static final int COLUMN_ICON_WIDTH = 22;
	
	private TrayIcon trayIcon;
	private boolean trayRunningState = false;
	private final Image trayDefaultImage = Toolkit.getDefaultToolkit().getImage(JTimeSchedApp.IMAGES_PATH + "history.png");
	private final Image trayRunningImage = Toolkit.getDefaultToolkit().getImage(JTimeSchedApp.IMAGES_PATH + "history-active.png");
	
	private JTable tblSched;
	private JLabel lblOverall;
	private ArrayList<Project> arPrj = new ArrayList<Project>();
	
	public JTimeSchedFrame() {
		super("jTimeSched (" + JTimeSchedApp.APP_VERSION + ")");
		
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(JTimeSchedApp.IMAGES_PATH + "history.png"));
		
		this.setPreferredSize(new Dimension(600, 200));
		this.setMinimumSize(new Dimension(460, 150));
		
		
		// create tray-icon
		if (this.setupTrayIcon())
			this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		else
			this.addWindowListener(new JTimeSchedFrameWindowListener());
		
		
		// load project-file
		File file = new File(JTimeSchedApp.PRJ_FILE);
		if (file.isFile()) {
			try {
				this.arPrj = JTimeSchedApp.loadProjects();
				System.out.println(this.arPrj);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		// create model an associate data
		TimeSchedTableModel tstm = new TimeSchedTableModel(this.arPrj);
		
		// create table
		this.tblSched = new JTable(tstm);
		this.tblSched.setFillsViewportHeight(true);
		this.tblSched.setShowGrid(true);
		this.tblSched.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.tblSched.setAutoCreateRowSorter(true);
		this.tblSched.setRowHeight(JTimeSchedFrame.COLUMN_ICON_WIDTH);
		//this.tblSched.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); // not needed?
		this.tblSched.getTableHeader().setReorderingAllowed(false);
		
		// set a custom default cell-renderer 
		TableCellRenderer defaultRenderer = this.tblSched.getDefaultRenderer(Object.class);
		this.tblSched.setDefaultRenderer(Object.class,
				new TimeSchedTableCellRenderer(defaultRenderer));
		
		
		// define and set column properties
		int[][] columnWidths = new int[][] {
				{TimeSchedTableModel.COLUMN_TITLE,			200,	100,		-1},
				//{TimeSchedTableModel.COLUMN_PRIORITY,		-1,		80,		80},
				{TimeSchedTableModel.COLUMN_CREATED,		-1,		80,		80},
				{TimeSchedTableModel.COLUMN_TIMEOVERALL,	95,		60,		95},
				{TimeSchedTableModel.COLUMN_TIMETODAY,		95,		60,		95},
				{TimeSchedTableModel.COLUMN_ACTION_DELETE,		-1,		JTimeSchedFrame.COLUMN_ICON_WIDTH,	JTimeSchedFrame.COLUMN_ICON_WIDTH},
				{TimeSchedTableModel.COLUMN_ACTION_STARTPAUSE,	-1,		JTimeSchedFrame.COLUMN_ICON_WIDTH,	JTimeSchedFrame.COLUMN_ICON_WIDTH},
		};
		
		TableColumnModel tcm = this.tblSched.getColumnModel();
		for (int[] cw: columnWidths) {
			TableColumn tc = tcm.getColumn(cw[0]);
			
			if (cw[1] > 0)
				tc.setPreferredWidth(cw[1]);
			
			if (cw[2] > 0)
				tc.setMinWidth(cw[2]);
			
			if (cw[3] > 0)
				tc.setMaxWidth(cw[3]);	
		}
		
		// column specific cell-renderer
		tcm.getColumn(TimeSchedTableModel.COLUMN_CREATED).setCellRenderer(new CustomCellRenderer());
		tcm.getColumn(TimeSchedTableModel.COLUMN_TIMEOVERALL).setCellRenderer(new CustomCellRenderer());
		tcm.getColumn(TimeSchedTableModel.COLUMN_TIMETODAY).setCellRenderer(new CustomCellRenderer());
		tcm.getColumn(TimeSchedTableModel.COLUMN_ACTION_DELETE).setCellRenderer(new CustomCellRenderer());
		tcm.getColumn(TimeSchedTableModel.COLUMN_ACTION_STARTPAUSE).setCellRenderer(new CustomCellRenderer());
		
		
		// listen on table-clicks
		this.tblSched.addMouseListener(new TimeSchedTableMouseListener());

		// add table to a scroll-pane
		JScrollPane spSched = new JScrollPane(this.tblSched);
		this.add(spSched, BorderLayout.CENTER);

		
		
		// bottom panel
		JPanel panelBottom = new JPanel();
		panelBottom.setLayout(new BoxLayout(panelBottom, BoxLayout.LINE_AXIS));
		JButton btnAdd = new JButton("Add project", new ImageIcon(JTimeSchedApp.IMAGES_PATH + "history-add.png"));
		btnAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				handleNewButton();
			}
		});
		panelBottom.add(btnAdd);
		
		
		// bottom statistics label
		panelBottom.add(Box.createHorizontalGlue());
		this.lblOverall = new JLabel("", SwingConstants.RIGHT);
		panelBottom.add(this.lblOverall);
		this.add(panelBottom, BorderLayout.SOUTH);


		
		// setup GUI update timer
		Timer timer = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateGUI();
			}
		});
		
		timer.setRepeats(true);
		timer.start();
		
		
		// initially refresh GUI values
		this.updateGUI();
		
		
		this.pack();
	}
	
	protected void updateGUI() {
		this.updateSchedTable();
		this.updateStatsLabel();
		
		
		// update system-tray
		if (SystemTray.isSupported()) {
			String strTray = "jTimeSched";
			boolean running = false;
			for (Project p: this.arPrj) {
				if (p.isRunning()) {
					running = true;
					strTray += " - " + p.getTitle() + " " + this.formatSeconds(p.getSecondsToday());
					break;
				}
			}
			
			this.trayIcon.setToolTip(strTray);
			
			
			// only update tray-icon on change
			if (this.trayRunningState != running) {
				if (running)
					this.trayIcon.setImage(this.trayRunningImage);
				else
					this.trayIcon.setImage(this.trayDefaultImage);
				
				this.trayRunningState = running;
			}
		}
	}
	
	protected void updateSchedTable() {
		TimeSchedTableModel tstm = (TimeSchedTableModel)tblSched.getModel();

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
							this.formatSeconds(timeOverall),
							this.formatSeconds(timeToday));
		}

		this.lblOverall.setText(strStats + " ");
	}
	
	
	public String formatSeconds(int s) {
		return String.format("%d:%02d:%02d", s/3600, (s%3600)/60, (s%60));
	}
	
	
	public void handleStartPause(TimeSchedTableModel tstm, Project prj, int row, int column) {
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
	
	
	public void handleDelete(TimeSchedTableModel tstm, Project prj, int row, int column) {
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
		Project prj = new Project("New project", ProjectPriority.MEDIUM);
		
		TimeSchedTableModel tstm = (TimeSchedTableModel)this.tblSched.getModel();
		tstm.addProject(prj);
		
		
		// get recently added row (view index)
		int row = this.tblSched.convertRowIndexToView(tstm.getRowCount() - 1);
		
		// start editing cell
		this.tblSched.editCellAt(row, TimeSchedTableModel.COLUMN_TITLE);
		
		// scroll to row/cell
		this.tblSched.changeSelection(row, TimeSchedTableModel.COLUMN_TITLE, false, false);

		// select all text if component is a textfield
		Component ec = this.tblSched.getEditorComponent();
		if (ec instanceof JTextField) {
			JTextField tf = (JTextField) ec;
			tf.selectAll();
		}
		
		// set input focus on edit-cell
		ec.requestFocusInWindow();
		
		
		this.updateStatsLabel();
	}
	
	
	public void handleTimeReset(Project prj, int row, int column) {
		int response = JOptionPane.showConfirmDialog(JTimeSchedFrame.this,
				"Reset " + (column == TimeSchedTableModel.COLUMN_TIMETODAY ? "today" : "overall") +  
				" time of project \"" + prj.getTitle() + "\"?",
				"Reset time?",
				JOptionPane.YES_NO_OPTION);
		
		if (response != JOptionPane.YES_OPTION)
			return;
		
		try {
			if (prj.isRunning())
				prj.pause();
			
			if (column == TimeSchedTableModel.COLUMN_TIMETODAY)
				prj.resetToday();
			else
				prj.resetOverall();
		} catch (ProjectException e1) {
			e1.printStackTrace();
		}
		
		
		((TimeSchedTableModel)tblSched.getModel()).fireTableRowsUpdated(row, row);
		
		updateStatsLabel();
	}
	
	
	public boolean setupTrayIcon() {
		if (SystemTray.isSupported()) {
			SystemTray tray = SystemTray.getSystemTray();
			
			ActionListener aboutListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					JOptionPane.showMessageDialog(null,
							"jTimeSched\nVersion " +
								JTimeSchedApp.APP_VERSION + "\n\n" +
								"written by Dominik D. Geyer\n\n" +
								"released under the GPLv3 license",
							"About jTimeSched",
							JOptionPane.INFORMATION_MESSAGE,
							new ImageIcon(JTimeSchedApp.IMAGES_PATH + "history.png"));
				}
			};

			ActionListener exitListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					cleanExit();
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


			trayIcon = new TrayIcon(this.trayDefaultImage, "jTimeSched", popup);

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

	public void cleanExit() {

		for (Project p: this.arPrj) {
			if (p.isRunning()) {
				try {
					p.pause();
				} catch (ProjectException e) {
					e.printStackTrace();
				}
			}
		}

		try {
			JTimeSchedApp.saveProjects(arPrj);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		
		if (SystemTray.isSupported())
			SystemTray.getSystemTray().remove(this.trayIcon);
		
		
		System.out.println("Exiting...");
		System.exit(0);
	}
	
	class JTimeSchedFrameWindowListener extends WindowAdapter {
		@Override
		public void windowClosing(WindowEvent e) {

			Window window = e.getWindow();
			window.setVisible(false);
			window.dispose();
			
			cleanExit();
		}
	}
	
	class CustomCellRenderer extends JLabel implements TableCellRenderer {
		public CustomCellRenderer() {
			this.setOpaque(true);
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			
			TimeSchedTableModel tstm = (TimeSchedTableModel) table.getModel();
			int modelRow = table.convertRowIndexToModel(row);
			Project prj = tstm.getProjectAt(modelRow);
			
			
			String text = null;
			
			switch (column) {
			case TimeSchedTableModel.COLUMN_TIMEOVERALL:
			case TimeSchedTableModel.COLUMN_TIMETODAY:
				text = formatSeconds(((Integer)value).intValue());
				this.setHorizontalAlignment(SwingConstants.RIGHT);
				this.setText(text);
				break;
			case TimeSchedTableModel.COLUMN_CREATED:
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd" /* HH:mm:ss */);
				text = sdf.format((Date)value);
				this.setHorizontalAlignment(SwingConstants.CENTER);
				this.setText(text);
				break;
			case TimeSchedTableModel.COLUMN_ACTION_DELETE:
				this.setIcon(new ImageIcon(JTimeSchedApp.IMAGES_PATH + "history-delete.png"));
				this.setHorizontalAlignment(SwingConstants.CENTER);
				break;
			case TimeSchedTableModel.COLUMN_ACTION_STARTPAUSE:
				ImageIcon ii;
				if (prj.isRunning())
					ii = new ImageIcon(JTimeSchedApp.IMAGES_PATH + "pause.png");
				else
					ii = new ImageIcon(JTimeSchedApp.IMAGES_PATH + "play.png");
				this.setIcon(ii);
				this.setHorizontalAlignment(SwingConstants.CENTER);
				break;
			}
			
			// row-color
			if (prj.isRunning()) {
				this.setFont(this.getFont().deriveFont(Font.BOLD));
				this.setBackground(new Color(0xFF, 0xE9, 0x7F));
			} else {
				this.setFont(this.getFont().deriveFont(Font.PLAIN));
				
				if (isSelected) {
					this.setBackground(table.getSelectionBackground());
				} else {
					this.setBackground(table.getBackground());
				}
			}
			
			return this;
		}
	}
	
	
	class TimeSchedTableCellRenderer implements TableCellRenderer {
		private TableCellRenderer defaultRenderer;

		public TimeSchedTableCellRenderer(TableCellRenderer renderer) {
			this.defaultRenderer = renderer;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			
			Component c = null;
			
			if (value instanceof Component)
				c = (Component) value;
			else {
				c = this.defaultRenderer.getTableCellRendererComponent(
		    		   table, value, isSelected, hasFocus, row, column);
			}
			
			int modelRow = table.convertRowIndexToModel(row);
			Project prj = ((TimeSchedTableModel)table.getModel()).getProjectAt(modelRow);
			if (prj.isRunning()) {
				c.setBackground(new Color(0xFF, 0xE9, 0x7F));
				c.setFont(c.getFont().deriveFont(Font.BOLD));
			} else {
				if (isSelected) {
					c.setBackground(table.getSelectionBackground());
				} else {
					c.setBackground(table.getBackground());
				}
			}
			
			if (value instanceof JLabel) {
				JLabel l = (JLabel) value;
				l.setOpaque(true);
			}
			
			return c;
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
			
			TimeSchedTableModel tstm = (TimeSchedTableModel) tblSched.getModel();
			
			if (tblSched.getRowCount() == 0 || tblSched.getSelectedRow() == -1)
				return;
			
			Project prj = tstm.getProjectAt(row);
			System.out.println(prj);
			
			switch (column) {
			case TimeSchedTableModel.COLUMN_ACTION_DELETE:
				if (e.getClickCount() == 2)
					handleDelete(tstm, prj, row, column);
				break;
			case TimeSchedTableModel.COLUMN_ACTION_STARTPAUSE:
				handleStartPause(tstm, prj, row, column);
				break;
			case TimeSchedTableModel.COLUMN_TIMEOVERALL:
			case TimeSchedTableModel.COLUMN_TIMETODAY:
				// reset time on double-click
				if (e.getClickCount() == 2)
					handleTimeReset(prj, row, column);
				break;
			}
		}
	}
}
