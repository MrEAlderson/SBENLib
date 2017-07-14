package de.marcely.sbenlib.server;

import java.net.InetAddress;

import de.marcely.sbenlib.network.packets.Packet;
import lombok.Getter;

public class Session {
	
	@Getter private final SBENServer server;
	@Getter private final InetAddress address;
	@Getter private final int port;
	@Getter private final Thread thread;
	@Getter private final Object obj;
	
	public Session(SBENServer server, InetAddress address, int port){
		this(server, address, port, null, null);
	}
	
	public Session(SBENServer server, InetAddress address, int port, Thread thread, Object obj){
		this.server = server;
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
	
	public void sendPacket(Packet packet){
		server.getSocketHandler().sendPacket(this, packet);
	}
}
