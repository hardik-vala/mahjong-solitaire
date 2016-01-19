package mahjong;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class HighScoreManager implements Serializable {

	private static final long serialVersionUID = -8321573007832005601L;
	private static final String HIGHSCORE_DIR = "HighScoreLists";
	private static final String HIGHSCORE_FILE_EXT = ".highScores";
	private static final int MAX_NUM_HIGHSCORES = 10;
	
	private String layout;
	private ArrayList<Score> highScoreList;
		
	public HighScoreManager (String l) {
		this.layout = l;
		this.highScoreList = new ArrayList<Score>();
	}
	
	@SuppressWarnings("unchecked")
	private void loadHighScoreFile () {
		File highScoreFile = new File(HIGHSCORE_DIR + java.io.File.separator + this.layout + HIGHSCORE_FILE_EXT);
		if (!highScoreFile.exists()) {
			try {
				highScoreFile.createNewFile();
			} catch (IOException e) {
				System.err.println("Error creating file: " + HIGHSCORE_DIR + java.io.File.separator + this.layout + HIGHSCORE_FILE_EXT);
				e.printStackTrace();
			}
		} else if (highScoreFile.length() > 0) {
			ObjectInputStream inputStream = null;
			try {
				inputStream = new ObjectInputStream(new FileInputStream(HIGHSCORE_DIR + java.io.File.separator + this.layout + HIGHSCORE_FILE_EXT));
				this.highScoreList = (ArrayList<Score>) inputStream.readObject();
			} catch (FileNotFoundException e) {
				e.printStackTrace(); 
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				System.err.println("Class Not Found error: " + e.getMessage());
				e.printStackTrace();
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
						System.err.println("IO error: " + e.getMessage());
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private void saveHighScoreFile () {		
		File highScoreFile = new File(HIGHSCORE_DIR + java.io.File.separator + this.layout + HIGHSCORE_FILE_EXT);
		if (!highScoreFile.exists()) {
			try {
				highScoreFile.createNewFile();
			} catch (IOException e) {
				System.err.println("Error creating file: " + HIGHSCORE_DIR + java.io.File.separator + this.layout + HIGHSCORE_FILE_EXT);
				e.printStackTrace();
			}
		}
		
		ObjectOutputStream outputStream = null;
		try {
			outputStream = new ObjectOutputStream(new FileOutputStream(HIGHSCORE_DIR + java.io.File.separator + this.layout + HIGHSCORE_FILE_EXT));
			outputStream.writeObject(this.highScoreList);
		} catch (FileNotFoundException e) {
			e.printStackTrace(); 
		} catch (IOException e) {
			System.err.println("IO error: " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if (outputStream != null) {
					(outputStream).flush();
					(outputStream).close();
				}
			} catch (IOException e) {
				System.err.println("IO error: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
		
	public ArrayList<Score> getHighScores() {
		this.loadHighScoreFile();
		this.sort();
		return this.highScoreList;
	}
	
	public void addScore (String n, long s) {
		this.loadHighScoreFile();
				
		(this.highScoreList).add(new Score(n, s));
		int size = (this.highScoreList).size();
		if (size > MAX_NUM_HIGHSCORES) {
			this.sort();
			for (int i = size - 1; i > (MAX_NUM_HIGHSCORES - 1); i--) this.highScoreList.remove(i);
		}
		
		this.saveHighScoreFile();
	}
	
	private void sort () {
		Collections.sort(this.highScoreList, new ScoreComparator());
	}
		
	public boolean isHighScore (long s) {
		this.loadHighScoreFile();
		this.sort();
		
		if ((this.highScoreList).size() < MAX_NUM_HIGHSCORES) return true;
		
		for (Score sc: this.highScoreList) {
			if (s < sc.score) return true;
		}
		
		return false;
	}
	
	protected void setLayout (String l) {
		this.layout = l;
	}
	
	public String toString () {
		String s = "Layout: " + this.layout + "\nHigh Score List:\n";
				
		ArrayList<Score> scs = this.getHighScores();
		for (int i = 0; i < scs.size(); i++) {
			Score sc = scs.get(i);
			s += (i + 1) + ". " + sc;
			if (i < (scs.size() - 1)) s += "\n";
		}
		
		return s;
	}
	
	private void printHighScoreList () {
		for (int i = 0; i < (this.highScoreList).size(); i++) {
			System.out.println((i + 1) + ". " + (this.highScoreList).get(i));
		}
	}
	
	public class Score implements Serializable {
		private static final long serialVersionUID = 6261843302531198842L;
		private String name;
		private long score;
		
		public Score (String n, long s) {
			this.name = n;
			this.score = s;
		}
		
		public String getName () {
			return name;
		}

		public long getScore () {
			return score;
		}

		public String getTimeString () {
			String minString = String.format("%02d", (this.score / TimerLabel.numSecPerMin) % TimerLabel.numMinPerHour);
			String secString = String.format("%02d", this.score % TimerLabel.numSecPerMin);
			return ((this.score / TimerLabel.numSecPerMin) / TimerLabel.numMinPerHour) + ":" + minString + ":" + secString;
		}
		
		public String toString () {
			return "Name: " + this.name + ", Score: " + this.score; 
		}
	}
	
	private class ScoreComparator implements Comparator<Score>, Serializable {
		private static final long serialVersionUID = 6491507616224828865L;

		public int compare (Score s1, Score s2) {			
			return (int)(s1.score - s2.score);
		}	
	}
		
}