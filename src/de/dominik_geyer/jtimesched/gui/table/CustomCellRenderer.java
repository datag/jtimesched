/* jTimeSched - A simple and lightweight time tracking tool
 * Copyright (C) 2010-2012 Dominik D. Geyer <dominik.geyer@gmail.com>
 * See LICENSE.txt for details.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.dominik_geyer.jtimesched.gui.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellRenderer;

import de.dominik_geyer.jtimesched.gui.JTimeSchedFrame;
import de.dominik_geyer.jtimesched.project.Project;
import de.dominik_geyer.jtimesched.project.ProjectTableModel;
import de.dominik_geyer.jtimesched.project.ProjectTime;


@SuppressWarnings("serial")
public class CustomCellRenderer extends JLabel implements TableCellRenderer {
	public static final Color COLOR_RUNNING = new Color(0xFF, 0xE9, 0x7F);
	
	public CustomCellRenderer() {
		this.setOpaque(true);
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		
		ProjectTableModel tstm = (ProjectTableModel) table.getModel();
		int modelRow = table.convertRowIndexToModel(row);
		int modelColumn = table.convertColumnIndexToModel(column);
		Project prj = tstm.getProjectAt(modelRow);
		
		
		String text = null;
		
		switch (modelColumn) {
		case ProjectTableModel.COLUMN_TITLE:
			this.setText((String)value);
			
			// row-color
			ProjectTable pt = (ProjectTable) table;
			if (pt.isHighlightRow(modelRow)) {
				this.setBorder(new LineBorder(Color.BLACK, 2));
			} else {
				this.setBorder(null);
			}
			
			if (!prj.getNotes().isEmpty()) {
				String tooltip = prj.getNotes()
					.replaceAll("&", "&amp;")
					.replaceAll("(\r\n|\n)", "<br/>")
					.replaceAll(" ", "&nbsp;")
					.replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
				
				this.setToolTipText("<html><strong><u>Notes:</u></strong><br/><br/>" +
						tooltip +
						"</html>");
			} else {
				this.setToolTipText(null);
			}
			
			break;
		case ProjectTableModel.COLUMN_CREATED:
			text = ProjectTime.formatDate((Date)value);
			this.setHorizontalAlignment(SwingConstants.CENTER);
			this.setText(text);
			break;
		case ProjectTableModel.COLUMN_ACTION_DELETE:
			this.setToolTipText("remove project");
			this.setIcon(JTimeSchedFrame.getImageIcon("project-delete.png"));
			this.setHorizontalAlignment(SwingConstants.CENTER);
			break;
		case ProjectTableModel.COLUMN_ACTION_STARTPAUSE:
			ImageIcon ii;
			//String tooltip;
			if (prj.isRunning()) {
				//tooltip = "pause";
				ii = JTimeSchedFrame.getImageIcon("pause.png");
			}
			else {
				//tooltip = "start";
				ii = JTimeSchedFrame.getImageIcon("start.png");
			}
			//this.setToolTipText(tooltip);
			this.setIcon(ii);
			this.setHorizontalAlignment(SwingConstants.CENTER);
			break;
		}
		
		if (prj.isRunning()) {
			this.setFont(this.getFont().deriveFont(Font.BOLD));
			this.setBackground(CustomCellRenderer.COLOR_RUNNING);
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
