/* jTimeSched - A simple and lightweight time tracking tool
 * Copyright (C) 2010-2014 Dominik D. Geyer <dominik.geyer@gmail.com>
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

import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import de.dominik_geyer.jtimesched.project.Project;
import de.dominik_geyer.jtimesched.project.ProjectTableModel;
import de.dominik_geyer.jtimesched.project.ProjectTime;


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
			tcc.setToolTipText(prj.getQuotaToday() > 0 ?
					String.format("Quota today: %s", ProjectTime.formatSeconds(prj.getQuotaToday())) :
					null);
			break;
		case ProjectTableModel.COLUMN_TIMEOVERALL:
			tcc = new TimeCellComponent(prj.getSecondsOverall(), prj.getQuotaOverall());
			tcc.setToolTipText(prj.getQuotaOverall() > 0 ?
					String.format("Quota overall: %s", ProjectTime.formatSeconds(prj.getQuotaOverall())) :
					null);
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
