package de.dominik_geyer.jtimesched.gui.table;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellRenderer;

import de.dominik_geyer.jtimesched.project.Project;
import de.dominik_geyer.jtimesched.project.ProjectTableModel;


@SuppressWarnings("serial")
public class ColorCellRenderer extends JLabel implements TableCellRenderer {
	
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
				this.setBackground(CustomCellRenderer.COLOR_RUNNING);
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
