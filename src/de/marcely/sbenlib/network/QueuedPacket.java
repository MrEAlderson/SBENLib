package de.marcely.sbenlib.network;

import de.marcely.sbenlib.network.packets.Packet;

public class QueuedPacket {
	
	public final Packet packet;
	public final PacketPriority priority;
	
	public long lastTimeSend = 0;
	
	public QueuedPacket(Packet packet, PacketPriority priority){
		this.packet = packet;
		this.priority = priority;
	}
}