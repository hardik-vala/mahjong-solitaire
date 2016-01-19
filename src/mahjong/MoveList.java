package mahjong;

import java.util.ArrayList;

public class MoveList {
	
	private Board b;
	private ArrayList<Move> moveList;
	private int lastMoveIndex;
	private boolean undoUsed;
	
	public MoveList (Board b) {
		this.b = b;
		this.moveList = new ArrayList<Move>();
		this.lastMoveIndex = -1;
		this.undoUsed = false;
	}
	
	public boolean isEmpty() {
		return (this.moveList).isEmpty();
	}
	
	public void undo() {
		if (this.lastMoveIndex >= 0) {
			Move m = (this.moveList).get(this.lastMoveIndex);
			this.lastMoveIndex--;
			
			this.putBackTile(m.removedTile1);
			this.putBackTile(m.removedTile2);
			
			(this.b).buildFreeTiles();
			this.undoUsed = true;
		}
	}
	
	private void putBackTile (Tile t) {
		int x = t.getX();
		int y = t.getY();
		int z = t.getZ();
		
		Tile[][][] c = (this.b).getContent();
		
		c[z][y][x] = t;
		c[z][y][x + 1] = t;
		c[z][y + 1][x] = t;
		c[z][y + 1][x + 1] = t;
	}
	
	public boolean canUndo () {
		return (this.lastMoveIndex >= 0);
	}
	
	public void redo() {
		if (this.lastMoveIndex < ((this.moveList).size() - 1)) {
			this.lastMoveIndex++;
			Move m = (this.moveList).get(this.lastMoveIndex);
			
			(this.b).remove(m.removedTile1);
			(this.b).remove(m.removedTile2);
		}
	}
	
	private void append (Move m) {
		this.removeAfterLastMove();
		(this.moveList).add(m);
		this.lastMoveIndex++;
	}
	
	public void appendMove (Tile t1, Tile t2) {
		this.append(new Move(t1, t2));
	}
	
	public void reset () {
		(this.moveList).clear();
		this.lastMoveIndex = -1;
		this.undoUsed = false;
	}
	
	private void removeAfterLastMove () {
		if (!this.isEmpty()) {
			for (int i = (this.moveList).size() - 1; i > this.lastMoveIndex; i--) (this.moveList).remove((this.moveList).size() - 1);
		}
	}
	
	public boolean getUndoUsed () {
		return this.undoUsed;
	}
	
	public static class Move {
		private Tile removedTile1;
		private Tile removedTile2;
		
		public Move (Tile t1, Tile t2) {
			this.removedTile1 = t1;
			this.removedTile2 = t2;
		}
	}
}
