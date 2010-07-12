import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.MenuItem;
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
import java.text.ParseException;
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
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;



public class JTimeSchedFrame extends JFrame {
	
	private JTable tblSched;
	private JLabel lblOverall;
	private ArrayList<Project> arPrj = new ArrayList<Project>();
	
	public JTimeSchedFrame() {
		super("jTimeSched (" + JTimeSchedApp.APP_VERSION + ")");
		
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(JTimeSchedApp.IMAGES_PATH + "history.png"));
		//this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		//this.addWindowListener(new JTimeSchedFrameWindowListener());
		
		this.setPreferredSize(new Dimension(600, 200));
		this.setMinimumSize(new Dimension(400, 150));
		
		
		File file = new File(JTimeSchedApp.PRJ_FILE);
		if (file.isFile()) {
			try {
				this.arPrj = JTimeSchedApp.loadProjects();
				System.out.println(this.arPrj);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		// >>>>>>>>> DEBUG start
//		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");	
//		try {
//			Project prj;
//			prj = new Project(
//					"debug", ProjectPriority.MEDIUM,
//					df.parse("2010-07-09 15:00:00"),
//					df.parse("2010-07-09 23:58:00"),
//					false,
//					130,
//					90
//			);
//			
//			this.arPrj.add(prj);
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
		// <<<<<<<<< DEBUG stop

		TimeSchedTableModel tstm = new TimeSchedTableModel(this.arPrj);

		this.tblSched = new JTable(tstm);
		
		this.tblSched.setFillsViewportHeight(true);
		this.tblSched.setShowGrid(true);
		//this.tblSched.setShowHorizontalLines(true);
		this.tblSched.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.tblSched.setAutoCreateRowSorter(true);
		
		this.tblSched.getTableHeader().setReorderingAllowed(false);
		
		
		TableCellRenderer defaultRenderer = this.tblSched.getDefaultRenderer(Object.class);
		this.tblSched.setDefaultRenderer(Object.class,
				new TimeSchedTableCellRenderer(defaultRenderer));

		
		final int columnWidthIcon = 22;
		this.tblSched.setRowHeight(columnWidthIcon);
		
		TableColumnModel tcm = this.tblSched.getColumnModel();
		
		int[][] columnWidths = new int[][] {
				{TimeSchedTableModel.COLUMN_TITLE,			200,	100,		-1},
				//{TimeSchedTableModel.COLUMN_PRIORITY,		-1,		80,		80},
				{TimeSchedTableModel.COLUMN_CREATED,		-1,		80,		80},
				{TimeSchedTableModel.COLUMN_TIMEOVERALL,	80,		55,		80},
				{TimeSchedTableModel.COLUMN_TIMETODAY,		80,		55,		80},
				{TimeSchedTableModel.COLUMN_ACTION_DELETE,		-1,		columnWidthIcon,	columnWidthIcon},
				{TimeSchedTableModel.COLUMN_ACTION_STARTPAUSE,	-1,		columnWidthIcon,	columnWidthIcon},
		};
		
		for (int[] cw: columnWidths) {
			TableColumn tc = tcm.getColumn(cw[0]);
			
			if (cw[1] > 0)
				tc.setPreferredWidth(cw[1]);
			
			if (cw[2] > 0)
				tc.setMinWidth(cw[2]);
			
			if (cw[3] > 0)
				tc.setMaxWidth(cw[3]);	
		}
		
	//	tcm.getColumn(TimeSchedTableModel.COLUMN_PRIORITY).setCellEditor(
	//			new MyComboBoxEditor(new String[] {"hallo", "food"} ));

		tcm.getColumn(TimeSchedTableModel.COLUMN_CREATED).setCellRenderer(
				new CustomCellRenderer());
		
		tcm.getColumn(TimeSchedTableModel.COLUMN_TIMEOVERALL).setCellRenderer(
				new CustomCellRenderer());
		
		tcm.getColumn(TimeSchedTableModel.COLUMN_TIMETODAY).setCellRenderer(
				new CustomCellRenderer());
		
		tcm.getColumn(TimeSchedTableModel.COLUMN_ACTION_DELETE).setCellRenderer(
				new CustomCellRenderer());
		
		tcm.getColumn(TimeSchedTableModel.COLUMN_ACTION_STARTPAUSE).setCellRenderer(
				new CustomCellRenderer());
		
		
		
		this.tblSched.addMouseListener(new TimeSchedTableMouseListener());

		JScrollPane spSched = new JScrollPane(this.tblSched);
		this.add(spSched, BorderLayout.CENTER);

		
		JPanel panelBottom = new JPanel();
		//panelBottom.setLayout(new FlowLayout(FlowLayout.LEFT));
		panelBottom.setLayout(new BoxLayout(panelBottom, BoxLayout.LINE_AXIS));
		JButton btnAdd = new JButton("Add project", new ImageIcon(JTimeSchedApp.IMAGES_PATH + "history-add.png"));
		btnAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				handleNewButton();
			}
		});
		
		panelBottom.add(btnAdd);
		
		panelBottom.add(Box.createHorizontalGlue());
		
		this.lblOverall = new JLabel("", SwingConstants.RIGHT);
		panelBottom.add(this.lblOverall);
		
		this.add(panelBottom, BorderLayout.SOUTH);


		// setup update timer
		Timer timer = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateGUI();
			}
		});
		
		timer.setRepeats(true);
		timer.start();
		
		
		// initially update table
		this.updateGUI();
		
		
		// create tray-icon
		trayIcon();
		
		
		this.pack();
	}
	
	protected void updateGUI() {
		TimeSchedTableModel tstm = (TimeSchedTableModel)tblSched.getModel();
		
		int rowCount = tstm.getRowCount();
		if (rowCount > 0)
			tstm.fireTableRowsUpdated(0, rowCount -1);
		
		int timeOverall = 0;
		int timeToday = 0;
		for (Project p: this.arPrj) {
			timeOverall += p.getSecondsOverall();
			timeToday += p.getSecondsToday();
		}
		
		this.lblOverall.setText(
				String.format("%d projects | %s overall | %s today ",
						rowCount,
						this.formatSeconds(timeOverall),
						this.formatSeconds(timeToday)));
	}
	
	
