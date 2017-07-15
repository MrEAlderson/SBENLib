package de.marcely.sbenlib.server;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;

import de.marcely.sbenlib.compression.Base64;
import de.marcely.sbenlib.network.ByteArraysCombiner;
import de.marcely.sbenlib.network.ConnectionState;
import de.marcely.sbenlib.network.Network;
import de.marcely.sbenlib.network.packets.Packet;
import de.marcely.sbenlib.network.packets.PacketLogin;
import de.marcely.sbenlib.network.packets.PacketLoginReply;
import de.marcely.sbenlib.network.packets.PacketPing;
import de.marcely.sbenlib.network.packets.PacketPong;
import de.marcely.sbenlib.server.protocol.Protocol;
import lombok.Getter;

public class SocketHandler {
	
	@Getter private final SBENServer server;
	@Getter private final Protocol protocol;
	@Getter private final HashMap<String, Session> sessions = new HashMap<String, Session>();
	
	private ByteArraysCombiner combiner;
	
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

			public List<Session> getSessions(){
				return (List<Session>) sessions.values();
			}

			public Session getSession(InetAddress address, int port){
				final String identifier = address.getHostAddress() + ":" + port;
				
				return sessions.get(identifier);
			}
			
		}, maxClients);
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
			closeSession(session, "SERVER_CLOSED");
		
		return protocol.close();
	}
	
	public boolean sendPacket(Session session, Packet packet){
		return protocol.sendPacket(session, packet);
	}
	
	public boolean closeSession(Session session, String reason){
		if(!session.isConnected())
			return false;
		
		final boolean success = protocol.closeSession(session, reason);
		
		this.sessions.remove(session.getIdentifier());
		for(SessionEventListener listener:session.getListeners())
			listener.onDisconnect(reason);
		
		session.setConnectionState(ConnectionState.Disconnected);
		
		return success;
	}
	
	
	
	
	private void workWithPacket(Session session, PacketLogin packet){
		final PacketLoginReply packet_reply = new PacketLoginReply();
		
		if(packet.version_protocol == Network.PROTOCOL_VERSION){
			packet_reply.reply = PacketLoginReply.REPLY_SUCCESS;
			
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
}
