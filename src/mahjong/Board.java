package mahjong;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/** This class represents a Mahjong board with tiles in it.
 */
public class Board {
	/** Constant number of tiles in a group. */
    public static final int GROUPSIZE = 4;
    /** Constant number of groups. */
    public static final int MAXGROUPS = 36;

    /** The random variable used to construct the board. */
    private Random r;

    /** The actual board is a 3D array of Tile objects. Each tile actually occupies a block
     * of 4 cells in the same dimension to allow for partial overlap of tiles. */
    private Tile[][][] content;

    /** Dimensions of board measured in terms of half tile (height) length. */
    private int height;
    /** Dimensions of board measured in terms of half tile (width) length. */
    private int width;
    /** Depth of the board in terms of layers. */
    private int depth;

    /** Directory path for layout file. */
    private String layoutFile;

    /** List for storing all free tiles on the board. */
    private ArrayList<Tile> freeTiles;

    /** Designates different tile assignment strategies. 
     * The TileAssignment.VOID strategy assigns distinct invalid group value and subindex
     * values to each distinct tile; represents a void tile assignment and mainly used for
     * the purpose of aiding in the implementation of the other strategies.
     * The TileAssignment.RANDOM strategy assigns valid value and subindex pairs to tiles
     * randomly such that each tile receives a distinct assignment.
     * The TileAssignment.SOLVABLE strategy assigns valid pairs to tiles randomly subject to
     * the constraint that the final board state be solvable.*/
    public enum TileAssignment { VOID, RANDOM, SOLVABLE };
    
    /** Tile assignment strategy used. */
    private TileAssignment tAssignment;
    
    /** Construct a new board from a given template file.
     * @param layoutFile Template file name.
     * @param s Tile assignment strategy.
     * @throws IOException 
     */
    public Board(String layoutFile, TileAssignment s) throws IOException {
    	int[] d = LayoutDimensions(layoutFile);
    	
    	this.width = d[0];
    	this.height = d[1];
    	this.depth = d[2];
    	
    	this.layoutFile = layoutFile;
    	
    	this.freeTiles = new ArrayList<Tile>();
    	
    	this.tAssignment = s;
    	
    	this.build(null, layoutFile, s);
    }

    /** Construct a new board from a given template file, but using a given
     * (seeded) random variable.
     * @param r1 Random variable to use.
     * @param layoutFile Template file name.
     * @param s Tile assignment strategy.
     * @throws IOException 
     */
    public Board(Random r1, String layoutFile, TileAssignment s) throws IOException {
    	int[] d = LayoutDimensions(layoutFile);
    	
    	width = d[0];
    	height = d[1];
    	depth = d[2];
    	
    	this.layoutFile = layoutFile;
    	
    	this.freeTiles = new ArrayList<Tile>();
    	
    	this.tAssignment = s;

    	build(r, layoutFile, s);
    }

    /** Constructs a new board using a tile layout, assumed to be well-formed, specified by a
     * three dimensional tile array argument and using a given (seeded) random variable.
     * (Used specifically to create copies of existing boards.)
     * @param r1 Random variable to use.
     * @param c Tile array to use.
     */
    private Board(Random r, Tile[][][] c) {
    	this.r = r;
    	
    	/* Dimensions of the board correspond to dimensions of the Tile array. (Tile array is
    	 * assumed to be dimensionally structured according to the following convention.) */
    	this.depth = c.length;
    	this.height = c[0].length;
    	this.width = c[0][0].length;
    	
    	this.content = new Tile[this.depth][this.height][this.width];
    	
    	/* Boolean array reflects the structure of the input Tile array to track which tiles
    	 * have been copied to the contents of the current board. (Cell value of true
    	 * indicates that the cell contents have already been copied.) */
    	boolean[][][] tilesVisited = new boolean[depth][height][width];
    	
    	/* The information in the input Tile array is copied to the contents array of the 
    	 * current board. */
    	Tile t;
    	for (int z = 0; z < this.depth; z++) {
    		for (int y = 0; y < this.height; y++) {
    			for (int x = 0; x < this.width; x++) {
    				if (c[z][y][x] == null) {
    					this.content[z][y][x] = null;
    				} else {
    					if (!tilesVisited[z][y][x]) {
    						t = c[z][y][x];
    						this.content[z][y][x] = new Tile(t.getValue(), t.getSubindex());
    						t = this.content[z][y][x];
    						this.addTile(t, x, y, z);
							
							/* Since a single tile occupies four positions (Arranged
							 * contigously into a square) in the contents array, the
							 * corresponding positions in the tilesVisited array are marked
							 * true. */
							tilesVisited[z][y][x] = true;
							tilesVisited[z][y][x + 1] = true;
							tilesVisited[z][y + 1][x] = true;
							tilesVisited[z][y + 1][x + 1] = true;
    					}
    				}
    			}
    		}
    	}
    	
    	this.freeTiles = new ArrayList<Tile>();
    	buildFreeTiles();
    }

