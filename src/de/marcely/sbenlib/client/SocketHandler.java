package de.marcely.sbenlib.client;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nullable;
import javax.crypto.spec.SecretKeySpec;

import de.marcely.sbenlib.client.protocol.Protocol;
import de.marcely.sbenlib.compression.Base64;
import de.marcely.sbenlib.network.ByteArraysCombiner;
import de.marcely.sbenlib.network.ConnectionState;
import de.marcely.sbenlib.network.Network;
import de.marcely.sbenlib.network.packets.Packet;
import de.marcely.sbenlib.network.packets.PacketClose;
import de.marcely.sbenlib.network.packets.PacketData;
import de.marcely.sbenlib.network.packets.PacketLogin;
import de.marcely.sbenlib.network.packets.PacketLoginReply;
import de.marcely.sbenlib.network.packets.PacketPing;
import de.marcely.sbenlib.network.packets.PacketPong;
import de.marcely.sbenlib.network.packets.data.DataPacket;
import de.marcely.sbenlib.util.Util;
import lombok.Getter;

public class SocketHandler {
	
	@Getter private final SBENServerConnection server;
	@Getter private final Protocol protocol;
	
	private ByteArraysCombiner combiner;
	
	public SocketHandler(SBENServerConnection server){
		this.server = server;
		combiner = new ByteArraysCombiner(Packet.SEPERATOR[0]);
		
		this.protocol = server.getConnectionInfo().PROTOCOL.getClientInstance(server.getConnectionInfo(), this, new ServerEventListener(){
			public void onPacketReceive(byte[] bytes){
				final List<byte[]> packets = combiner.addBytes(bytes);
				
				for(byte[] rawPacket:packets){
					// decode base64
					rawPacket = Base64.decode(rawPacket);
					
					// work with packet
					final byte id = rawPacket[0];
					
					switch(id){
					case Packet.TYPE_LOGIN_REPLY:
						
						final PacketLoginReply packet_reply = new PacketLoginReply();
						packet_reply.decode(rawPacket);
						
						workWithPacket(packet_reply);
						
						break;
					
					case Packet.TYPE_PONG:
						
						final PacketPong packet_pong = new PacketPong();
						packet_pong.decode(rawPacket);
						
						workWithPacket(packet_pong);
						
						break;
						
					case Packet.TYPE_CLOSE:
						
						final PacketClose packet_close = new PacketClose();
						packet_close.decode(rawPacket);
						
						workWithPacket(packet_close);
						
						break;
						
					case Packet.TYPE_DATA:
						
						final PacketData packet_data = new PacketData();
						packet_data.packetsData = getServer().getPacketsData();
						packet_data.decode(rawPacket);
						
						workWithPacket(packet_data);
						
						break;
					}
				}
			}

			@Override
			public void onDisconnect(){
				close();
			}
		});
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
				
				// send login packet
				final PacketLogin packet = new PacketLogin();
				packet.security_id = server.key.getEncoded();
				packet.version_protocol = Network.PROTOCOL_VERSION;
				packet.encode();
				
				sendPacket(packet);
				
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
		
		if(isRunning()){
			this.getServer().setConnectionState(ConnectionState.Disconnected);
			this.getServer().onDisconnect(reason);
			
			return protocol.close();
		}else
			return false;
	}
	
	public boolean sendPacket(DataPacket packet){
		final PacketData packet_data = new PacketData();
		packet_data.data = packet;
		packet_data.packetsData = getServer().getPacketsData();
		packet_data.encode();
		
		return sendPacket(packet_data);
	}
	
	public boolean sendPacket(Packet packet){
		return protocol.sendPacket(packet);
	}
	
	
	
	
	
	private void workWithPacket(PacketLoginReply packet){
		switch(packet.reply){
		case PacketLoginReply.REPLY_SUCCESS:
			getServer().setConnectionState(ConnectionState.Connected);
			
			// register ping timer
			final Timer timer_ping = new Timer();
			timer_ping.schedule(new TimerTask(){
				public void run(){
					final PacketPing packet = new PacketPing();
					
					packet.time = System.currentTimeMillis();
					
					packet.encode();
					sendPacket(packet);
				}
			}, Network.PING_UPDATE, Network.PING_UPDATE);
			
			getServer().registerTimer(timer_ping);
			
			break;
		case PacketLoginReply.REPLY_FAILED_PROTOCOL_OUTDATED_CLIENT:
			getServer().setConnectionState(ConnectionState.Disconnected);
			getServer().onDisconnect("LOGIN_PROTOCOL_OUTDATED_CLIENT");
			break;
		case PacketLoginReply.REPLY_FAILED_PROTOCOL_OUTDATED_SERVER:
			getServer().setConnectionState(ConnectionState.Disconnected);
			getServer().onDisconnect("LOGIN_PROTOCOL_OUTDATED_SERVER");
			break;
		case PacketLoginReply.REPLY_FAILED_UNKOWN:
			getServer().setConnectionState(ConnectionState.Disconnected);
			getServer().onDisconnect("LOGIN_UNKOWN");
			break;
		}
	}
	
	private void workWithPacket(PacketPong packet){
		final long delay = System.currentTimeMillis() - packet.time;
		
		getServer().setPing(delay);
	}
	
	private void workWithPacket(PacketClose packet){
		close(packet.reason);
	}
	
	private void workWithPacket(PacketData packet){
		final DataPacket dataPacket = packet.data;
		
		getServer().onPacketReceive(dataPacket);
	}
}