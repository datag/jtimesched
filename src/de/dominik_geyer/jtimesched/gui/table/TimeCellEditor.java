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
