package de.marcely.sbenlib.network;

import java.util.HashMap;
import java.util.Map;

import de.marcely.sbenlib.network.packets.data.DataPacket;
import lombok.Getter;
import lombok.Setter;

public class PacketsData {
	
	@Getter @Setter private Map<Byte, DataPacket> packets = new HashMap<>();
	
	public PacketsData(){ }
	
	public void addPacket(DataPacket packet){
		this.packets.put(packet.getPacketID(), packet);
	}
	
	public void addPackets(DataPacket... packets){
		for(DataPacket packet:packets)
			addPacket(packet);
	}
	
	public void removePacket(byte id){
		this.packets.remove(id);
	}
	
	public DataPacket getPacket(byte id){
		return this.packets.get(id);
	}
}
