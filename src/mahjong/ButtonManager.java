package mahjong;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * The manager of the button list at the bottom of the GUI.
 */
@SuppressWarnings("serial")
public class ButtonManager extends JPanel implements ActionListener {
	/** Button value for the new game button. */
	private static final int NEWGAME = 0;
	/** Button value for the hint mode button. */
	private static final int PAUSE = 1;
	/** Button value for the undo button. */
	private static final int UNDO = 2;
	/** Button value for the redo button. */
	private static final int REDO = 3;
	/** Button value for the quit button. */
	private static final int HINT = 4;
	/** Button value for the pause mode button. */
	private static final int QUIT = 5;
	
	/** Actual Button objects. */
	private JButton[] b;
	/** Reference to the drawing area we need to interact with. */
	private JViewer jv;
	
	/** Button text and mouseover explanations. */
	private String[][] btext = {
		{"New Game", "Start a new game" },
		{"Pause", "Pause the game" },
		{"Undo", "Undo your last move" },
		{"Redo", "Redo" },
		{"Hint Mode", "Toggle hint mode" },
		{"Quit", "Quit the game" }};

	/**
	 * Construct a new button manager.
	 * @param d1 The Viewer associated.
	 */
	public ButtonManager(JViewer jv) {
		b = new JButton[btext.length];
		// the button manager will hold a list of buttons. This requires
		// a flow layout
		setLayout(new FlowLayout());
//		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// store the drawing area for later use
		this.jv = jv;
		
		// Here's some buttons:
		for (int i = 0; i < btext.length; i++) {
			b[i] = new JButton(btext[i][0]);
			b[i].setActionCommand(btext[i][0]);
			// set us up as a listener to each button, so
			// actionPerformed(...) gets called
			b[i].addActionListener(this);
			b[i].setToolTipText(btext[i][1]);
			// add the buttons to us
			add(b[i]);
		}
	}


	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		Viewer d = (this.jv).getViewer();
		for (int i = 0; i < btext.length; i++) {
			if (e.getActionCommand().equals(btext[i][0])) {
				switch(i) {
				case NEWGAME:
					try {
						LayoutSelector l = (this.jv).getLayoutSelector();
						l.reset();
						
						d.repaint();
						
						String s = l.showLayoutSelectorDialog();
						(d.getBoard()).setLayoutFile(LayoutSelector.LAYOUTDIR + File.separator + s.toLowerCase() + LayoutSelector.LAYOUT_EXT);
						(this.jv).setTitle("Mahjongg: " + s);
						(d.getHighScoreManager()).setLayout(s);
						d.reset();
						l.layoutSelected();
						
						d.repaint();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					break;
				case PAUSE:
					if (d.isPaused()) d.unPause();
					else if (d.isPlaying()) d.pause();
					d.repaint();
					break;
				case UNDO:
					if (d.isPlaying()) {
						if (d.canUndo() && !d.undoUsed() && d.getCanEnterHighScore()) {
							int option = JOptionPane.showConfirmDialog(d, "Using Undo will prevent you from being\n" +
									"eligible for the list of top times.\n" +
									"So do you want to Undo?", null, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
							if (option == JOptionPane.YES_OPTION) {
								d.undoMove();
								d.cantEnterHighScore();
							} 
						} else d.undoMove();
						d.repaint();
					}
					break;
				case REDO:
					if (d.isPlaying()) {
						d.redoMove();
						d.repaint();
					}
					break;
				case HINT:
					if (d.isPlaying()) {
						if (!d.hintUsed() && d.getCanEnterHighScore()) {
							int option = JOptionPane.showConfirmDialog(d, "Using Hint Mode will prevent you from being\n" +
								"eligible for the list of top times.\n" +
								"So do you want to use Hint Mode?", null, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
							if (option == JOptionPane.YES_OPTION) {
								d.toggleHintMode();
								d.cantEnterHighScore();
							}
						} else d.toggleHintMode();
						d.repaint();
					}
					break;
				case QUIT:
					d.pause();
					int option = JOptionPane.showConfirmDialog(d, "Are you sure you want to quit?", null, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if (option == JOptionPane.NO_OPTION) {
						d.unPause();
						d.repaint();
					} else System.exit(0);
					break;
				default:
					System.err.println("Incorrectly constructed button option.");
					System.exit(1);
				}
			}
		}
	}
	
}
