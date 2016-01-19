package mahjong;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

/** The JFrame that defines the entire GUI.
 */
@SuppressWarnings("serial")
public class JViewer extends JFrame {
	/** The actual board drawing area. */
	private Viewer drawingArea = null;
	private Board b;
	private TimerLabel timerArea;
	private LayoutSelector layoutselector;
	
	/** Main constructor to use.
	 * @param bn Board name, used in the window title.
	 * @param b Actual Board object.
	 * @throws IOException 
	 */
	public JViewer(String bn, Board b) throws IOException {
//		super("Mahjongg: " + bn);
		super();
		
		/* A closing adaptor to respond to window close events (This is so clicking on the
		 * window-close button on the main frame will cause it to go away). */
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.out.println("Quitting main frame....");
				System.exit(0);
			}
		});

		/* The contents of a jframe are not accessed directly, rather are accessed through the
		 * ContentPane, which is guaranteed to be non-null. */
		Container c = getContentPane();

		// c.setLayout(new BorderLayout());
		
		this.b = b;
		
		this.timerArea = new TimerLabel();
		this.timerArea.setOpaque(true);
	
		this.layoutselector = new LayoutSelector(this.drawingArea);
		
		/* Inside there are have two panels; one to contain buttons, the other to contain the
		 * drawing area. */
		this.drawingArea = new Viewer(this.b, timerArea, this.layoutselector);
//		ButtonManager buttonList = new ButtonManager(this.drawingArea);
		ButtonManager buttonList = new ButtonManager(this);
		
		/* These main components are added to the frame's content. The drawing area is
		 * centered (Occupies most of the window), and the button list is located in a row
		 * underneath it. */
	
		JScrollPane scroller = new JScrollPane(this.drawingArea);
		scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		c.add(timerArea,BorderLayout.NORTH);
		
		c.add(scroller, BorderLayout.CENTER);
		c.add(buttonList, BorderLayout.SOUTH);
		
		/* Arrange components */
		this.pack();
		
//		this.setSize(this.getWidth(), this.getHeight());

		/* Set visible so window appears on screen. */
		this.setVisible(true);
		
		String s = (this.layoutselector).showLayoutSelectorDialog();
		if (!s.equals(LayoutSelector.getLayoutTitle((b.getLayoutFile().substring((LayoutSelector.LAYOUTDIR + java.io.File.separator).length()))))) {			
			Board board = (this.drawingArea).getBoard();
			board.setLayoutFile(LayoutSelector.LAYOUTDIR + File.separator + s.toLowerCase() + LayoutSelector.LAYOUT_EXT);
			board.reset();
			
			(this.drawingArea).setHighScoreManager(s);
			
			this.setTitle("Mahjongg: " + s);
		}
		(this.layoutselector).layoutSelected();
		(this.drawingArea).repaint();
	}

	/** Getter for actual Board GUI component.
	 * @return The JPanel containing the board itself.
	 */
	public Viewer getViewer() {
		return drawingArea;
	}
	
	public LayoutSelector getLayoutSelector () {
		return this.layoutselector;
	}

}
