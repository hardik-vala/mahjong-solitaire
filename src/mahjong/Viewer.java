package mahjong;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import mahjong.HighScoreManager.Score;

/** Actual board component of the GUI.
 */
@SuppressWarnings("serial")
public class Viewer extends JPanel {
	/** Constant tile width. */
	public static final int TILEW = 32;
	/** Constant tile height. */
	public static final int TILEH = 88;
	/** Constant tile shift due to increased level. */
	public static final int TILESKEW = 6;

	/** (Non-highlighted) Tile images. */
	private Image[][] tileImages;
	/** Highlighted tile images. */
	private Image[][] tileImagesHL;
	private Image[][] tileImagesSEL;
	private Image[][] tileImagesHint1;
	private Image[][] tileImagesHint2;
	
	/** Board itself. */
	private Board b;

	private LayoutSelector layoutSelector;
	private ViewerMouseListener vMouseListener;
	private GameStatus gameStatus;
	private TimerLabel timerLabel;
	private MoveList moveList;
	private HighScoreManager highScoreManager;
	private boolean canEnterHighScore;
	private boolean displayHighScores;
	
	private Tile tileSelected;
	
	/** Constructor, creates a viewer for the given board.
	 * @param b1 The board to display.
	 */
	public Viewer(Board b, TimerLabel t, LayoutSelector l) {
		super(true);
		this.b = b;
		this.makeTileImages(Board.MAXGROUPS);
		this.layoutSelector = l;
		this.gameStatus = new GameStatus(this.b);
		(this.gameStatus).updateStatus();
		this.timerLabel = t;
		this.moveList = new MoveList(b);
		String layoutTitle = LayoutSelector.getLayoutTitle((b.getLayoutFile().substring((LayoutSelector.LAYOUTDIR + java.io.File.separator).length())));
		this.highScoreManager = new HighScoreManager(layoutTitle);
		this.canEnterHighScore = true;
		this.displayHighScores = false;
		this.tileSelected = null;
		vMouseListener = new ViewerMouseListener(this);
		this.addMouseListener(vMouseListener);
		this.setBackground(Color.black);
	}

	/** Loads the images for a single set of tiles.
	 * 
	 * @param groups Number of groups to load.
	 * @param append The String common to each tile image file name.
	 * @return A 2D array of Image objects, providing images for
	 * each subindex member of each group.
	 */
	private Image[][] loadTileSet(int groups, String append) {
		Image[][] tr = new Image[groups][Board.GROUPSIZE];
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		MediaTracker mediaTracker = new MediaTracker(this);
		BufferedReader f = null;
		try {
			f = new BufferedReader(new FileReader(Main.getTileSetFile()));
			String s = f.readLine();
			while (s != null) {
				if (!s.startsWith("#")) {
					String[] tokens = s.split("[ ]+");
					try {
						int gn = Integer.parseInt(tokens[0]);
						for (int i = 1; i <= (Board.GROUPSIZE); i++) {
							Image img = toolkit.getImage(Main.getTileDir() + append + tokens[i] + ".png");
							mediaTracker.addImage(img, 0);
							try {
								mediaTracker.waitForID(0);
							} catch (InterruptedException ie) {
								System.err.println(ie);
								System.exit(1);
							}
							tr[gn][i - 1] = img;
						}
					} catch (NumberFormatException n) {
						System.err.println("Error reading group count: " + s);
						System.exit(1);
					}
				}
				s = f.readLine();
			}
		} catch (IOException ie) {
			System.err.println("IOException: " + ie);
			ie.printStackTrace();
			System.exit(1);
		} finally {
			try {
				f.close();
			} catch (IOException e) {
				System.err.println("Error closing reader for " + Main.getTileSetFile() + ".");
				e.printStackTrace();
			}
		}
		return tr;
	}

	/** For setting size of drawing area in gui.
	 * 
	 * @return The preferred size. 
	 */
	public Dimension getPreferredSize() {
		int maxw = b.getWidth();
		int maxh = b.getHeight();
		maxw = TILEW*(maxw + 2);
		maxh = TILEH/2*(maxh + 2);
		return new Dimension(maxw, maxh);
	}


	/** Load images for the given number of groups.
	 * 
	 * @param groups The number of groups in the tile set. 
	 */
	private void makeTileImages(int groups) {
		this.tileImages = loadTileSet(groups, "unlit_");
		this.tileImagesHL = loadTileSet(groups, "lit_");
		//////////////////////////////////
		this.tileImagesSEL = loadTileSet(groups, "sel_lit_");
		this.tileImagesHint1 = loadTileSet(groups, "hint1_lit_");
		this.tileImagesHint2 = loadTileSet(groups, "hint2_lit_");
	}