    /** Processes a layout file, assumed to be well-formed (See 
     * 'LayoutFileSpecifications.txt'), and computes the required board dimensions (The 
     * planar dimensions are measured in 1/2 tile sides and the height is measured in 
     * layers).
     * @param layoutFile Name of the layout file.
     * @return Integer array containing 3 dimensions of board.
     * @throws IOException
     */
    protected static int[] LayoutDimensions(String layoutFile) throws IOException {
    	BufferedReader reader1 = new BufferedReader(new FileReader(layoutFile));
    	BufferedReader reader2 = new BufferedReader(new FileReader(layoutFile));
    	
    	/* Layout file to be processed line by line. */
    	String L1 = reader1.readLine();
    	String L2 = reader2.readLine();
    	
    	int x = 0;
    	int y = 0;
    	int z = 0;
    	
    	int[] dimensions = new int[3];
    	
    	/* Each line from the beginning of the file is skipped until a line starting with '%'
    	 * is encountered, indicating that the information to immediately follow is the tile
    	 * arrangement for the first layer or level 0. */
    	while (L1.equals("") || (L1.charAt(0) != '%')) {
    		L1 = reader1.readLine();
    	}
    	
		L1 = reader1.readLine();
		L1 = reader1.readLine();
		/* The horizontal dimension of the board is calculated by iterating through the first
		 * line of the tile arrangement information for the first layer and counting the
		 * non-space characters (Each representing a unit on the board having width equal to
		 * 1/2 that of a single tile). */
		for (int i = 0; i < L1.length(); i++) {
			if(L1.charAt(i) != ' ') {
				x++;
			}
		}
		
		/* The vertical dimension of the board is calculated by counting the number of
		 * non-empty lines for the first layer (Each non-empty line begins with a character
		 * that represents a unit on the board having height equal to 1/2 that of a single
		 * tile). (The halting condition is either an EOF or a '%' character, the latter
		 * indicating the start of a new layer.) */
		while (L1 != null && (L1.charAt(0) != ' ') && (L1.charAt(0) != '%')) {
			/* Once a non-empty line is encountered, the next consecutive line is also
			 * non-empty as a result of the structure of the grid representation of the tile
			 * arrangement.*/
			y = y + 2;
			L1 = reader1.readLine();
			if (L1 == null) {
				break;
			}
			L1 = reader1.readLine();
			L1 = reader1.readLine();	
		}
		
		/* The third dimension of the board is calculated by counting the number of lines
		 * beginning with the character '%'. */
		while (L2 != null) {
    		if (!L2.equals("")) {
    			if (L2.charAt(0) == '%') {
    				z++;
    			}
    			L2 = reader2.readLine();
    		} else L2 = reader2.readLine();
    	}
		
		dimensions[0] = x;
		dimensions[1] = y;
		dimensions[2] = z;
		
		reader1.close();
		reader2.close();
		
    	return dimensions;
    }
    
