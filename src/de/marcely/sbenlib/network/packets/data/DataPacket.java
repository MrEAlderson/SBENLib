package de.marcely.sbenlib.network.packets.data;

import de.marcely.sbenlib.util.BufferedReadStream;
import de.marcely.sbenlib.util.BufferedWriteStream;
import lombok.Getter;
import lombok.Setter;

public abstract class DataPacket {
	
	@Getter @Setter protected BufferedWriteStream writeStream;
	@Getter @Setter protected BufferedReadStream readStream;
	
	public static final byte TYPE_NORMAL = 0x0;
	public static final byte TYPE_SECURED = 0x1;
	
	public abstract byte getTypeID();
	
	public abstract byte getPacketID();
	
	public abstract void encode();
	
	public abstract void decode();
	
	public abstract byte[] getWriteBytes();
	
	public abstract byte[] getReadBytes(byte[] bytes);
}
