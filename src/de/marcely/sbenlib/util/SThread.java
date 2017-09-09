package de.marcely.sbenlib.util;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

public abstract class SThread extends Thread {
	
	@Getter private static final List<SThread> runningThreads = new ArrayList<SThread>();
	
	@Getter private final ThreadType type;
	
	public SThread(){
		this(ThreadType.Custom);
	}
	
	public SThread(ThreadType type){
		super();
		this.type = type;
	}
	
	@Override
	public void run(){
		runningThreads.add(this);
		_run();
		runningThreads.remove(this);
	}
	
	protected abstract void _run();
	
	
	
	public static enum ThreadType {
		Protocol_TCP_Client,
		Protocol_TCP_Server,
		Protocol_UDP_Client,
		Protocol_UDP_Server,
		ShutdownHook_Client,
		Custom;
	}
}