    /** Basic setup routine. Uses or creates a random number generator and then initializes
     * the board and the GUI. 
     * @param r1 Random number generator to use.
     * @param layoutFile Template file name.
     * @throws IOException 
     */
    protected void build(Random r1, String layoutFile, TileAssignment s) throws IOException {
        if (r1 == null) {
            long seed = (new Random()).nextLong();
            this.r = new Random(seed);
//          System.out.println("Seed used: " + seed);
        } else {
            this.r = r1;
        }
    	
        this.content = new Tile[this.depth][this.height][this.width];
    	    	
    	int x;
    	int y;
    	int z = -1;
    	
    	BufferedReader reader = new BufferedReader(new FileReader(layoutFile));

    	String L = reader.readLine();
    	/* Used as a temporary group value and subindex value for all tiles on the current
    	 * board. */
    	int v = -1;
    	/* The information from the layout file is extracted line by line. */
    	while (L != null) {
    		if (L.equals("")) {
    			L = reader.readLine();
    		} else {
    			/* If true, then the next higher layer is to be filled. */
    			if (L.charAt(0) == '%') {
    				L = reader.readLine();
    				L = reader.readLine();
    				z++;
    				/* Loops over rows of for the given layer, ensuring it is filled with
    				 * tiles row by row. */
    				for (y = 0; y < this.height; y++) {
    					x = 0;
    					int i = 0;
    					/* Loops through the tiles for a given row. */
    					while (x < this.width) {
    						/* Space characters merely serve as delimiters for the template
    						 * file and so are ignored. */
   							if (L.charAt(i) == ' ') {
   								i++;
   							} else {
   								/* '0' characters mark empty locations on the board and so
   								 * are ignored. Also, if the location is already
   								 * filled, then it is ignored. */
   								if ((L.charAt(i) == '0') || (this.content[z][y][x] != null)) {
   									x++;
   									i++;
   								} else {
   									/* The given position in the board is filled by a new
   									 * tile if the character encountered in the template file
   									 * is not '0' and the corresponding cell in the 'content'
   									 * array is empty. */
   									if (this.content[z][y][x] == null) {
   										Tile t = new Tile(v, v);
   										this.addTile(t, x, y, z);
   	    								/* Decrementing v after each tile assignment
   	    								 * to the board ensures that each distinct tile
   	    								 * receives distinct group value and subindex pair
   	    								 * values. */
   	    								v--;
   	    								x = x + 2;
   	    								/* Handles special case when the representation of a
   	    								 * single tile in the layout file straddles a grid
   	    								 * cell boundary. */
   	    								if ((i < L.length() - 1) && (L.charAt(i + 1) == ' ')) {
   	    									i = i + 3;
   	    								} else {
   	    									i = i + 2;
   	    								}
   	    							}
   								}
    						}
    					}   					
    					L = reader.readLine();
    					if ((L != null) && L.equals("")) {
    						L = reader.readLine();
    					}
    				}
    			} else {
    				L = reader.readLine();
    			}
    		}
    	}
    
    	reader.close();
    	
    	tileAssigner(s);
    	
    	buildFreeTiles();
//		printFreeTiles();
    }

