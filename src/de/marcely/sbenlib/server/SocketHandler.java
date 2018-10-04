package de.marcely.sbenlib.server;

import java.net.InetAddress;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;

import de.marcely.sbenlib.network.ConnectionState;
import de.marcely.sbenlib.network.packets.PacketClose;
import de.marcely.sbenlib.server.protocol.Protocol;
import de.marcely.sbenlib.util.TickTimer;
import lombok.Getter;

public class SocketHandler {
	
	@Getter private final SBENServer server;
	@Getter private final Protocol protocol;
	@Getter private final HashMap<String, Session> sessions = new HashMap<String, Session>();
	
	// packet handler
	private Queue<RawPacket> packetsQueue = new ArrayDeque<RawPacket>();
	private final TickTimer packetHandlerTimer;
	
	public SocketHandler(final SBENServer server, int maxClients){
		this.server = server;
		
		this.protocol = server.getConnectionInfo().protocol.getServerInstance(server.getConnectionInfo(), server, new ServerEventListener(){
			public void onClientRequest(Session session){
				session.setConnectionState(ConnectionState.Connecting);
				
				sessions.put(session.getIdentifier(), session);
			}

			public void onClientDisconnect(Session session){
				session.setConnectionState(ConnectionState.Disconnected);
				
				sessions.remove(session.getIdentifier());
			}

			public void onPacketReceive(Session session, byte[] data){
				packetsQueue.add(new RawPacket(session, data));
			}

			public List<Session> getSessions(){
				return (List<Session>) sessions.values();
			}

			public Session getSession(InetAddress address, int port){
				final String identifier = address.getHostAddress() + ":" + port;
				
				return sessions.get(identifier);
			}
			
		}, maxClients);
		
		// raw packet byte data to actual packet instance
		this.packetHandlerTimer = new TickTimer(true, 100){
			public void onRun(){
				// timeout
				for(Session s:new ArrayList<Session>(sessions.values())){
					if(System.currentTimeMillis() - 1000*10 > s.pingLastUpdate)
						s.close("CLIENT_TIMEOUT");
					
					s.getTransmitter().sendNacks();
				}
				
				RawPacket packet = null;
				
				while((packet = packetsQueue.poll()) != null){
					try{
						packet.session.getTransmitter().handlePacket(packet.packet);
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		};
		this.packetHandlerTimer.start();
	}
	
	public boolean isRunning(){
		return this.protocol.isRunning();
	}
	
	public int getMaxClients(){
		return this.protocol.getMaxClients();
	}
	
	public ServerStartInfo run(){
		return protocol.run();
	}
	
	public boolean close(){
		for(Session session:sessions.values())
			session.close("SERVER_CLOSED");
		
		packetHandlerTimer.stop();
		
		return protocol.close();
	}
	
	public boolean closeSession(Session session, String reason){
		if(!session.isConnected())
			return false;
		
		// send packet
		final PacketClose packet = new PacketClose();
		
		packet.reason = reason;
		
		try{
			session.getTransmitter().sendPacket(packet, false);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		// remove from cache
		this.sessions.remove(session.getIdentifier());
		
		final boolean success = protocol.closeSession(session, reason);
		
		for(SessionEventListener listener:session.getListeners())
			listener.onDisconnect(reason);
		
		// tell it the server
		session.setConnectionState(ConnectionState.Disconnected);
		
		return success;
	}
	
	
	
	private static class RawPacket {
		
		public final Session session;
		public final byte[] packet;
		
		public RawPacket(Session session, byte[] packet){
			this.session = session;
			this.packet = packet;
		}
	}
}
