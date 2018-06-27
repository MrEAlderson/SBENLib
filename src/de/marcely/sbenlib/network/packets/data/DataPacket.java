package de.marcely.sbenlib.network.packets.data;

import javax.crypto.spec.SecretKeySpec;

import de.marcely.sbenlib.util.BufferedReadStream;
import de.marcely.sbenlib.util.BufferedWriteStream;
import lombok.Setter;

public abstract class DataPacket {
	
	@Setter protected SecretKeySpec _key;
	
	public static final byte TYPE_NORMAL = 0x0;
	public static final byte TYPE_SECURED = 0x1;
	
	public abstract byte getTypeID();
	
	public abstract byte getPacketID();
	
	public abstract void encode(BufferedWriteStream stream);
	
	public abstract void decode(BufferedReadStream stream);
}