//	class JTimeSchedFrameWindowListener extends WindowAdapter {
//		@Override
//		public void windowClosing(WindowEvent e) {
//			
//			Window window = e.getWindow();
//			window.setVisible(false);
//			//window.dispose();
//			//System.exit(0);
//		}
//	}
	


	
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
	
	/*
	public class MyComboBoxEditor extends DefaultCellEditor {
		public MyComboBoxEditor(String[] items) {
			super(new JComboBox(items));
		}
	}
	*/

	public String formatSeconds(int s) {
		return String.format("%d:%02d:%02d", s/3600, (s%3600)/60, (s%60));
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
				handleDelete(tstm, prj, row, column);
				break;
			case TimeSchedTableModel.COLUMN_ACTION_STARTPAUSE:
				handleStartPause(tstm, prj, row, column);
				break;
			case TimeSchedTableModel.COLUMN_TIMETODAY:
				// reset today's time on double-click
				if (e.getClickCount() == 2) {
					int response = JOptionPane.showConfirmDialog(JTimeSchedFrame.this,
							"Reset time of project \"" + prj.getTitle() + "\" for today?",
							"Reset today time",
							JOptionPane.YES_NO_OPTION);
					
					if (response != JOptionPane.YES_OPTION)
						return;
					
					try {
						if (prj.isRunning())
							prj.pause();
						prj.resetToday();
					} catch (ProjectException e1) {
						e1.printStackTrace();
					}
					
					
					((TimeSchedTableModel)tblSched.getModel()).fireTableRowsUpdated(row, row);
				}
				break;
			}
		}
		
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
				
				prj.start();
			}
		} catch (ProjectException ex) {
			ex.printStackTrace();
		}
		
		// update GUI
		this.updateGUI();
	}
	
	public void handleDelete(TimeSchedTableModel tstm, Project prj, int row, int column) {
		int response = JOptionPane.showConfirmDialog(
				this,
				"Delete project \"" + prj.getTitle() + "\"?",
				"Delete position?",
				JOptionPane.YES_NO_OPTION);
		
		if (response != JOptionPane.YES_OPTION)
			return;
		
		tstm.removeProject(row);
	}

	public void handleNewButton() {
		Project prj = new Project("New project", ProjectPriority.MEDIUM);
		
		((TimeSchedTableModel)this.tblSched.getModel()).addProject(prj);
	}
	
	public void trayIcon() {
		final TrayIcon trayIcon;

		if (SystemTray.isSupported()) {

			SystemTray tray = SystemTray.getSystemTray();
			Image image = Toolkit.getDefaultToolkit().getImage(JTimeSchedApp.IMAGES_PATH + "history.png");

			//		    MouseListener mouseListener = new MouseListener() {
			//		                
			//		        public void mouseClicked(MouseEvent e) {
			//		            System.out.println("Tray Icon - Mouse clicked!");                 
			//		        }
			//
			//		        public void mouseEntered(MouseEvent e) {
			//		            System.out.println("Tray Icon - Mouse entered!");                 
			//		        }
			//
			//		        public void mouseExited(MouseEvent e) {
			//		            System.out.println("Tray Icon - Mouse exited!");                 
			//		        }
			//
			//		        public void mousePressed(MouseEvent e) {
			//		            System.out.println("Tray Icon - Mouse pressed!");                 
			//		        }
			//
			//		        public void mouseReleased(MouseEvent e) {
			//		            System.out.println("Tray Icon - Mouse released!");                 
			//		        }
			//		    };

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


			trayIcon = new TrayIcon(image, "jTimeSched", popup);

			ActionListener actionListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//		            trayIcon.displayMessage("Action Event", 
					//		                "An Action Event Has Been Performed!",
					//		                TrayIcon.MessageType.INFO);
					if (!isVisible() /*|| !isActive()*/)
						setVisible(true);
					else
						setVisible(false);
				}
			};

			trayIcon.setImageAutoSize(true);
			trayIcon.addActionListener(actionListener);
			//		    trayIcon.addMouseListener(mouseListener);

			try {
				tray.add(trayIcon);
			} catch (AWTException e) {
				System.err.println("TrayIcon could not be added.");
			}

		} else {
			//  System Tray is not supported
			System.err.println("TrayIcon is not supported.");
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

		System.out.println("Exiting...");
		System.exit(0);
	}
}
