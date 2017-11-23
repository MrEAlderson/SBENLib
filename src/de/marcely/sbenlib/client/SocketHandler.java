package de.marcely.sbenlib.client;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.crypto.spec.SecretKeySpec;

import de.marcely.sbenlib.client.protocol.Protocol;
import de.marcely.sbenlib.compression.Base64;
import de.marcely.sbenlib.network.ByteArraysCombiner;
import de.marcely.sbenlib.network.ConnectionState;
import de.marcely.sbenlib.network.Network;
import de.marcely.sbenlib.network.PacketDecoder;
import de.marcely.sbenlib.network.PacketPriority;
import de.marcely.sbenlib.network.PacketTransmitter;
import de.marcely.sbenlib.network.packets.Packet;
import de.marcely.sbenlib.network.packets.PacketAck;
import de.marcely.sbenlib.network.packets.PacketClose;
import de.marcely.sbenlib.network.packets.PacketData;
import de.marcely.sbenlib.network.packets.PacketLogin;
import de.marcely.sbenlib.network.packets.PacketLoginReply;
import de.marcely.sbenlib.network.packets.PacketPing;
import de.marcely.sbenlib.network.packets.PacketPong;
import de.marcely.sbenlib.network.packets.data.DataPacket;
import de.marcely.sbenlib.util.TickTimer;
import de.marcely.sbenlib.util.Util;
import lombok.Getter;

public class SocketHandler {
	
	@Getter private final SBENServerConnection server;
	@Getter private final Protocol protocol;
	
	private long lastReceivedPacket = System.currentTimeMillis();
	
	// packet handler
	private ByteArraysCombiner combiner;
	private PacketTransmitter packetTransmitter;
	private List<byte[]> packetsQuery = new ArrayList<byte[]>();
	private final TickTimer packetHandlerTimer;
	
	public SocketHandler(final SBENServerConnection server){
		this.server = server;
		combiner = new ByteArraysCombiner(Packet.SEPERATOR[0]);
		
		this.protocol = server.getConnectionInfo().PROTOCOL.getClientInstance(server.getConnectionInfo(), this, new ServerEventListener(){
			public void onPacketReceive(byte[] bytes){
				packetsQuery.add(bytes);
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
				
				if(packetsQuery.size() == 0)
					return;
				
				lastReceivedPacket = System.currentTimeMillis();
				
				final List<byte[]> bytes = new ArrayList<byte[]>(packetsQuery);
				
				for(byte[] rp:bytes){
					
					for(byte[] rawPacket:combiner.addBytes(rp)){
						// decode base64
						rawPacket = Base64.decode(rawPacket);
						
						try{
							packetTransmitter.onReceivePacket(PacketDecoder.decode(server.getPacketsData(), rawPacket));
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				}
				
				packetsQuery.removeAll(bytes);
			}
		};
		this.packetHandlerTimer.start();
		
		// create transmitter to add priority for packets
		this.packetTransmitter = new PacketTransmitter(){
			protected void send(Packet packet, Object[] data){
				protocol.sendPacket(packet);
			}
			
			public void receive(Packet packet, Object[] data){
				switch(packet.getType()){
				case Packet.TYPE_LOGIN_REPLY:
					workWithPacket((PacketLoginReply) packet);
					break;
					
				case Packet.TYPE_PONG:
					workWithPacket((PacketPong) packet);
					break;
					
				case Packet.TYPE_CLOSE:
					workWithPacket((PacketClose) packet);
					break;
					
				case Packet.TYPE_DATA:
					workWithPacket((PacketData) packet);
					break;
					
				case Packet.TYPE_ACK:
					onReceiveAck((PacketAck) packet, false);
					break;
				}
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
				
				packetHandlerTimer.start();
				
				// send login packet
				final PacketLogin packet = new PacketLogin();
				packet.security_id = server.key.getEncoded();
				packet.version_protocol = Network.PROTOCOL_VERSION;
				packet.encode();
				
				protocol.sendPacket(packet);
				
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
			this.getServer().setConnectionState(ConnectionState.Disconnected);
			this.getServer().onDisconnect(reason);
			
			return protocol.close();
		}else
			return false;
	}
	
	public boolean sendPacket(DataPacket packet, PacketPriority priority){
		final PacketData packet_data = new PacketData();
		packet_data.data = packet;
		packet_data.packetsData = getServer().getPacketsData();
		packet_data.encode();
		
		return packetTransmitter.addToSendQueue(packet_data, priority);
	}
	
	
	
	
	
	private void workWithPacket(PacketLoginReply packet){
		switch(packet.reply){
		case PacketLoginReply.REPLY_SUCCESS:
			getServer().setConnectionState(ConnectionState.Connected);
			
			// register ping timer
			final TickTimer timer_ping = new TickTimer(true, Network.PING_UPDATE){
				public void onRun(){
					final PacketPing packet = new PacketPing();
					
					packet.time = System.currentTimeMillis();
					
					packet.encode();
					protocol.sendPacket(packet);
				}
			};
			timer_ping.start();
			
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