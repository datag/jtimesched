package de.dominik_geyer.jtimesched.gui.table;

import java.awt.Component;
import java.text.ParseException;
import java.util.Date;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import de.dominik_geyer.jtimesched.project.ProjectTime;


@SuppressWarnings("serial")
public class DateCellEditor extends DefaultCellEditor {
	private JTextField tfEdit;
	private Date oldDate;
	
	public DateCellEditor() {
		super(new JTextField());
		this.tfEdit = (JTextField) this.getComponent();
		this.tfEdit.setHorizontalAlignment(SwingConstants.CENTER);
	}

	@Override
	public Object getCellEditorValue() {
		String strDate = this.tfEdit.getText();
		Date newDate = this.oldDate;
		
		if (strDate.isEmpty())
			newDate = new Date();
		else {
			try {
				newDate = ProjectTime.parseDate(strDate);
			} catch (ParseException e) {
				System.err.println("Invalid date-string, keeping previous value");
			}
		}
		
		return newDate;
	}

	@Override
	public Component getTableCellEditorComponent(JTable table,
			Object value, boolean isSelected, int row, int column) {
		this.oldDate = (Date)value;
		String strDate = ProjectTime.formatDate(this.oldDate);
		this.tfEdit.setText(strDate);
		
		return this.tfEdit;
	}
}
