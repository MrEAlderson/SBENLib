package de.marcely.sbenlib.network;

import java.util.HashMap;

import de.marcely.sbenlib.network.packets.data.DataPacket;
import lombok.Getter;
import lombok.Setter;

public class PacketsData {
	
	@Getter @Setter private HashMap<Byte, DataPacket> packets = new HashMap<Byte,  DataPacket>();
	
	public PacketsData(){ }
	
	public void addPacket(DataPacket packet){
		this.packets.put(packet.getPacketID(), packet);
	}
	
	public void removePacket(byte id){
		this.packets.remove(id);
	}
	
	public DataPacket getPacket(byte id){
		return this.packets.get(id);
	}
}
