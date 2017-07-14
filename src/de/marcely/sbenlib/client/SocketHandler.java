package de.marcely.sbenlib.client;

import java.util.List;

import de.marcely.sbenlib.client.protocol.Protocol;
import de.marcely.sbenlib.network.ByteArraysCombiner;
import de.marcely.sbenlib.network.packets.Packet;
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
				
				for(byte[] packet:packets){
					
				}
			}
		});
	}
	
	public boolean isRunning(){
		return this.protocol.isRunning();
	}
	
	public boolean run(){
		if(!isRunning()){
			return protocol.run();
		}else
			return false;
	}
}
