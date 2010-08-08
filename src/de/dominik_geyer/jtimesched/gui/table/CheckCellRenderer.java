package de.dominik_geyer.jtimesched.gui.table;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import de.dominik_geyer.jtimesched.project.Project;
import de.dominik_geyer.jtimesched.project.ProjectTableModel;


@SuppressWarnings("serial")
public class CheckCellRenderer extends JCheckBox implements TableCellRenderer {
	
	@Override
	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row,
			int column) {
		
		ProjectTableModel tstm = (ProjectTableModel) table.getModel();
		int modelRow = table.convertRowIndexToModel(row);
		Project prj = tstm.getProjectAt(modelRow);
		
		this.setSelected(prj.isChecked());
		
		if (prj.isRunning()) {
			this.setBackground(CustomCellRenderer.COLOR_RUNNING);
		} else {
			if (isSelected) {
				this.setBackground(table.getSelectionBackground());
			} else {
				this.setBackground(table.getBackground());
			}
		}
		
		return this;
	}
}
