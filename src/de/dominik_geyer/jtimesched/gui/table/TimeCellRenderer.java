package de.dominik_geyer.jtimesched.gui.table;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import de.dominik_geyer.jtimesched.project.Project;
import de.dominik_geyer.jtimesched.project.ProjectTableModel;


@SuppressWarnings("serial")
public class TimeCellRenderer extends JLabel implements TableCellRenderer {
	
	@Override
	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		
		ProjectTableModel tstm = (ProjectTableModel) table.getModel();
		int modelRow = table.convertRowIndexToModel(row);
		int modelColumn = table.convertColumnIndexToModel(column);
		Project prj = tstm.getProjectAt(modelRow);
		
		TimeCellComponent tcc = null;
		
		switch (modelColumn) {
		case ProjectTableModel.COLUMN_TIMETODAY:
			tcc = new TimeCellComponent(prj.getSecondsToday(), prj.getQuotaToday());
			break;
		case ProjectTableModel.COLUMN_TIMEOVERALL:
			tcc = new TimeCellComponent(prj.getSecondsOverall(), prj.getQuotaOverall());
			break;
		}
		
		if (prj.isRunning()) {
			tcc.setFont(tcc.getFont().deriveFont(Font.BOLD));
			tcc.setBackground(CustomCellRenderer.COLOR_RUNNING);
		} else {
			tcc.setFont(tcc.getFont().deriveFont(Font.PLAIN));
			
			if (isSelected) {
				tcc.setBackground(table.getSelectionBackground());
			} else {
				tcc.setBackground(table.getBackground());
			}
		}
		
		return tcc;
	}
}