	/** Resets the game state.
	 * @throws IOException
	 */
	public void reset() throws IOException {
		b.reset();
		(this.gameStatus).reset();
		(this.vMouseListener).resetFirstClick();
		(this.timerLabel).reset();
		(this.moveList).reset();
		String layoutTitle = LayoutSelector.getLayoutTitle((b.getLayoutFile().substring((LayoutSelector.LAYOUTDIR + java.io.File.separator).length())));
		this.highScoreManager = new HighScoreManager(layoutTitle);
		this.canEnterHighScore = true;
		this.displayHighScores = false;
		this.tileSelected = null;
	}

	/** The paintComponent method is called whenever this component needs to be repainted.
	 * @param graphics The Graphics object.
	 */
	public void paintComponent(Graphics graphics) {
		/* Let superclass paint to fill in background. */
		super.paintComponent(graphics);

		Tile[][][] content = (this.b).getContent();
		ArrayList<Tile> removableTiles = (this.b).getRemovableTiles();
		
		if ((content == null) || (this.tileImages == null)) return;

		if ((this.layoutSelector).getLayoutSelected()) {
			/* Draw tiles back to front. */
			for (int z = 0; z < content.length; z++) {
				for (int y = 0; y < content[z].length; y++) {
					for (int x = 0; x < content[z][y].length; x++) {
						Tile t = content[z][y][x];
						if (t != null) {
							if (y > 0 && content[z][y - 1][x] != null && t.equals(content[z][y - 1][x])) {
								continue;
							}
							if (x > 0 && content[z][y][x - 1] != null && t.equals(content[z][y][x - 1])) {
								continue;
							}
							int val = t.getValue();
							int subInd = t.getSubindex();
							Image image = tileImages[val][subInd];
							if (b.free(t)) {
								image = tileImagesHL[val][subInd];
								if (this.isHintModeOn() && removableTiles.contains(t)) {
									if ((this.b).isPartOfQuadrupleMatching(t)) image = this.tileImagesHint1[val][subInd];
									else image = this.tileImagesHint2[val][subInd];
								}
								if (this.tileSelected == t) image = tileImagesSEL[val][subInd];
							}
							graphics.drawImage(image, x*TILEW + TILEW/2 + z*TILESKEW, (y + 1)*TILEH/2 - z*TILESKEW, null);
						}
					}
				}
			}
			
			switch ((this.gameStatus).getStatus()) {
				case PAUSE:
					this.displayPauseMessage(graphics);
					break;
				case WIN:
					this.displayWinMessage(graphics);
					if (this.displayHighScores) this.displayHighScores(graphics);
					break;
				case DEADLOCK:
					this.displayDeadlockMessage(graphics);
					break;
				default:
					break;
			}
		}
	}
	
	private void displayWinMessage (Graphics graphics) {
		int w = (this.getPreferredSize()).width;
		int h = (this.getPreferredSize()).height;
		
		graphics.setColor(new Color(50, 50, 50, 150));
		graphics.fillRect(0, 0, w, h);
		
		graphics.setColor(Color.yellow);
		graphics.setFont(new Font(Font.MONOSPACED, Font.BOLD, 100));
		graphics.drawString("CONGRATULATIONS!", w/2 - 480, h/2 - 60);
		graphics.drawString("YOU WIN!", w/2 - 240, h/2 + 20);
		
	}
	
	private void displayHighScores (Graphics graphics) {		
		int w = (this.getPreferredSize()).width;
		int h = (this.getPreferredSize()).height;
		
		graphics.setColor(Color.white);
		graphics.setFont(new Font(Font.MONOSPACED, Font.BOLD, 40));
		graphics.drawString("BEST TIMES", w/2 - 140, h/2 + 75);
		
		int lineStartX = w/2 - 215;
		int lineLength = 395;
		int lineY = h/2 + 86;
		graphics.drawLine(lineStartX, lineY, lineStartX + lineLength, lineY);
		graphics.drawLine(lineStartX, lineY - 1, lineStartX + lineLength, lineY - 1);
		graphics.drawLine(lineStartX, lineY - 2, lineStartX + lineLength, lineY - 2);
		
		graphics.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
		int scoreDisplayX = lineStartX;
		int firstScoreDisplayY = h/2 + 105;
		int spaceBetweenScores = 20;
		
		ArrayList<Score> highScoresList = (this.highScoreManager).getHighScores();
		for (int i = 0; i < highScoresList.size(); i++) {
			graphics.drawString(scoreToStringForWinScreen(i + 1, highScoresList.get(i)), scoreDisplayX, firstScoreDisplayY + i * spaceBetweenScores);
		}
		
	}
	
