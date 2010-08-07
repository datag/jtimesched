import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;


@SuppressWarnings("serial")
public class ColorDialog extends JDialog implements ActionListener {
	private static final String NOCOLOR = "nocolor";
	private static final String CHOOSER = "chooser";
	private static final int SIZE = 32;
	
	private Color selectedColor;
	private Color currentColor;
	
	public ColorDialog(JFrame parent, Point position, Color currentColor) {
		super(parent);
		
		this.setModal(true);
		this.setUndecorated(true);
		this.setLocation(position);
		
		this.currentColor = currentColor;
		
		
		Color[] colors = new Color[] {
				new Color(219,	148,	112),
				new Color(219,	201,	112),
				//new Color(184,	219,	112),
				new Color(130,	219,	112),
				new Color(229,	122,	194),
				//new Color(207,	111,	63),
				new Color(122,	194,	229),
				new Color(205,	205,	205),
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

		
		// custom-color chooser button
		btn = new JButton(new ImageIcon(JTimeSchedApp.IMAGES_PATH + "color-custom.png"));
		btn.setToolTipText("choose custom color");
		btn.setPreferredSize(new Dimension(SIZE, SIZE));
		btn.setMinimumSize(new Dimension(SIZE, SIZE));
		btn.setMaximumSize(new Dimension(SIZE, SIZE));
		btn.setMargin(new Insets(0, 0, 0, 0));
		
		btn.setBackground(Color.WHITE);
		btn.setForeground(Color.BLUE);
		btn.setFont(btn.getFont().deriveFont(22.0f));
		
		btn.addActionListener(this);
		btn.setActionCommand(CHOOSER);
		
		panel.add(btn);
		
		
		// no-color button
		btn = new JButton(new ImageIcon(JTimeSchedApp.IMAGES_PATH + "color-reset.png"));
		btn.setToolTipText("reset color");
		btn.setPreferredSize(new Dimension(SIZE, SIZE));
		btn.setMinimumSize(new Dimension(SIZE, SIZE));
		btn.setMaximumSize(new Dimension(SIZE, SIZE));
		btn.setMargin(new Insets(0, 0, 0, 0));
		
		btn.setBackground(Color.WHITE);
		btn.setForeground(Color.RED);
		btn.setFont(btn.getFont().deriveFont(22.0f));
		
		btn.addActionListener(this);
		btn.setActionCommand(NOCOLOR);
		
		panel.add(btn);
		
		
		// exit on key ESC
		final KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true);
		getRootPane().registerKeyboardAction(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ColorDialog.this.selectedColor = ColorDialog.this.currentColor;
				
				ColorDialog.this.setVisible(false);
				ColorDialog.this.dispose();
			}},
			keyStroke,
			JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		
		
		this.pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JButton btn = (JButton) e.getSource();
		
		if (e.getActionCommand() == NOCOLOR) {
			this.selectedColor = null;
		} else if (e.getActionCommand() == CHOOSER) {
			Color chosenColor = JColorChooser.showDialog(ColorDialog.this,
					"Choose a custom color",
					ColorDialog.this.currentColor);
			
			if (chosenColor != null)
				this.selectedColor = chosenColor;
			else
				this.selectedColor = this.currentColor;
		} else {
			this.selectedColor = btn.getBackground();
		}
		
		this.setVisible(false);
		this.dispose();
	}
	
	public Color getSelectedColor() {
		return this.selectedColor;
	}
}
