package de.marcely.sbenlib.client;

import java.util.ArrayDeque;
import java.util.Queue;

import javax.annotation.Nullable;
import javax.crypto.spec.SecretKeySpec;

import de.marcely.sbenlib.client.protocol.Protocol;
import de.marcely.sbenlib.network.ConnectionState;
import de.marcely.sbenlib.network.Network;
import de.marcely.sbenlib.network.PacketTransmitter;
import de.marcely.sbenlib.network.ProtocolType;
import de.marcely.sbenlib.network.packets.Packet;
import de.marcely.sbenlib.network.packets.PacketAck;
import de.marcely.sbenlib.network.packets.PacketClose;
import de.marcely.sbenlib.network.packets.PacketData;
import de.marcely.sbenlib.network.packets.PacketLogin;
import de.marcely.sbenlib.network.packets.PacketLoginReply;
import de.marcely.sbenlib.network.packets.PacketNack;
import de.marcely.sbenlib.network.packets.PacketPing;
import de.marcely.sbenlib.network.packets.PacketPong;
import de.marcely.sbenlib.network.packets.data.DataPacket;
import de.marcely.sbenlib.network.packets.data.SecuredPacket;
import de.marcely.sbenlib.util.TickTimer;
import de.marcely.sbenlib.util.Util;
import lombok.Getter;

public class SocketHandler {
	
	@Getter private final SBENServerConnection server;
	@Getter private final Protocol protocol;
	
	private long lastReceivedPacket = System.currentTimeMillis();
	
	// packet handler
	private PacketTransmitter packetTransmitter;
	private Queue<byte[]> packetsQueue = new ArrayDeque<byte[]>();
	private final TickTimer packetHandlerTimer;
	
