package mahjong;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.Timer;

@SuppressWarnings("serial")
public class TimerLabel extends JLabel {
	protected static final int numSecPerMin = 60;
	protected static final int numMinPerHour = 60;
	
	private long currentTime;
	private long stopTime;
	private Timer timer;
	
	public TimerLabel () {
		super("Time Elapsed: 0:00:00", CENTER);
		this.currentTime = 0;
		this.initTimer();
		this.setFont(new Font("Monospaced", Font.BOLD, 15));
		this.setBackground(Color.red);
		this.setForeground(Color.yellow);
	}

	private void initTimer() {
		final TimerLabel tl = this;
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tl.increment();
			}
		};
		this.timer = new Timer(1000, al);
	}
	
	public void startTimer() {
		this.timer.start();
	}
	
	public void stopTimer() {
		this.timer.stop();
		this.stopTime = this.currentTime;
	}
	
	public long getStopTime () {
		if (!(this.timer).isRunning()) return this.stopTime;
		return -1;
	}
		
	public void increment () {
		this.currentTime++;
		String minString = String.format("%02d", (this.currentTime / numSecPerMin) % numMinPerHour);
		String secString = String.format("%02d", this.currentTime % numSecPerMin);
		this.setText("Time Elapsed: " + ((this.currentTime / numSecPerMin) / numMinPerHour) + ":" + minString + ":" + secString);
	}
	
	public void reset () {
		this.timer.stop();
		this.currentTime = 0;
		this.setText("Time Elapsed: 0:00:00");
	}
}
