package de.marcely.sbenlib.server;

import de.marcely.sbenlib.network.ConnectionState;
import de.marcely.sbenlib.network.packets.data.DataPacket;

public interface SessionEventListener {
	
	public void onStateChange(ConnectionState state);
	
	public void onPacketReceive(DataPacket dataPacket);
	
	public void onDisconnect(String reason);
}