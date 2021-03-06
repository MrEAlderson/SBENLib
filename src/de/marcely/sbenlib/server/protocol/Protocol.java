package de.marcely.sbenlib.server.protocol;

import de.marcely.sbenlib.network.ConnectionInfo;
import de.marcely.sbenlib.network.ProtocolType;
import de.marcely.sbenlib.network.packets.Packet;
import de.marcely.sbenlib.network.packets.PacketClose;
import de.marcely.sbenlib.server.SBENServer;
import de.marcely.sbenlib.server.ServerEventListener;
import de.marcely.sbenlib.server.ServerStartInfo;
import de.marcely.sbenlib.server.Session;
import de.marcely.sbenlib.util.SThread;
import de.marcely.sbenlib.util.TickTimer;
import lombok.Getter;

public abstract class Protocol {
	
	@Getter protected final SBENServer server;
	@Getter protected final ConnectionInfo connectionInfo;
	@Getter protected final ServerEventListener listener;
	@Getter protected final int maxClients;
	
	@Getter protected SThread thread = null; // TODO: Move to TickTimer
	@Getter protected TickTimer tickTimer = null;
	
	@Getter protected boolean running = false;
	
	public Protocol(ConnectionInfo conn, SBENServer server, ServerEventListener listener, int maxClients){
		this.server = server;
		this.connectionInfo = conn;
		this.listener = listener;
		this.maxClients = maxClients;
	}
	
	public ServerStartInfo run(){
		final ServerStartInfo info = _run();
		
		running = info == ServerStartInfo.SUCCESS;
		return info;
	}
	
	public boolean sendPacket(Session session, Packet packet){
		return sendPacket(session, packet.encode());
	}
	
	public boolean sendPacket(Session session, byte[] packet){
		return _sendPacket(session, packet);
	}
	
	public boolean closeSession(Session session, String reason){
		if(session.isConnected()){
			final PacketClose packet_close = new PacketClose();
			packet_close.reason = reason;
			packet_close.encode();
			
			sendPacket(session, packet_close);
		
			return _closeSession(session);
		}
		
		return false;
	}
	
	
	public abstract ProtocolType getType();
	
	protected abstract ServerStartInfo _run();
	
	public abstract boolean close();
	
	protected abstract boolean _closeSession(Session session);
	
	protected abstract boolean _sendPacket(Session session, byte[] packet);
}