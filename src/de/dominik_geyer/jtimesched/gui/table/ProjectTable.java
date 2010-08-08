package de.dominik_geyer.jtimesched.gui.table;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import de.dominik_geyer.jtimesched.project.ProjectTableModel;


@SuppressWarnings("serial")
public class ProjectTable extends JTable {
	private static final int COLUMN_ICON_WIDTH = 22;
	
	private JFrame parentFrame;
	
	public ProjectTable(JFrame parentFrame, ProjectTableModel ptm) {
		super(ptm);
		
		this.parentFrame = parentFrame;
		
		this.setFillsViewportHeight(true);
		this.setShowGrid(true);
		this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.setAutoCreateRowSorter(true);
		this.setRowHeight(ProjectTable.COLUMN_ICON_WIDTH);
		//this.tblSched.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); // not needed?
		this.getTableHeader().setReorderingAllowed(false);
		
		// set a custom default cell-renderer 
		//TableCellRenderer defaultRenderer = this.getDefaultRenderer(Object.class);
		//this.setDefaultRenderer(Object.class, new CustomCellRenderer(/*defaultRenderer*/));
		
		
		// set default sort-column
		List <RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
		sortKeys.add(new RowSorter.SortKey(ProjectTableModel.COLUMN_CREATED, SortOrder.ASCENDING));
		this.getRowSorter().setSortKeys(sortKeys);
		
		
		// define and set column properties
		Object[][] columnProps = new Object[][] {
				{
					ProjectTableModel.COLUMN_CHECK,
					-1,
					ProjectTable.COLUMN_ICON_WIDTH,
					ProjectTable.COLUMN_ICON_WIDTH,
					new CheckCellRenderer(),
					null
				},
				{
					ProjectTableModel.COLUMN_TITLE,
					200,
					100,
					-1,
					new CustomCellRenderer(),
					null
				},
				{
					ProjectTableModel.COLUMN_COLOR,
					-1,
					ProjectTable.COLUMN_ICON_WIDTH,
					ProjectTable.COLUMN_ICON_WIDTH,
					new ColorCellRenderer(),
					new ColorCellEditor(this.parentFrame)
				},
				{
					ProjectTableModel.COLUMN_CREATED,
					-1,
					80,
					80,
					new CustomCellRenderer(),
					null
				},
				{
					ProjectTableModel.COLUMN_TIMEOVERALL,
					95,
					60,
					95,
					new CustomCellRenderer(),
					new TimeCellEditor()
				},
				{
					ProjectTableModel.COLUMN_TIMETODAY,
					95,
					60,
					95,
					new CustomCellRenderer(),
					new TimeCellEditor()
				},
				{
					ProjectTableModel.COLUMN_ACTION_DELETE,
					-1,
					ProjectTable.COLUMN_ICON_WIDTH,
					ProjectTable.COLUMN_ICON_WIDTH,
					new CustomCellRenderer(),
					null
				},
				{
					ProjectTableModel.COLUMN_ACTION_STARTPAUSE,
					-1,
					ProjectTable.COLUMN_ICON_WIDTH,
					ProjectTable.COLUMN_ICON_WIDTH,
					new CustomCellRenderer(),
					null
				},
		};
		
		
		TableColumnModel tcm = this.getColumnModel();
		for (Object[] cp: columnProps) {
			TableColumn tc = tcm.getColumn((Integer)cp[0]);
			
			if ((Integer)cp[1] > 0)
				tc.setPreferredWidth((Integer)cp[1]);
			
			if ((Integer)cp[2] > 0)
				tc.setMinWidth((Integer)cp[2]);
			
			if ((Integer)cp[3] > 0)
				tc.setMaxWidth((Integer)cp[3]);
			
			if ((TableCellRenderer)cp[4] != null)
				tc.setCellRenderer((TableCellRenderer)cp[4]);
			
			if ((TableCellEditor)cp[5] != null)
				tc.setCellEditor((TableCellEditor)cp[5]);
		}
	}
}
