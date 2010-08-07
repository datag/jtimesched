package de.dominik_geyer.jtimesched.gui;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractCellEditor;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
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
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.RowSorter.SortKey;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import de.dominik_geyer.jtimesched.JTimeSchedApp;
import de.dominik_geyer.jtimesched.project.Project;
import de.dominik_geyer.jtimesched.project.ProjectException;
import de.dominik_geyer.jtimesched.project.ProjectTableModel;


@SuppressWarnings("serial")
public class JTimeSchedFrame extends JFrame {
	private static final int COLUMN_ICON_WIDTH = 22;
	private static final Color COLOR_RUNNING = new Color(0xFF, 0xE9, 0x7F);
	
	private TrayIcon trayIcon;
	private boolean runningState = false;
	private static final Image trayDefaultImage = Toolkit.getDefaultToolkit().getImage(JTimeSchedApp.IMAGES_PATH + "jtimesched-inactive.png");
	private static final Image trayRunningImage = Toolkit.getDefaultToolkit().getImage(JTimeSchedApp.IMAGES_PATH + "jtimesched-active.png");
	
	private JTable tblSched;
	private JLabel lblOverall;
	private ArrayList<Project> arPrj = new ArrayList<Project>();
	
	private boolean initiallyVisible = true;
	
	public JTimeSchedFrame() {
		super("jTimeSched (" + JTimeSchedApp.APP_VERSION + ")");
		
		
		// set shutdown hook
		Thread t = new Thread(new ShutdownActions());
		Runtime.getRuntime().addShutdownHook(t);
		
		
		this.setIconImage(JTimeSchedFrame.trayDefaultImage);
		this.setPreferredSize(new Dimension(600, 200));
		this.setMinimumSize(new Dimension(460, 150));
		
		
		// create tray-icon and set default close-behavior
		if (this.setupTrayIcon())
			this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		else
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		// load project-file
		File file = new File(JTimeSchedApp.PRJ_FILE);
		if (file.isFile()) {
			try {
				this.loadProjects();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		// create model an associate data
		ProjectTableModel tstm = new ProjectTableModel(this.arPrj);
		
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
		
		
		// set default sort-column
		List <RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
		sortKeys.add(new RowSorter.SortKey(ProjectTableModel.COLUMN_CREATED, SortOrder.ASCENDING));
		this.tblSched.getRowSorter().setSortKeys(sortKeys);
		
		
		// define and set column properties
		int[][] columnWidths = new int[][] {
				{ProjectTableModel.COLUMN_CHECK,			-1,		JTimeSchedFrame.COLUMN_ICON_WIDTH,	JTimeSchedFrame.COLUMN_ICON_WIDTH},
				{ProjectTableModel.COLUMN_TITLE,			200,	100,		-1},
				{ProjectTableModel.COLUMN_COLOR,			-1,		JTimeSchedFrame.COLUMN_ICON_WIDTH,	JTimeSchedFrame.COLUMN_ICON_WIDTH},
				{ProjectTableModel.COLUMN_CREATED,		-1,		80,		80},
				{ProjectTableModel.COLUMN_TIMEOVERALL,	95,		60,		95},
				{ProjectTableModel.COLUMN_TIMETODAY,		95,		60,		95},
				{ProjectTableModel.COLUMN_ACTION_DELETE,		-1,		JTimeSchedFrame.COLUMN_ICON_WIDTH,	JTimeSchedFrame.COLUMN_ICON_WIDTH},
				{ProjectTableModel.COLUMN_ACTION_STARTPAUSE,	-1,		JTimeSchedFrame.COLUMN_ICON_WIDTH,	JTimeSchedFrame.COLUMN_ICON_WIDTH},
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
		tcm.getColumn(ProjectTableModel.COLUMN_COLOR).setCellRenderer(new ColorCellRenderer());
		tcm.getColumn(ProjectTableModel.COLUMN_COLOR).setCellEditor(new ColorCellEditor());
		tcm.getColumn(ProjectTableModel.COLUMN_CREATED).setCellRenderer(new CustomCellRenderer());
		tcm.getColumn(ProjectTableModel.COLUMN_TIMEOVERALL).setCellRenderer(new CustomCellRenderer());
		tcm.getColumn(ProjectTableModel.COLUMN_TIMEOVERALL).setCellEditor(new TimeCellEditor());
		tcm.getColumn(ProjectTableModel.COLUMN_TIMETODAY).setCellRenderer(new CustomCellRenderer());
		tcm.getColumn(ProjectTableModel.COLUMN_TIMETODAY).setCellEditor(new TimeCellEditor());
		tcm.getColumn(ProjectTableModel.COLUMN_ACTION_DELETE).setCellRenderer(new CustomCellRenderer());
		tcm.getColumn(ProjectTableModel.COLUMN_ACTION_STARTPAUSE).setCellRenderer(new CustomCellRenderer());
		
		
		// listen on table-clicks
		this.tblSched.addMouseListener(new TimeSchedTableMouseListener());

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
		
		
		// bottom statistics label
		panelBottom.add(Box.createHorizontalGlue());
		this.lblOverall = new JLabel("", SwingConstants.RIGHT);
		this.lblOverall.setFont(this.lblOverall.getFont().deriveFont(Font.PLAIN));
		panelBottom.add(this.lblOverall);
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
						JTimeSchedFrame.formatSeconds(runningProject.getSecondsToday()));
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
							JTimeSchedFrame.formatSeconds(timeOverall),
							JTimeSchedFrame.formatSeconds(timeToday));
		}

		this.lblOverall.setText(strStats + " ");
	}
	
	
	public static String formatSeconds(int s) {
		return String.format("%d:%02d:%02d", s/3600, (s%3600)/60, (s%60));
	}
	