	public SocketHandler(final SBENServerConnection server){
		this.server = server;
		
		this.protocol = server.getConnectionInfo().PROTOCOL.getClientInstance(server.getConnectionInfo(), this, new ServerEventListener(){
			public void onPacketReceive(byte[] data){
				packetsQueue.add(data);
			}

			@Override
			public void onDisconnect(){
				close();
			}
		});
		
		// raw packet byte data to actual packet instance
		this.packetHandlerTimer = new TickTimer(true, 100){
			public void onRun(){
				// timeout
				if(System.currentTimeMillis() - 1000*6 > lastReceivedPacket)
					close("SERVER_TIMEOUT");
				
				if(packetsQueue.size() == 0)
					return;
				
				lastReceivedPacket = System.currentTimeMillis();
				
				byte[] data = null;
				
				while((data = packetsQueue.poll()) != null){
					try{
						packetTransmitter.handlePacket(data);
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				
				packetTransmitter.sendNacks();
			}
		};
		this.packetHandlerTimer.start();
		
		// create transmitter to add priority for packets
		this.packetTransmitter = new PacketTransmitter(server.getPacketsData()){
			protected void send(byte[] data){
				protocol.sendPacket(data);
			}
			
			public void receive(Packet packet){
				SocketHandler.this.handlePacket(packet);
			}
		};
	}
	
	public boolean isRunning(){
		return this.protocol.isRunning();
	}
	
	public boolean run(){
		if(!isRunning()){
			final boolean success = protocol.run();
			
			if(success){
				this.getServer().setConnectionState(ConnectionState.Connecting);
				
				server.key = new SecretKeySpec(Util.generateRandomSecurityID(), "AES");
				packetTransmitter.setKey(server.key);
				packetHandlerTimer.start();
				
				// send login packet
				final PacketLogin packet = new PacketLogin();
				packet.security_id = server.key.getEncoded();
				packet.version_protocol = Network.PROTOCOL_VERSION;
				packet.encode();
				
				packetTransmitter.sendPacket(packet, false);
				
				return true;
			}else{
				this.getServer().setConnectionState(ConnectionState.Disconnected);
				
				return false;
			}
		}else
			return false;
	}
	
	public boolean close(){
		return close(null);
	}
	
	public boolean close(@Nullable String reason){
		if(reason == null)
			reason = "SOCKET_CLIENT_CLOSE";
		
		packetHandlerTimer.stop();
		
		if(isRunning()){
			// send packet
			final PacketClose packet = new PacketClose();
			
			packet.reason = reason;
			
			packetTransmitter.sendPacket(packet, false);
			
			// do final stuff
			this.getServer().setConnectionState(ConnectionState.Disconnected);
			this.getServer().onDisconnect(reason);
			
			return protocol.close();
		}else
			return false;
	}
	
	public boolean sendPacket(DataPacket packet, boolean needACK){
		if(needACK && getProtocol().getType() == ProtocolType.TCP)
			needACK = false;
		
		if(packet instanceof SecuredPacket)
			((SecuredPacket) packet).set_key(this.server.key);
		
		final PacketData packet_data = new PacketData();
		packet_data.data = packet;
		packet_data.packetsData = getServer().getPacketsData();
		
		return packetTransmitter.sendPacket(packet_data, needACK);
	}
	
	
	
	private void handlePacket(Packet packet){
		switch(packet.getType()){
		case Packet.TYPE_LOGIN_REPLY:
			handle((PacketLoginReply) packet);
			break;
			
		case Packet.TYPE_PONG:
			handle((PacketPong) packet);
			break;
			
		case Packet.TYPE_CLOSE:
			handle((PacketClose) packet);
			break;
			
		case Packet.TYPE_DATA:
			handle((PacketData) packet);
			break;
			
		case Packet.TYPE_ACK:
			handle((PacketAck) packet);
			break;
			
		case Packet.TYPE_NACK:
			handle((PacketNack) packet);
			break;
		}
	}
	
	private void handle(PacketLoginReply packet){
		if(server.getConnectionState() != ConnectionState.Connecting) return;
		
		switch(packet.reply){
		case PacketLoginReply.REPLY_SUCCESS:
			getServer().setConnectionState(ConnectionState.Connected);
			
			// register ping timer
			final TickTimer timer_ping = new TickTimer(true, Network.PING_UPDATE){
				public void onRun(){
					final PacketPing packet = new PacketPing();
					
					packet.time = System.currentTimeMillis();
					
					packet.encode();
					packetTransmitter.sendPacket(packet, false);
				}
			};
			timer_ping.start();
			
			getServer().registerTimer(timer_ping);
			
			break;
		case PacketLoginReply.REPLY_FAILED_PROTOCOL_OUTDATED_CLIENT:
			getServer().setConnectionState(ConnectionState.Disconnected);
			close("LOGIN_PROTOCOL_OUTDATED_CLIENT");
			break;
		case PacketLoginReply.REPLY_FAILED_PROTOCOL_OUTDATED_SERVER:
			getServer().setConnectionState(ConnectionState.Disconnected);
			close("LOGIN_PROTOCOL_OUTDATED_SERVER");
			break;
		case PacketLoginReply.REPLY_FAILED_UNKOWN:
			getServer().setConnectionState(ConnectionState.Disconnected);
			close("LOGIN_UNKOWN");
			break;
		}
	}
	
	private void handle(PacketPong packet){
		final long delay = System.currentTimeMillis() - packet.time;
		
		getServer().setPing(delay);
	}
	
	private void handle(PacketClose packet){
		if(server.getConnectionState() != ConnectionState.Connected) return;
		
		close(packet.reason);
	}
	
	private void handle(PacketData packet){
		if(server.getConnectionState() != ConnectionState.Connected) return;
		
		final DataPacket dataPacket = packet.data;
		
		getServer().onPacketReceive(dataPacket);
	}
	
	private void handle(PacketAck packet){
		for(byte window:packet.windows)
			this.packetTransmitter.receiveNack(window);
	}
	
	private void handle(PacketNack packet){
		for(byte window:packet.windows)
			this.packetTransmitter.receiveNack(window);
	}
}