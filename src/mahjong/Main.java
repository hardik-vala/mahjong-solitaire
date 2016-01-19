package mahjong;

import java.io.*;
import java.util.Random;

import mahjong.Board.TileAssignment;

/** Main entry point to the mahjong game. Contains only static methods.
 */
public class Main {
    /** Directory where the individual tiles are found. */
	private static final String TILEDIR = "Tiles";
    /** Name of actual tileset file. */
	private static final String TILESET = "tiles.set";
    /** Default board layout. */
	private static final String LAYOUT_DEFAULT = "turtle.layout";
	
    /** Mutable directory where the individual tiles are found. */
    private static String tileDir = TILEDIR + java.io.File.separator;
    /** Mutable name of tileset file. */
    private static String tileSetFile = TILEDIR + java.io.File.separator + TILESET;
    /** Random seed, for repeatability. */
    private static long seed;
    /** Name of board layout file. */
    private static String layoutFileInitial = LayoutSelector.LAYOUTDIR + java.io.File.separator + LAYOUT_DEFAULT;
    private static Board.TileAssignment strategyInitial = TileAssignment.SOLVABLE;
    
	/**
	 * @param args
	 */
	public static void main (String[] args) {
    	Board b = null;
        // parse arguments; this will create a board too
        parse(args);
        // construct a board, seeded or really random
        try {
        	if (seed == 0L) {
        		b = new Board(layoutFileInitial, strategyInitial);
        	} else {
        		b = new Board(new Random(seed), layoutFileInitial, strategyInitial);
        	}
        } catch (IOException e) {
        	System.err.println(layoutFileInitial + " is an invalid path.");
        	System.err.println(e.getMessage()); ///////
                System.exit(1);
        }
        
        // construct the Player and the GUI
        JViewer v = null;
		try {
			v = new JViewer(LayoutSelector.getLayoutTitle(layoutFileInitial.substring((LayoutSelector.LAYOUTDIR + java.io.File.separator).length())), b);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/** Displays default usage flags. */
    public static void help() {
        System.out.println("Usage: java mahjong.Main [ -h | -seed n | -tiledir dir | -tileset tiles.set | -f board.layout | -s RANDOM ]*");
        System.out.println("Where:");
        System.out.println("    -h                 This help");
        System.out.println("    -seed n            Specify random seed");
        if (seed != 0L) {
            System.out.println("                        (currently=" + seed + ")");
        }
        System.out.println("    -tiledir dir       Directory with tile files (ends in a path separator)");
        System.out.println("                        (currently=" + tileDir + ")");
        System.out.println("    -tileset filename  Name of file that maps tiles to image files");
        System.out.println("                        (currently=" + tileSetFile + ")");
        System.out.println("    -f filename        Load the specified board layout");
        System.out.println("                        (currently=" + layoutFileInitial + ")");
        
        System.out.println("    -s strategy        Use the specified tile assignment strategy (RANDOM or SOLVABLE)");
        System.out.println("                        (currently=" + strategyInitial + ")");
    }

	/** Method to parse argument array and set appropriate values.
     * @param args The argument array given to main.
     */
    private static void parse(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if ("-seed".equals(args[i])) {
                i++;
                try {
                    seed = Long.parseLong(args[i]);
                } catch (NumberFormatException nfe) {
                    System.out.println(nfe);
                    seed = 0L;
                }
                System.out.println("Seed set to " + seed);
            } else if ("-h".equals(args[i])) {
                help();
                System.exit(0);
            } else if ("-f".equals(args[i])) {
                i++;
                layoutFileInitial = LayoutSelector.LAYOUTDIR + java.io.File.separator + args[i];
            } else if ("-tiledir".equals(args[i])) {
                i++;
                tileDir = args[i];
            } else if ("-tileset".equals(args[i])) {
                i++;
                tileSetFile = args[i];
            } else if ("-s".equals(args[i])) {
                i++;
                if ("RANDOM".equals(args[i])) {
                } else if ("SOLVABLE".equals(args[i])) {
                	strategyInitial = Board.TileAssignment.SOLVABLE;
                } else {
                	System.out.println("Unrecognized tile assignment strategy: " + args[i]);
                    help();
                    System.exit(1);
                }
            } else {
                System.out.println("Unrecognized option: " + args[i]);
                help();
                System.exit(1);
            }
        }
    }

    /** Getter for the the tile set file name.
     * @return The name of the file containing the tile set description.
     */
    public static String getTileSetFile() {
    	return tileSetFile;
    }

    /** Getter for the tile set directory name.
     * @return The directory containing the tiles (Ends in directory separator).
     */
    public static String getTileDir() {
    	return tileDir;
    }
    
}
