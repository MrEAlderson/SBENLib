package de.marcely.sbenlib.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import lombok.Getter;

// TODO
public abstract class TickTimer {
	
	@Getter private static final List<TickTimer> timers = new ArrayList<TickTimer>();
	
	@Getter private final boolean repeating;
	@Getter private final long start, delay;
	
	private Timer timer;
	
	public TickTimer(boolean repeating, long delay){
		this(repeating, delay, 0);
	}
	
	public TickTimer(boolean repeating, long delay, long start){
		this.repeating = repeating;
		this.delay = delay;
		this.start = start;
	}
	
	public boolean isRunning(){
		return timer != null;
	}
	
	public boolean start(){
		if(isRunning())
			return false;
		
		timer = new Timer();
		timers.add(this);
		if(repeating){
			timer.schedule(new TimerTask(){
				public void run(){
					onRun();
				}
			}, start, delay);
		}else{
			timer.schedule(new TimerTask(){
				public void run(){
					onRun();
				}
			}, delay);
		}
		
		return true;
	}
	
	public boolean stop(){
		if(!isRunning())
			return false;
		
		timers.remove(this);
		timer.cancel();
		timer = null;
		
		return true;
	}
	
	public abstract void onRun();
}
