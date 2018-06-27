package de.marcely.sbenlib.server;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.crypto.spec.SecretKeySpec;

import de.marcely.sbenlib.network.ConnectionState;
import de.marcely.sbenlib.network.Network;
import de.marcely.sbenlib.network.PacketTransmitter;
import de.marcely.sbenlib.network.ProtocolType;
import de.marcely.sbenlib.network.packets.Packet;
import de.marcely.sbenlib.network.packets.PacketAck;
import de.marcely.sbenlib.network.packets.PacketData;
import de.marcely.sbenlib.network.packets.PacketLogin;
import de.marcely.sbenlib.network.packets.PacketLoginReply;
import de.marcely.sbenlib.network.packets.PacketNack;
import de.marcely.sbenlib.network.packets.PacketPing;
import de.marcely.sbenlib.network.packets.PacketPong;
import de.marcely.sbenlib.network.packets.data.DataPacket;
import de.marcely.sbenlib.network.packets.data.SecuredPacket;
import lombok.Getter;
import lombok.Setter;

public class Session {
	
	@Getter private final SBENServer server;
	@Getter private final InetAddress address;
	@Getter private final int port;
	@Getter private final Thread thread;
	@Getter private final Object[] obj;
	@Getter private final PacketTransmitter transmitter;
	
	@Getter private ConnectionState connectionState = ConnectionState.NotStarted;
	@Getter @Setter private long ping = 0;
	@Getter @Setter private SecretKeySpec key;
	
	@Getter private List<SessionEventListener> listeners = new ArrayList<SessionEventListener>();
	public long pingLastUpdate = System.currentTimeMillis();
	
	public Session(SBENServer server, InetAddress address, int port){
		this(server, address, port, null, new Object[0]);
	}
	
	public Session(final SBENServer server, InetAddress address, int port, Thread thread, Object... obj){
		this.server = server;
		this.address = address;
		this.port = port;
		this.thread = thread;
		this.obj = obj;
		
		this.transmitter = new PacketTransmitter(server.getPacketsData()){
			protected void send(byte[] data){
				server.getSocketHandler().getProtocol().sendPacket(Session.this, data);
			}

			public void receive(Packet packet){
				Session.this.handlePacket(packet);
			}
		};
	}
	
	public boolean hasThread(){
		return this.thread != null;
	}
	
	public String getIdentifier(){
		return address.getHostAddress() + ":" + port;
	}
	
	public void sendPacket(DataPacket packet){
		sendPacket(packet, true);
	}
	
	public void sendPacket(DataPacket packet, boolean needACK){
		if(needACK && server.getSocketHandler().getProtocol().getType() == ProtocolType.TCP)
			needACK = true;
		
		if(packet instanceof SecuredPacket)
			((SecuredPacket) packet).set_key(this.key);
		
		final PacketData packet_data = new PacketData();
		packet_data.data = packet;
		packet_data.packetsData = getServer().getPacketsData();
		
		this.transmitter.sendPacket(packet_data, needACK);
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
	
	private void handlePacket(Packet packet){
		switch(packet.getType()){
		case Packet.TYPE_LOGIN:
			handle((PacketLogin) packet);
			break;
			
		case Packet.TYPE_DATA:
			handle((PacketData) packet);
			break;
			
		case Packet.TYPE_PING:
			handle((PacketPing) packet);
			break;
			
		case Packet.TYPE_ACK:
			handle((PacketAck) packet);
			break;
			
		case Packet.TYPE_NACK:
			handle((PacketNack) packet);
			break;
		}
	}
	
	private void handle(PacketLogin packet){
		if(this.connectionState != ConnectionState.Connecting) return;
		
		final PacketLoginReply packet_reply = new PacketLoginReply();
		
		if(packet.version_protocol == Network.PROTOCOL_VERSION){
			packet_reply.reply = PacketLoginReply.REPLY_SUCCESS;
			
			server.onSessionRequest(this);
			this.setKey(new SecretKeySpec(packet.security_id, "AES"));
			this.transmitter.setKey(this.key);
			this.setConnectionState(ConnectionState.Connected);
			
		}else if(packet.version_protocol < Network.PROTOCOL_VERSION)
			packet_reply.reply = PacketLoginReply.REPLY_FAILED_PROTOCOL_OUTDATED_CLIENT;
		else
			packet_reply.reply = PacketLoginReply.REPLY_FAILED_PROTOCOL_OUTDATED_SERVER;
		
		packet_reply.encode();
		transmitter.sendPacket(packet_reply, true);
	}
	
	private void handle(PacketPing packet){
		if(this.connectionState != ConnectionState.Connected) return;
		
		if(packet.time == 0) return;
		
		final PacketPong packet_pong = new PacketPong();
		
		packet_pong.time = packet.time;
		
		packet_pong.encode();
		transmitter.sendPacket(packet_pong, false);
		
		// get ping
		setPing((packet.time - pingLastUpdate)-Network.PING_UPDATE);
		pingLastUpdate = packet.time;
	}
	
	private void handle(PacketData packet){
		if(this.connectionState != ConnectionState.Connected) return;
		
		final DataPacket dataPacket = packet.data;
		
		for(SessionEventListener listener:getListeners())
			listener.onPacketReceive(dataPacket);
	}
	
	private void handle(PacketAck packet){
		for(byte window:packet.windows)
			this.transmitter.receiveNack(window);
	}
	
	private void handle(PacketNack packet){
		for(byte window:packet.windows)
			this.transmitter.receiveNack(window);
	}
}