	public static int parseSeconds(String strTime) throws ParseException {
		 Pattern p = Pattern.compile("(\\d+):([0-5]?\\d):([0-5]?\\d)");	// 0:00:00
		 Matcher m = p.matcher(strTime);
		 
		 if (!m.matches())
			 throw new ParseException("Invalid seconds-string", 0);
		 
		 int hours = Integer.parseInt(m.group(1));
		 int minutes = Integer.parseInt(m.group(2));
		 int seconds = Integer.parseInt(m.group(3));
		 
		 return (hours * 3600 + minutes * 60 + seconds);
	}
	
	public void handleStartPause(ProjectTableModel tstm, Project prj, int row, int column) {
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
	
	class ShutdownActions implements Runnable {

		public void run() {
			for (Project p: JTimeSchedFrame.this.arPrj) {
				if (p.isRunning()) {
					try {
						p.pause();
					} catch (ProjectException e) {
						e.printStackTrace();
					}
				}
			}

			try {
				JTimeSchedFrame.this.saveProjects();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			
			System.out.println("Exiting...");
		}

	}
	
	
	@SuppressWarnings("unchecked")
	public void loadProjects() throws Exception {
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream(JTimeSchedApp.PRJ_FILE);
			in = new ObjectInputStream(fis);
			this.arPrj = (ArrayList<Project>) in.readObject();
			in.close();
			
			//System.out.println(arPrj);
		} catch(IOException ex) {
			throw ex;
		} catch(ClassNotFoundException ex) {
			throw ex;
		}
	}
	
	
	public void saveProjects() throws Exception {
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try	{
			fos = new FileOutputStream(JTimeSchedApp.PRJ_FILE);
			out = new ObjectOutputStream(fos);
			out.writeObject(this.arPrj);
			out.close();
		} catch(IOException ex) {
			throw ex;
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
	
	
	class CustomCellRenderer extends JLabel implements TableCellRenderer {
		public CustomCellRenderer() {
			this.setOpaque(true);
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			
			ProjectTableModel tstm = (ProjectTableModel) table.getModel();
			int modelRow = table.convertRowIndexToModel(row);
			Project prj = tstm.getProjectAt(modelRow);
			
			
			String text = null;
			
			switch (column) {
			case ProjectTableModel.COLUMN_TIMEOVERALL:
			case ProjectTableModel.COLUMN_TIMETODAY:
				text = formatSeconds(((Integer)value).intValue());
				this.setHorizontalAlignment(SwingConstants.RIGHT);
				this.setText(text);
				break;
			case ProjectTableModel.COLUMN_CREATED:
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd" /* HH:mm:ss */);
				text = sdf.format((Date)value);
				this.setHorizontalAlignment(SwingConstants.CENTER);
				this.setText(text);
				break;
			case ProjectTableModel.COLUMN_ACTION_DELETE:
				this.setToolTipText("delete project");
				this.setIcon(new ImageIcon(JTimeSchedApp.IMAGES_PATH + "project-delete.png"));
				this.setHorizontalAlignment(SwingConstants.CENTER);
				break;
			case ProjectTableModel.COLUMN_ACTION_STARTPAUSE:
				ImageIcon ii;
				//String tooltip;
				if (prj.isRunning()) {
					//tooltip = "pause";
					ii = new ImageIcon(JTimeSchedApp.IMAGES_PATH + "pause.png");
				}
				else {
					//tooltip = "start";
					ii = new ImageIcon(JTimeSchedApp.IMAGES_PATH + "start.png");
				}
				//this.setToolTipText(tooltip);
				this.setIcon(ii);
				this.setHorizontalAlignment(SwingConstants.CENTER);
				break;
			}
			
			// row-color
			if (prj.isRunning()) {
				this.setFont(this.getFont().deriveFont(Font.BOLD));
				this.setBackground(COLOR_RUNNING);
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
	
	
	class ColorCellRenderer extends JLabel implements TableCellRenderer {
		
		public ColorCellRenderer() {
			this.setOpaque(true);
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			
			if (value != null) {
				this.setBackground((Color) value);
				this.setBorder(new LineBorder(Color.WHITE, 2));
			}
			else {
				ProjectTableModel tstm = (ProjectTableModel) table.getModel();
				int modelRow = table.convertRowIndexToModel(row);
				Project prj = tstm.getProjectAt(modelRow);
				
				if (prj.isRunning()) {
					this.setBackground(COLOR_RUNNING);
				} else {
					if (isSelected) {
						this.setBackground(table.getSelectionBackground());
					} else {
						this.setBackground(table.getBackground());
					}
				}
				
				this.setBorder(null);
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
			Project prj = ((ProjectTableModel)table.getModel()).getProjectAt(modelRow);
			if (prj.isRunning()) {
				c.setBackground(COLOR_RUNNING);
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
	
	class ColorCellEditor extends AbstractCellEditor implements TableCellEditor, MouseListener {
		JButton btnEdit;
		Color currentColor;
		Color selectedColor;
		
		public ColorCellEditor() {
			this.btnEdit = new JButton();
			this.btnEdit.addMouseListener(this);
		}
		
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value,
				boolean isSelected, int row, int column) {
			
			this.currentColor = (Color) value;
			
			return this.btnEdit;
		}

		@Override
		public Object getCellEditorValue() {
			return this.selectedColor; 
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			this.btnEdit.setBackground(this.currentColor);
			
			Point posClick = e.getLocationOnScreen();
			ColorDialog colorDialog = new ColorDialog(JTimeSchedFrame.this,
					posClick,
					this.currentColor);
			
			colorDialog.setVisible(true);
			this.selectedColor = colorDialog.getSelectedColor();
			
			this.fireEditingStopped();
		}
		
		@Override
		public void mouseClicked(MouseEvent arg0) {}

		@Override
		public void mouseEntered(MouseEvent arg0) {}

		@Override
		public void mouseExited(MouseEvent arg0) {}

		@Override
		public void mouseReleased(MouseEvent arg0) {}

		@Override
		public void cancelCellEditing() {
			
			super.cancelCellEditing();
			
			System.out.println("canel");
		}
		
		
	}
	
	class TimeCellEditor extends DefaultCellEditor {
		private JTextField tfEdit;
		private int oldSeconds;
		
		public TimeCellEditor() {
			super(new JTextField());
			this.tfEdit = (JTextField) this.getComponent();
			this.tfEdit.setHorizontalAlignment(SwingConstants.RIGHT);
		}

		@Override
		public Object getCellEditorValue() {
			String strTime = this.tfEdit.getText();
			int newSeconds = this.oldSeconds;
			
			if (strTime.isEmpty() || strTime.equals("0"))
				newSeconds = 0;
			else {
				try {
					newSeconds = parseSeconds(strTime);
				} catch (ParseException e) {
					System.err.println("Invalid seconds-string, keeping previous value");
				}
			}
			
			return newSeconds;
		}

		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			this.oldSeconds = ((Integer)value).intValue();
			String strTime = formatSeconds(this.oldSeconds);
			this.tfEdit.setText(strTime);
			
			return this.tfEdit;
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
}
