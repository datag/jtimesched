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