    /** Assigns each tile on the board with a valid group value and subindex pair according
     * to a pre-specified strategy.
     * @param s Choice of strategy
     */
    private void tileAssigner(TileAssignment s) {
    	if (s == TileAssignment.VOID) {
    		/* Boolean array reflects the structure of the current board's contents array and is
        	 * used to track which tiles have been assigned a value and subindex pair. (Cell
        	 * value of true indicates that the tile occupying the corresponding position has
        	 * already been assigned.)*/
    		boolean[][][] tilesVisited = new boolean[this.depth][this.height][this.width];
    		/* Each tile is assigned a group value and subindex of v, which represents an
    		 * invalid assignment, in the context of the game, since the value of v will
    		 * always be negative. */
    		int v = -1;
    		
        	for (int z = 0; z < this.depth; z++) {
        		for (int y = 0; y < this.height; y++) {
        			for (int x = 0; x < this.width; x++) {
        				if ((this.content[z][y][x] != null) && !tilesVisited[z][y][x]) {
        						Tile t = this.content[z][y][x];
        						
        						t.setValue(v);
        						t.setSubindex(v);
        						
    							tilesVisited[z][y][x] = true;
    							tilesVisited[z][y][x + 1] = true;
    							tilesVisited[z][y + 1][x] = true;
    							tilesVisited[z][y + 1][x + 1] = true;
    							
    							/* Decremented after each tile assignment to ensure each
    							 * tile receives a distinct value and subindex pair. */
    							v--;
        				}
        			}
        		}
        	}
    	}
    	if (s == TileAssignment.RANDOM) {
    		/* Array is indexed by all possible group value and subindex pairs, where the
        	 * contents of a given cell is true if and only if the index of the cell 
        	 * corresponds to the value and subindex of a tile already on the board. */
        	boolean[][] tileVI = new boolean[MAXGROUPS][GROUPSIZE];
        	boolean[][][] tilesVisited = new boolean[this.depth][this.height][this.width];
        	int value;
        	int subindex;
        	
    		for (int z = 0; z < this.depth; z++) {
        		for (int y = 0; y < this.height; y++) {
        			for (int x = 0; x < this.width; x++) {
        				if ((this.content[z][y][x] != null) && !tilesVisited[z][y][x]) {
        					Tile t = this.content[z][y][x];
        					
        					/* Tile is assigned value and subindex pair randomly subject
							 * to the condition that no other existing tile on the board
							 * as the same value pair. */
        					do {
								value = r.nextInt(MAXGROUPS);
								subindex = r.nextInt(GROUPSIZE);
							} while (tileVI[value][subindex]);
							/* Marked true to prevent other tiles from being assigned the
							 * same value and subindex. */
							tileVI[value][subindex] = true;
							
							t.setValue(value);
							t.setSubindex(subindex);
							
							tilesVisited[z][y][x] = true;
							tilesVisited[z][y][x + 1] = true;
							tilesVisited[z][y + 1][x] = true;
							tilesVisited[z][y + 1][x + 1] = true;
        				}
        			}
        		}
        	}
    	}
    	if (s == TileAssignment.SOLVABLE) {
        	boolean[][] tileVI;
        	/* Indicates whether the tile assignment procedure reaches an unsolvable board
        	 * state. */
    		boolean unsolvable;
        	int value;
        	int subindex;
    		Tile t;
    		Tile t1;
    		Tile t2;
    		
    		do {
    			unsolvable = false;
    			/* Current board is given a void tile assignment. */
    			tileAssigner(TileAssignment.VOID);
    			tileVI = new boolean[MAXGROUPS][GROUPSIZE];
    			/* Copy of the current board with void tile assignment is instantiated. */
    			Board boardCopy = new Board(r, this.content);
        		ArrayList<Tile> freeTilesCopy = boardCopy.getFreeTiles();
        		/* Each iteration of the loop assigns a pair of distinct free tiles on the
        		 * current board with the same value but different subindices and then
        		 * removes the corresponding tiles from boardCopy. The loop halts once
        		 * boardCopy is empty, indicating that all the tiles on the current board
        		 * have been assigned. */
        		while (!boardCopy.isEmpty()) {
        			/* If at any point the number of free tiles on boardCopy is one, then
        			 * the tile assignment so far reaches an unsolvable board state, i.e. a
        			 * dead end. */
        			if (freeTilesCopy.size() == 1) {
        				unsolvable = true;
        				break;
        			}
        			/* Pair of distinct free tiles is selected from boardCopy, which
        			 * correspond to distinct free tiles on the current board. */
        			do {
        				t1 = freeTilesCopy.get(r.nextInt(freeTilesCopy.size()));
        				t2 = freeTilesCopy.get(r.nextInt(freeTilesCopy.size()));
        			} while (t1 == t2);
        			do {
        				value = r.nextInt(MAXGROUPS);
        				subindex = r.nextInt(GROUPSIZE);
        			} while (tileVI[value][subindex]);
        			tileVI[value][subindex] = true;
        			
        			/* The corresponding tile in the current board to the first tile in the
        			 * chosen pair from boardCopy, is assigned the selected value and
        			 * subindex. */
        			t = content[t1.getZ()][t1.getY()][t1.getX()];
        			t.setValue(value);
        			t.setSubindex(subindex);
        			
        			/* Randomly selects a subindex value for the given group value that has
        			 * not been assigned to an existing tile. */
        			do {
        				subindex = r.nextInt(GROUPSIZE);
        			} while (tileVI[value][subindex]);
        			tileVI[value][subindex] = true;
        			
        			/* The correspond tile in the current board to the second tile in the
        			 * chosen pair from boardCopy is assigned the same value and the new
        			 * selected subindex. */
        			t = this.content[t2.getZ()][t2.getY()][t2.getX()];
        			t.setValue(value);
        			t.setSubindex(subindex);
        			
        			/* Pair of tiles are removed from boardCopy to indicate that the
        			 * corresponding tiles in the current board have already been properly
        			 * assigned. */
        			boardCopy.remove(t1);
        			boardCopy.remove(t2);
        		}
        	/* If at any point the board is not empty and becomes unsolvable, then the
        	 * tile assignment procedure is restarted and repeated until a solvable final
        	 * board state is achieved. */
    		} while (unsolvable);
    	} 
    	
    }
    
