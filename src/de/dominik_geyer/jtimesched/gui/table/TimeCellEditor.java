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
import java.text.ParseException;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import de.dominik_geyer.jtimesched.project.ProjectTime;


@SuppressWarnings("serial")
public class TimeCellEditor extends DefaultCellEditor {
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
				newSeconds = ProjectTime.parseSeconds(strTime);
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
		String strTime = ProjectTime.formatSeconds(this.oldSeconds);
		this.tfEdit.setText(strTime);
		
		return this.tfEdit;
	}
}
