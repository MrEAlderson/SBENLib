package de.marcely.sbenlib.server;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.crypto.spec.SecretKeySpec;

import de.marcely.sbenlib.compression.Base64;
import de.marcely.sbenlib.network.ByteArraysCombiner;
import de.marcely.sbenlib.network.ConnectionState;
import de.marcely.sbenlib.network.Network;
import de.marcely.sbenlib.network.packets.Packet;
import de.marcely.sbenlib.network.packets.PacketData;
import de.marcely.sbenlib.network.packets.PacketLogin;
import de.marcely.sbenlib.network.packets.PacketLoginReply;
import de.marcely.sbenlib.network.packets.PacketPing;
import de.marcely.sbenlib.network.packets.PacketPong;
import de.marcely.sbenlib.network.packets.data.DataPacket;
import de.marcely.sbenlib.server.protocol.Protocol;
import de.marcely.sbenlib.util.TickTimer;
import lombok.Getter;

public class SocketHandler {
	
	@Getter private final SBENServer server;
	@Getter private final Protocol protocol;
	@Getter private final HashMap<String, Session> sessions = new HashMap<String, Session>();
	
	// packet handler
	private ByteArraysCombiner combiner;
	private List<RawPacket> packetsQuery = new ArrayList<RawPacket>();
	private final TickTimer packetHandlerTimer;
	
	public SocketHandler(SBENServer server, int maxClients){
		this.server = server;
		combiner = new ByteArraysCombiner(Packet.SEPERATOR[0]);
		
		this.protocol = server.getConnectionInfo().PROTOCOL.getServerInstance(server.getConnectionInfo(), server, new ServerEventListener(){
			public void onClientRequest(Session session){
				session.setConnectionState(ConnectionState.Connecting);
				
				sessions.put(session.getIdentifier(), session);
			}

			public void onClientDisconnect(Session session){
				session.setConnectionState(ConnectionState.Disconnected);
				
				sessions.remove(session.getIdentifier());
			}

			public void onPacketReceive(Session session, byte[] data){
				packetsQuery.add(new RawPacket(session, data));
			}

			public List<Session> getSessions(){
				return (List<Session>) sessions.values();
			}

			public Session getSession(InetAddress address, int port){
				final String identifier = address.getHostAddress() + ":" + port;
				
				return sessions.get(identifier);
			}
			
		}, maxClients);
		
		packetHandlerTimer = new TickTimer(true, 100){
			public void onRun(){
				// timeout
				for(Session s:new ArrayList<Session>(sessions.values())){
					if(System.currentTimeMillis() - 1000*10 > s.pingLastUpdate)
						s.close("CLIENT_TIMEOUT");
				}
				
				final List<RawPacket> rps = new ArrayList<RawPacket>(packetsQuery);
				
				if(rps.size() == 0)
					return;
				
				for(RawPacket rp:rps){
					final Session session = rp.session;
					final byte[] data = rp.packet;
					
					final List<byte[]> packets = combiner.addBytes(data);
					
					for(byte[] rawPacket:packets){
						// decode base64
						rawPacket = Base64.decode(rawPacket);
						
						// work with packet
						final byte id = rawPacket[0];
						
						switch(id){
						case Packet.TYPE_LOGIN:
							
							final PacketLogin packet_login = new PacketLogin();
							packet_login.decode(rawPacket);
							workWithPacket(session, packet_login);
							
							break;
							
						case Packet.TYPE_DATA:
							
							final PacketData packet_data = new PacketData();
							packet_data.packetsData = getServer().getPacketsData();
							packet_data.decode(rawPacket);
							workWithPacket(session, packet_data);
							
							break;
						case Packet.TYPE_ACK:
							break;
						case Packet.TYPE_NAK:
							break;
						case Packet.TYPE_PING:
							
							final PacketPing packet_ping = new PacketPing();
							packet_ping.decode(rawPacket);
							workWithPacket(session, packet_ping);
							
							break;
						}
					}
				}
				
				packetsQuery.removeAll(rps);
			}
		};
		packetHandlerTimer.start();
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
	
	public boolean sendPacket(Session session, DataPacket packet){
		final PacketData packet_data = new PacketData();
		packet_data.data = packet;
		packet_data.packetsData = getServer().getPacketsData();
		packet_data.encode();
		
		return sendPacket(session, packet_data);
	}
	
	public boolean sendPacket(Session session, Packet packet){
		return protocol.sendPacket(session, packet);
	}
	
	public boolean closeSession(Session session, String reason){
		if(!session.isConnected())
			return false;
		
		// remove from cache
		this.sessions.remove(session.getIdentifier());
		
		final boolean success = protocol.closeSession(session, reason);
		
		for(SessionEventListener listener:session.getListeners())
			listener.onDisconnect(reason);
		
		// tell it the server
		session.setConnectionState(ConnectionState.Disconnected);
		
		return success;
	}
	
	
	
	
	private void workWithPacket(Session session, PacketLogin packet){
		final PacketLoginReply packet_reply = new PacketLoginReply();
		
		if(packet.version_protocol == Network.PROTOCOL_VERSION){
			packet_reply.reply = PacketLoginReply.REPLY_SUCCESS;
			
			server.onSessionRequest(session);
			session.setKey(new SecretKeySpec(packet.security_id, "AES"));
			session.setConnectionState(ConnectionState.Connected);
			
		}else if(packet.version_protocol < Network.PROTOCOL_VERSION)
			packet_reply.reply = PacketLoginReply.REPLY_FAILED_PROTOCOL_OUTDATED_CLIENT;
		else
			packet_reply.reply = PacketLoginReply.REPLY_FAILED_PROTOCOL_OUTDATED_SERVER;
		
		packet_reply.encode();
		session.sendPacket(packet_reply);
	}
	
	private void workWithPacket(Session session, PacketPing packet){
		final PacketPong packet_pong = new PacketPong();
		
		packet_pong.time = packet.time;
		
		packet_pong.encode();
		session.sendPacket(packet_pong);
		
		// get ping
		session.setPing((packet.time - session.pingLastUpdate)-Network.PING_UPDATE);
		session.pingLastUpdate = packet.time;
	}
	
	private void workWithPacket(Session session, PacketData packet){
		final DataPacket dataPacket = packet.data;
		
		for(SessionEventListener listener:session.getListeners())
			listener.onPacketReceive(dataPacket);
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