    /** Adds a given tile to the board at the specified position. (The input tile is assumed
     * to be non-null and the input coordinates are assumed to be valid with respect to the
     * board.)
     * @param t	Tile to add.
     * @param x x-coordinate of position.
     * @param y y-coordinate of position.
     * @param z z-coordinate of position.
     */
    private void addTile(Tile t, int x, int y, int z) {
    	t.setCoord(z, y, x);
		this.content[z][y][x] = t;
		this.content[z][y][x + 1] = t;
		this.content[z][y + 1][x] = t;
		this.content[z][y + 1][x + 1] = t;
    }
        
    /** Constructs the free tile list given the current board state.
     */
    protected void buildFreeTiles() {
    	Tile t;
    	
    	for (int k = 0; k < this.depth; k++) {
    		for (int j = 0; j < this.height; j++) {
    			for (int i = 0; i < this.width; i++) {
    				t = this.content[k][j][i];
    				if ((t != null) && this.free(t) && !this.freeTiles.contains(t)) {
    					this.freeTiles.add(this.content[k][j][i]);
    				}
    			}
    		}
    	}
    }
        
    /** Wipes the current board and constructs a new one using the current Random variable.
     * @throws IOException
     */
    public void reset() throws IOException {
    	int[] d = LayoutDimensions(layoutFile);
    	
    	this.width = d[0];
    	this.height = d[1];
    	this.depth = d[2];
    	
    	this.freeTiles.clear();
    	this.build(this.r, layoutFile, this.tAssignment);
    }

    /** Checks if the board is empty.
     * @return true if the board has no more tiles, false otherwise.
     */
    public boolean isEmpty() {
    	for (int k = 0; k < depth; k++) {
    		for (int j = 0; j < height; j++) {
    			for (int i = 0; i < width; i++) {
    				if (content[k][j][i] != null) {
    					return false;
    				}
    			}
    		}
    	}
    	return true;
    }

    /** Attempt to remove the given tile; the tile must be present.
     * @param t The tile to remove.
     * @return false if the tile wasn't present, or true if it was and successfully removed.
     */
    public boolean remove(Tile t) {
    	int x = t.getX();
    	int y = t.getY();
    	int z = t.getZ();
    	
    	if (content[z][y][x] != null) {
    		if (free(t)) {
    			content[z][y][x] = null;
    			content[z][y][x + 1] = null;
    			content[z][y + 1][x] = null;
    			content[z][y + 1][x + 1] = null;
    			/* Removed tile is also deleted from the 'freeTiles' list, which is then
    			 * reconstructed from the new board state. */
    			freeTiles.remove(t);
    			buildFreeTiles();
    			return true;
    		}
    	}
    	
        return false;
    }

    /** Checks if the given tile is free.
     * @param t The Tile to check.
     * @return true if the Tile is visible and can be removed, false otherwise.
     * */
    public boolean free(Tile t) {
    	int x = t.getX();
    	int y = t.getY();
    	int z = t.getZ();
    	
    	/* If the given tile, in addition to being visible, 
    	 * is the leftmost or rightmost tile on the board in the row that it coincides, then
    	 * it is free. */
    	if (isVisible(t)) {
    		if (x == 0 || ((content[z][y][x - 1] == null)
    			&& (content[z][y + 1][x - 1] == null))) {
    			return true;
    		}
    		if (x + 1 == width - 1 || ((content[z][y][x + 2] == null)
    			&& (content[z][y + 1][x + 2] == null))) {
    			return true;
    		}
    	}
    	
    	return false;
    }

