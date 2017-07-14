package de.marcely.sbenlib.server;

import java.net.InetAddress;

import lombok.Getter;

public class Session {
	
	@Getter private final InetAddress address;
	@Getter private final int port;
	@Getter private final Thread thread;
	@Getter private final Object obj;
	
	public Session(InetAddress address, int port){
		this(address, port, null, null);
	}
	
	public Session(InetAddress address, int port, Thread thread, Object obj){
		this.address = address;
		this.port = port;
		this.thread = thread;
		this.obj = obj;
	}
	
	public boolean hasThread(){
		return this.thread != null;
	}
	
	public String getIdentifier(){
		return address.getHostAddress() + ":" + port;
	}
}
