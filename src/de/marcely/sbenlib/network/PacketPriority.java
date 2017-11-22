package de.marcely.sbenlib.network;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import lombok.Getter;

public enum PacketPriority {
	
	NORMAL((byte) 0x0, false),
	DONT_LOSE((byte) 0x1, true),
	DONE_LOSE_AND_SORTED((byte) 0x2, true);
	
	private static Map<Byte, PacketPriority> VALUES = new HashMap<>();
	
	static {
		for(PacketPriority priority:values())
			VALUES.put(priority.id, priority);
	}
	
	@Getter private final byte id;
	@Getter private final boolean sendAck;
	
	private PacketPriority(byte id, boolean sendAck){
		this.id = id;
		this.sendAck = sendAck;
	}
	
	public static @Nullable PacketPriority fromId(byte id){
		return VALUES.get(id);
	}
}