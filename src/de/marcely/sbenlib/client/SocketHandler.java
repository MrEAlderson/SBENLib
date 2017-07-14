package de.marcely.sbenlib.client;

import java.util.List;

import de.marcely.sbenlib.client.protocol.Protocol;
import de.marcely.sbenlib.compression.Base64;
import de.marcely.sbenlib.network.ByteArraysCombiner;
import de.marcely.sbenlib.network.ConnectionState;
import de.marcely.sbenlib.network.Network;
import de.marcely.sbenlib.network.packets.Packet;
import de.marcely.sbenlib.network.packets.PacketLogin;
import de.marcely.sbenlib.util.Util;
import lombok.Getter;

public class SocketHandler {
	
	@Getter private final SBENServerConnection server;
	@Getter private final Protocol protocol;
	
	private ByteArraysCombiner combiner;
	
	public SocketHandler(SBENServerConnection server){
		this.server = server;
		combiner = new ByteArraysCombiner(Packet.SEPERATOR[0]);
		
		this.protocol = server.getConnectionInfo().PROTOCOL.getClientInstance(server.getConnectionInfo(), new ServerEventListener(){
			public void onPacketReceive(byte[] bytes){
				final List<byte[]> packets = combiner.addBytes(bytes);
				
				for(byte[] rawPacket:packets){
					// decode base64
					rawPacket = Base64.decode(rawPacket);
					
					// work with packet
					final byte id = rawPacket[0];
					
					switch(id){
					case Packet.TYPE_LOGIN_REPLY:
						
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
				
				// send login packet
				final PacketLogin packet = new PacketLogin();
				packet.security_id = Util.generateRandomSecurityID();
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
		if(isRunning()){
			this.getServer().setConnectionState(ConnectionState.Disconnected);
			
			return protocol.close();
		}else
			return false;
	}
	
	public boolean sendPacket(Packet packet){
		return protocol.sendPacket(packet);
	}
}