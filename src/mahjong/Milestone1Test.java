package mahjong;

import java.io.IOException;

import junit.framework.*;

public class Milestone1Test extends TestCase {
	
	private static final String TURTLELAYOUTFILE = "Layouts" + java.io.File.separator + "turtle.layout";
	private static final String ZIGGURATLAYOUTFILE = "Layouts" + java.io.File.separator + "ziggurat.layout";
	private static final String EMPTYLAYOUTFILE = "Layouts" + java.io.File.separator + "empty.layout";
		
	public void testLayoutDimensions() throws IOException {
		int[] d1 = Board.LayoutDimensions(TURTLELAYOUTFILE);
			
		int[] d2 = new int[3];
		d2[0] = 30;
		d2[1] = 16;
		d2[2] = 5;
		
		assertTrue((d1[0] == d2[0]) && (d1[1] == d2[1]) && (d1[2] == d2[2]));
		
		d1 = Board.LayoutDimensions(ZIGGURATLAYOUTFILE);
			
		d2[2] = 6;
			
		assertTrue((d1[0] == d2[0]) && (d1[1] == d2[1]) && (d1[2] == d2[2]));
	}
		
		public void testBuild() throws IOException {	
			Board board = new Board(TURTLELAYOUTFILE, Board.TileAssignment.SOLVABLE);
			board.printContents(); //Printed output is compared to layout file
			
			board = new Board(ZIGGURATLAYOUTFILE, Board.TileAssignment.SOLVABLE);
			board.printContents();
		}
		
		public void testIsEmpty() throws IOException {
			Board board = new Board(EMPTYLAYOUTFILE, Board.TileAssignment.RANDOM);
			assertTrue(board.isEmpty());
		}
		
		/* Implicitly tests the correctness of free(). */
		public void testBuildFreeTiles() throws IOException {
			Board board = new Board(TURTLELAYOUTFILE, Board.TileAssignment.SOLVABLE);
			System.out.println("\nFree tile list contents for turtle.layout: \n");
			board.printFreeTiles(); //Printed output is compared to layout file
			
			board = new Board(ZIGGURATLAYOUTFILE, Board.TileAssignment.SOLVABLE);
			System.out.println("\nFree tile list contents for ziggurat.layout: \n");
			board.printFreeTiles();
		}
		
		/* Implicitly tests the correctness of free(). */
		public void testRemove() throws IOException {
			Board board = new Board(TURTLELAYOUTFILE, Board.TileAssignment.SOLVABLE);
			
			int numTiles = (Board.MAXGROUPS)*(Board.GROUPSIZE);
			Tile t;
			
			for (int i = 0; i < numTiles; i++) {
				t = board.getFreeTiles().get(0);
				int z = t.getZ();
				int y = t.getY();
				int x = t.getX();
				board.remove(board.getFreeTiles().get(0));
				assertTrue((board.getContent()[z][y][x] == null) &&
						(board.getContent()[z][y][x + 1] == null) &&
						(board.getContent()[z][y + 1][x] == null) &&
						(board.getContent()[z][y + 1][x + 1] == null) &&
						!(board.getFreeTiles().contains(t)));
				
				
			}
			
			assertTrue(board.isEmpty());
			
			board = new Board(ZIGGURATLAYOUTFILE, Board.TileAssignment.SOLVABLE);
				
			for (int i = 0; i < numTiles; i++) {
				t = board.getFreeTiles().get(0);
				int z = t.getZ();
				int y = t.getY();
				int x = t.getX();
				board.remove(board.getFreeTiles().get(0));
				assertTrue((board.getContent()[z][y][x] == null) &&
						(board.getContent()[z][y][x + 1] == null) &&
						(board.getContent()[z][y + 1][x] == null) &&
						(board.getContent()[z][y + 1][x + 1] == null) &&
						!(board.getFreeTiles().contains(t)));
				
				
			}
			
			assertTrue(board.isEmpty());
		}
		
}
