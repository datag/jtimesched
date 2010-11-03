package de.dominik_geyer.jtimesched.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class NotesDialog extends JDialog {
	private static final int TEXTAREA_COLS = 40;
	private static final int TEXTAREA_ROWS = 5;
	
	private JTextArea taNotes;
	private Boolean confirmed = false;
	
	public NotesDialog(JFrame parent, String text) {
		super(parent);
		
		this.setModal(true);
		//this.setLocation(position);
		this.setTitle("Notes");
		
		//this.add(new JLabel("Additional notes:"), BorderLayout.NORTH);
		
		this.taNotes = new JTextArea(NotesDialog.TEXTAREA_ROWS, NotesDialog.TEXTAREA_COLS);
		this.taNotes.setAutoscrolls(true);
		this.taNotes.setLineWrap(true);
		this.taNotes.setWrapStyleWord(true);
		//this.taNotes.setBorder(BorderFactory.createLoweredBevelBorder());
		this.taNotes.setText(text);
		this.taNotes.setTabSize(2);
		
		this.add(new JScrollPane(this.taNotes), BorderLayout.CENTER);
		
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		
		buttonPane.add(btnCancel);
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		
		JButton btnOk = new JButton("OK");
		btnOk.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				confirmed = true;
				dispose();
			}
		});
		
		buttonPane.add(btnOk);
		
		this.add(buttonPane, BorderLayout.SOUTH);
		
		this.pack();
		this.setLocationRelativeTo(parent);
		
		this.taNotes.requestFocusInWindow();
	}
	
	public Boolean isConfirmed() {
		return this.confirmed;
	}
	
	public String getInputText() {
		return this.taNotes.getText();
	}
}
