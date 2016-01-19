package mahjong;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class ViewerMouseListener implements MouseListener {
	private Viewer v;
	private boolean firstClick;
	
	public ViewerMouseListener(Viewer v) {
		this.v = v;
		this.firstClick = false;
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		synchronized (this) {
			if ((this.v).isPlaying()) {
				if (!this.firstClick) {
					this.firstClick = true;
					this.v.getTimerLabel().startTimer();
				}
				Tile t = this.getTile(e.getX(), e.getY());
				Tile tSel = (this.v).getTileSelected();
				Board b = (this.v).getBoard();
				if (b.isRemovablePair(t, tSel)) {
					b.remove(t);
					b.remove(tSel);
					(this.v).appendMove(t, tSel);
					(this.v).setTileSelected(null);
					(this.v).updateStatus();
				} else {
					(this.v).setTileSelected(t);
				}
				(this.v).repaint();
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}
	
	private Tile getTile(int X, int Y) {
		Tile[][][] c = ((this.v).getBoard()).getContent();
		
		int x;
		int y;
		int z = c.length;
		do {
			z--;
			x = (X - Viewer.TILEW/2 - z*Viewer.TILESKEW)/Viewer.TILEW;
			y = 2*(Y + z*Viewer.TILESKEW)/Viewer.TILEH - 1;
			if ((x < 0) || (x >= c[0][0].length) || (y < 0) || (y >= c[0].length)) {
				return null;
			}
		} while((z > 0) && (c[z][y][x] == null));
		
		return c[z][y][x];
	}
	
	protected void resetFirstClick() {
		this.firstClick = false;
	}

}