    /** Checks if the given tile is visible, which is true if there are no tiles directly
     * above it.
     * @param t The tile to check.
     * @return true if the given tile is visible and false if the tile is completely or
     * partially covered by another tile.
     */
    private boolean isVisible(Tile t) {
    	int x = t.getX();
    	int y = t.getY();
    	int z = t.getZ();
    	
    	/* If the tile resides on the topmost layer, then the tile is trivially visible. */
    	if (z == this.depth - 1) {
    		return true;
    	} else {
    		if ((this.content[z + 1][y][x] == null) && 
    	    	(this.content[z + 1][y][x + 1] == null) &&
    	    	(this.content[z + 1][y + 1][x] == null) &&
    	    	(this.content[z + 1][y + 1][x + 1] == null)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    /** Getter.
     * @return Tile content.
     */
    public Tile[][][] getContent() {
    	return content;
    }

   /** Getter.
     * @return Board width in units representing half the width of a single tile. 
     */
    public int getWidth() {
        return width;
    }

    /** Getter.
     * @return Board height in units representing half the height of a single tile.
     */
    public int getHeight () {
        return height;
    }
    
    /** Getter.
     * @return Board depth in layers.
     */
    public int getDepth () {
    	return depth;
    }
    
    public String getLayoutFile () {
    	return this.layoutFile;
    }
    
    public Random getR () {
    	return this.r;
    }
    
    public TileAssignment getTileAssignment () {
    	return this.tAssignment;
    }
    
    protected void setLayoutFile (String l) {
    	this.layoutFile = l;
    }
        
    /** Getter.
     * @return Free tiles list.
     */
    public ArrayList<Tile> getFreeTiles () {
    	return this.freeTiles;
    }
    
    /** Checks whether the input pair of tiles can be removed from the board, i.e. checks
     * that they are non-null, both free, non-identical, and matching.
     * @param t1 First tile in pair.
     * @param t2 Second tile in pair.
     * @return true if the pair is removable, false otherwise.
     */
    public boolean isRemovablePair(Tile t1, Tile t2) {
    	return (t1 != null) && (t2 != null) && this.free(t1) && this.free(t2) && !t1.equals(t2) && t1.matches(t2);
    }
    
    /** Determines whether the board, in its current state, can be solved, i.e. there exists two
     *  tiles that can be matched.
     * @return true if the board is solvable.
     */
    public boolean isSolvable() {
//    	/* If the there are no free tiles on the board, i.e. the 'freeTiles' list is empty, 
//    	 * then the board is trivially unsolvable. */
//    	if (!(this.freeTiles).isEmpty()) {
//    		for (int i = 0; i < (this.freeTiles).size(); i++) {
//    			for (int j = 0; j < (this.freeTiles).size(); j++) {
//    				Tile t1 = (this.freeTiles).get(i);
//    				Tile t2 = (this.freeTiles).get(j);
//    				if (this.isRemovablePair(t1, t2)) {
//    					return true;
//    				}
//    			}
//    		}
//    	}
//    	return false;
    	
    	return !((this.getRemovableTiles()).isEmpty());
    }
    
    public ArrayList<Tile> getRemovableTiles() {
    	ArrayList<Tile> removableTiles = new ArrayList<Tile>();
    	
    	for (Tile t1: this.freeTiles) {
    		for (Tile t2: this.freeTiles) {
    			if (this.isRemovablePair(t1, t2)) {
    				if (!removableTiles.contains(t1)) removableTiles.add(t1);
    				if (!removableTiles.contains(t2)) removableTiles.add(t2);
    			}
        	}
    	}
      	
    	return removableTiles;
    }
    
    public ArrayList<Tile> getRemovableMatchingTiles(Tile t) {
    	ArrayList<Tile> removableTiles = this.getRemovableTiles();
    	ArrayList<Tile> removableMatchingTiles = new ArrayList<Tile>();
    	
    	for (Tile u: removableTiles) {
    		if (t.matches(u) && !t.equals(u)) removableMatchingTiles.add(u);
    	}
    	
    	return removableMatchingTiles;
    }
    
    public boolean isPartOfQuadrupleMatching(Tile t) {
    	return ((this.getRemovableMatchingTiles(t)).size() == 3);
    }
    
    /** Prints the information stored in the contents array. If a given position is null,
     * then 0 is printed, otherwise a 1 is printed. */
    protected void printContents() {
    	for (int k = 0; k < depth; k++) {
    		System.out.println("");
    		System.out.println("%Level " + k);
    		for (int j = 0; j < height; j++) {
    			System.out.println("");
    			for (int i = 0; i < width; i++) {
    				if (content[k][j][i] == null) {
    					System.out.print("0");
    				} else {
    					System.out.print("1");
    				}
    			}
    		}
    		System.out.println("");
    	}
    	System.out.println("");
	}
    
    /** Prints the contents of the 'freeTiles' list by printing the information associated
     * with each tile in the list.
     */
    protected void printFreeTiles() {
    	if (!freeTiles.isEmpty()) {
    		int i = 1;
    		for (Tile t: freeTiles) {
    			System.out.println("Free tile " + i + ":\n" + t.toString());
    			i++;
    		}
    	}
    }
    
}
