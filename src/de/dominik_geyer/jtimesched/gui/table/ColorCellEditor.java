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
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import de.dominik_geyer.jtimesched.gui.ColorDialog;


@SuppressWarnings("serial")
public 	class ColorCellEditor extends AbstractCellEditor implements TableCellEditor, MouseListener {
	JFrame parent;
	JButton btnEdit;
	Color currentColor;
	Color selectedColor;
	
	public ColorCellEditor(JFrame parent) {
		this.parent = parent;
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
		ColorDialog colorDialog = new ColorDialog(parent,
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
}
