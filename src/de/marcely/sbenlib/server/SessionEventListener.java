package de.marcely.sbenlib.server;

import de.marcely.sbenlib.network.ConnectionState;
import de.marcely.sbenlib.network.packets.PacketData;

public interface SessionEventListener {
	
	public void onStateChange(ConnectionState state);
	
	public void onPacketReceive(PacketData packet);
	
	public void onDisconnect(String reason);
}