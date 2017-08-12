package de.marcely.sbenlib.server;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.crypto.spec.SecretKeySpec;

import de.marcely.sbenlib.network.ConnectionState;
import de.marcely.sbenlib.network.packets.Packet;
import de.marcely.sbenlib.network.packets.data.DataPacket;
import lombok.Getter;
import lombok.Setter;

public class Session {
	
	@Getter private final SBENServer server;
	@Getter private final InetAddress address;
	@Getter private final int port;
	@Getter private final Thread thread;
	@Getter private final Object[] obj;
	
	@Getter private ConnectionState connectionState = ConnectionState.NotStarted;
	@Getter @Setter private long ping = 0;
	@Getter @Setter private SecretKeySpec key;
	
	@Getter private List<SessionEventListener> listeners = new ArrayList<SessionEventListener>();
	public long pingLastUpdate = System.currentTimeMillis();
	
	public Session(SBENServer server, InetAddress address, int port){
		this(server, address, port, null, new Object[0]);
	}
	
	public Session(SBENServer server, InetAddress address, int port, Thread thread, Object... obj){
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
	
	public void sendPacket(DataPacket packet){
		server.getSocketHandler().sendPacket(this, packet);
	}
	
	public void setConnectionState(ConnectionState state){
		if(this.connectionState == state)
			return;
		
		for(SessionEventListener listener:listeners)
			listener.onStateChange(state);
		
		this.connectionState = state;
	}
	
	public void registerListener(SessionEventListener listener){
		this.listeners.add(listener);
	}
	
	public boolean unregisterListener(SessionEventListener listener){
		return this.listeners.remove(listener);
	}
	
	public void close(){
		close("SOCKET_CLIENT_CLOSED");
	}
	
	public void close(@Nullable String reason){
		if(reason == null)
			reason = "UNKOWN";
		
		server.getSocketHandler().closeSession(this, reason);
	}
	
	public boolean isConnected(){
		return server.getSocketHandler().getSessions().containsKey(getIdentifier());
	}
}
