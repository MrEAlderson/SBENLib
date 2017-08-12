package de.marcely.sbenlib.util;

import java.util.Timer;
import java.util.TimerTask;

import lombok.Getter;

public abstract class TickTimer {
	
	@Getter private final long delay;
	
	private Timer timer;
	
	public TickTimer(long delay){
		this.delay = delay;
	}
	
	public boolean isRunning(){
		return timer != null;
	}
	
	public boolean start(){
		if(isRunning())
			return false;
		
		timer = new Timer();
		timer.schedule(new TimerTask(){
			public void run(){
				onRun();
			}
		}, 0, delay);
		
		return true;
	}
	
	public boolean stop(){
		if(!isRunning())
			return false;
		
		timer.cancel();
		timer = null;
		
		return true;
	}
	
	public abstract void onRun();
}
