package mahjong;

public class GameStatus {

	public enum Status {PLAYING, PAUSE, DEADLOCK, WIN};
	
	private Board b;
	private Status status;
	private boolean hintMode;
	private boolean hintUsed;
	
	public GameStatus (Board b) {
		this.b = b;
		this.status = Status.PLAYING;
		this.hintMode = false;
	}
	
	public Status getStatus () {
		return this.status;
	}

	public void updateStatus () {
		if ((this.b).isEmpty()) this.status = Status.WIN;
		else if (!(this.b).isSolvable()) this.status = Status.DEADLOCK;
		else if (this.status != Status.PAUSE) {
			this.status = Status.PLAYING;
		}
	}
		
	public void reset () {
		this.unPause();
		this.updateStatus();
		if (this.hintMode) this.hintMode = !this.hintMode;
		this.hintUsed = false;
	}
	
	public void pause () {
		this.status = Status.PAUSE;
	}
	
	public void unPause () {
		this.status = Status.PLAYING;
	}
	
	public void toggleHintMode () {
		if (this.status == Status.PLAYING) this.hintMode = !this.hintMode;
		this.hintUsed = true;
	}
	
	protected boolean getHintUsed () {
		return this.hintUsed;
	}
	
	public boolean isPlaying () {
		return (this.status == Status.PLAYING);
	}
	
	public boolean isPaused () {
		return (this.status == Status.PAUSE);
	}
	
	public boolean isHintModeOn () {
		return this.hintMode;
	}
	
	public boolean isWin () {
		return (this.status == Status.WIN);
	}
	
	public String toString () {
		String s = null;
		switch(this.status) {
			case PLAYING:
				s = "PLAYING";
				break;
			case PAUSE:
				s = "PAUSE";
				break;
			case DEADLOCK:
				s = "DEADLOCK";
				break;
			case WIN:
				s = "WIN";
				break;
		}
		
		return s;
	}
}
