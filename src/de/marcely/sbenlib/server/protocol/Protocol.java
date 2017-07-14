package de.marcely.sbenlib.server.protocol;

import de.marcely.sbenlib.network.ConnectionInfo;
import de.marcely.sbenlib.network.ProtocolType;
import de.marcely.sbenlib.server.ServerEventListener;
import de.marcely.sbenlib.server.ServerStartInfo;
import de.marcely.sbenlib.server.Session;
import lombok.Getter;

public abstract class Protocol {
	
	@Getter protected final ConnectionInfo connectionInfo;
	@Getter protected final ServerEventListener listener;
	@Getter protected final int maxClients;
	
	@Getter protected Thread thread = null;
	
	@Getter protected boolean running = false;
	
	public Protocol(ConnectionInfo conn, ServerEventListener listener, int maxClients){
		this.connectionInfo = conn;
		this.listener = listener;
		this.maxClients = maxClients;
	}
	
	public ServerStartInfo run(){
		final ServerStartInfo info = _run();
		
		running = info == ServerStartInfo.SUCCESS;
		return info;
	}
	
	
	public abstract ProtocolType getType();
	
	protected abstract ServerStartInfo _run();
	
	public abstract boolean close();
	
	public abstract boolean sendPacket(Session session, byte[] packet);
}