	private static String scoreToStringForWinScreen (int rank, Score sc) {
		String s = sc.getName();
		String t = sc.getTimeString();
		
		int displaySize = 30;
		if (rank > 9) displaySize--;
		for (int i = s.length() + t.length(); i < displaySize; i++)
			s += " ";
		return rank + ". " + s + t;
	}
	
	private void displayDeadlockMessage (Graphics graphics) {
		int w = this.getPreferredSize().width;
		int h = this.getPreferredSize().height;
		
		graphics.setColor(new Color(50, 50, 50, 150));
		graphics.fillRect(0, 0, w, h);
		
		graphics.setColor(Color.red);
		graphics.setFont(new Font(Font.MONOSPACED, Font.BOLD, 120));
		graphics.drawString("GAME OVER", w/2 - 340, h/2 - 40);
		graphics.setFont(new Font(Font.MONOSPACED, Font.BOLD, 90));
		graphics.drawString("DEADLOCK", w/2 - 230, h/2 + 40);
	}

	private void displayPauseMessage (Graphics graphics) {
		int w = this.getPreferredSize().width;
		int h = this.getPreferredSize().height;
		
		graphics.setColor(new Color(50, 50, 50, 250));
		graphics.fillRect(0, 0, w, h);
		
		graphics.setColor(Color.white);
		graphics.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 80));
		graphics.drawString("PAUSE", w/2 - 140, h/2);
	}
	
	public void updateStatus () {
		(this.gameStatus).updateStatus();
		if(!((this).isPlaying())) {
			(this.timerLabel).stopTimer();
			if ((this.gameStatus).isWin()) {
				this.repaint();
				
				if (this.canEnterHighScore) {
					long s = (this.timerLabel).getStopTime();
					if ((this.highScoreManager).isHighScore(s)) {
						String n = this.showHighScoreInputDialog();
						if (n != null && !n.equals("")) (this.highScoreManager).addScore(n, s);
					}
					
				}
				
				this.displayHighScores = true;
			}
		}
	}
		
	private String showHighScoreInputDialog () {
		String layoutTitle = LayoutSelector.getLayoutTitle(((this.b).getLayoutFile()).substring((LayoutSelector.LAYOUTDIR + java.io.File.separator).length()));
		String s = (String) JOptionPane.showInputDialog(this, 
					"Congratulations! You've achieved a top time for " + layoutTitle + "!\n" + 
					"Enter a name to go with your top time (Max. 20 Characters):",
					"You Made The Top Ten!",
					JOptionPane.PLAIN_MESSAGE,
					null, null, null);
		return s;
	}
	
	public void pause () {
		(this.gameStatus).pause();
		(this.timerLabel).stopTimer();
	}
	
	public void unPause () {
		(this.gameStatus).unPause();
		(this.timerLabel).startTimer();
	}
	
	public boolean isPlaying () {
		return (this.gameStatus).isPlaying();
	}
	
	public boolean isPaused () {
		return (this.gameStatus).isPaused();
	}
	
	public void appendMove (Tile t1, Tile t2) {
		(this.moveList).appendMove(t1, t2);
	}
	
	public void undoMove () {
		(this.moveList).undo();
	}
	
	public boolean undoUsed () {
		return (this.moveList).getUndoUsed();
	}
	
	public boolean canUndo () {
		return (this.moveList).canUndo();
	}
	
	public void toggleHintMode () {
		(this.gameStatus).toggleHintMode();
	}
	
	public void redoMove () {
		(this.moveList).redo();
	}
	
	public boolean isHintModeOn () {
		return (this.gameStatus).isHintModeOn();
	}
	
	public boolean hintUsed () {
		return (this.gameStatus).getHintUsed();
	}
	
	public void cantEnterHighScore () {
		this.canEnterHighScore = false;
	}
	
	public boolean getCanEnterHighScore () {
		return this.canEnterHighScore;
	}
	
	public Board getBoard () {
		return this.b;
	}

	public Image[][] getTileImages () {
		return this.tileImages;
	}

	public Image[][] getTileImagesHL () {
		return this.tileImagesHL;
	}
	
	public Tile getTileSelected () {
		return this.tileSelected;
	}

	public void setTileSelected (Tile tSelected) {
		this.tileSelected = tSelected;
	}
		
	public TimerLabel getTimerLabel () {
		return this.timerLabel;
	}
	
	public HighScoreManager getHighScoreManager () {
		return this.highScoreManager;
	}
	
	protected void setHighScoreManager (String layout) {
		this.highScoreManager = new HighScoreManager(layout);
	}
	
}