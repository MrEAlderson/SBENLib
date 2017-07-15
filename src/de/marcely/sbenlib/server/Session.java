package de.marcely.sbenlib.server;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import de.marcely.sbenlib.network.ConnectionState;
import de.marcely.sbenlib.network.packets.Packet;
import lombok.Getter;
import lombok.Setter;

public class Session {
	
	@Getter private final SBENServer server;
	@Getter private final InetAddress address;
	@Getter private final int port;
	@Getter private final Thread thread;
	@Getter private final Object obj;
	
	@Getter @Setter private ConnectionState connectionState = ConnectionState.NotStarted;
	@Getter @Setter private long ping = 0;
	
	@Getter private List<SessionEventListener> listeners = new ArrayList<SessionEventListener>();
	public long pingLastUpdate = 0;
	
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
