package mahjong;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JOptionPane;

public class LayoutSelector {

	public static final String LAYOUT_EXT = ".layout";
	/** Name of board layout directory. */
	protected static final String LAYOUTDIR = "Layouts";
	
	private HashMap<String, File> layouts;
	private boolean layoutSelected;
	Viewer v;
	
	public LayoutSelector (Viewer v) throws IOException {
		File layoutDir = new File(LAYOUTDIR);
		File[] layoutFiles = layoutDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				return filename.endsWith(LAYOUT_EXT);
			}
		});
		if (layoutFiles.length == 0) throw new IOException("Directory " + layoutDir + " does not contain any layout Files (i.e. Files with extension " + LAYOUT_EXT + ").");
		
		this.layouts = new HashMap<String, File>();
		for (File f : layoutFiles) {
			String t = getLayoutTitle((f.getAbsolutePath()).substring((layoutDir.getAbsolutePath() + File.separator).length()));
			(this.layouts).put(t, f);
		}
		
		this.layoutSelected = false;
		this.v = v;
	}
	
    protected static String getLayoutTitle (String l) {
    	String titleLow = l.substring(0, l.length() - (LAYOUT_EXT).length());
    	String firstLetUpper = (titleLow.substring(0, 1)).toUpperCase();
    	String restOfTitle = titleLow.substring(1);
    	return firstLetUpper + restOfTitle;
    }
	
    public String showLayoutSelectorDialog () {   	
    	String s = null;
    	do {
    		s = (String) JOptionPane.showInputDialog(this.v, "Select a layout:", "Layout", JOptionPane.QUESTION_MESSAGE, null, ((this.layouts).keySet()).toArray(), ((this.layouts).keySet()).toArray()[0]);
    		if (s == null) JOptionPane.showMessageDialog(this.v, "You must select a layout!", "Layout", JOptionPane.ERROR_MESSAGE);
    	} while (s == null);
    	
    	return s;
    }
    
    public void layoutSelected () {
    	this.layoutSelected = true;
    }
    
    public void reset () {
    	this.layoutSelected = false;
    }
    
    public boolean getLayoutSelected () {
    	return this.layoutSelected;
    }
    
}
