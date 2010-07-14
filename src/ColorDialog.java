import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;


@SuppressWarnings("serial")
public class ColorDialog extends JDialog implements ActionListener {
	private static final String NOCOLOR = "nocolor";
	private static final int SIZE = 32;
	
	private Color selectedColor;
	
	public ColorDialog(JFrame parent, Point position, Color currentColor /*not used yet*/) {
		super(parent);
		
		this.setModal(true);
		this.setUndecorated(true);
		this.setLocation(position);
		
		Color[] colors = new Color[] {
				new Color(219,	148,	112),
				new Color(219,	201,	112),
				new Color(184,	219,	112),
				new Color(130,	219,	112),
				new Color(219,	112,	130),
				new Color(207,	111,	63),
				new Color(112,	184,	219),
		};
		
		JPanel panel = new JPanel();
		
		panel.setLayout(new FlowLayout());
		
		
		JButton btn;
		
		for (Color c: colors) {
			btn = new JButton();
			btn.setPreferredSize(new Dimension(SIZE, SIZE));
			btn.setMinimumSize(new Dimension(SIZE, SIZE));
			btn.setMaximumSize(new Dimension(SIZE, SIZE));
			
			btn.setBackground(c);
			
			btn.addActionListener(this);
			
			panel.add(btn);
		}
		
		this.add(panel);
		
		
		// no-color button
		btn = new JButton("\u2022");
		btn.setPreferredSize(new Dimension(SIZE, SIZE));
		btn.setMinimumSize(new Dimension(SIZE, SIZE));
		btn.setMaximumSize(new Dimension(SIZE, SIZE));
		btn.setMargin(new Insets(0, 0, 0, 0));
		
		btn.setBackground(Color.WHITE);
		btn.setForeground(Color.RED);
		btn.setFont(btn.getFont().deriveFont(20.0f));
		
		btn.addActionListener(this);
		btn.setActionCommand(NOCOLOR);
		
		panel.add(btn);
		
		
		this.pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JButton btn = (JButton) e.getSource();
		
		if (e.getActionCommand() == NOCOLOR)
			this.selectedColor = null;
		else
			this.selectedColor = btn.getBackground();
		
		this.setVisible(false);
		this.dispose();
	}
	
	public Color getSelectedColor() {
		return this.selectedColor;
	}
}
