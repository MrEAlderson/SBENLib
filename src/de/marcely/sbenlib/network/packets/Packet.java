package de.marcely.sbenlib.network.packets;

import java.io.IOException;

import de.marcely.sbenlib.util.BufferedReadStream;
import de.marcely.sbenlib.util.BufferedWriteStream;
import lombok.Getter;

public abstract class Packet {
	
	public static final byte TYPE_LOGIN = (byte) 0x0;
	public static final byte TYPE_LOGIN_REPLY = (byte) 0x1;
	public static final byte TYPE_DATA = (byte) 0x2;
	public static final byte TYPE_PING = (byte) 0x3;
	public static final byte TYPE_PONG = (byte) 0x4;
	public static final byte TYPE_ACK = (byte) 0x5;
	public static final byte TYPE_NAK = (byte) 0x6;
	public static final byte TYPE_CLOSE = (byte) 0x7;
	public static final byte[] SEPERATOR;
	
	@Getter protected BufferedWriteStream writeStream;
	@Getter protected BufferedReadStream readStream;
	
	static {
		SEPERATOR = new byte[1];
		SEPERATOR[0] = (byte) 0x0;
	}
	
	public byte[] encode(){
		this.writeStream = new BufferedWriteStream();
		
		this.writeStream.writeByte(getType());
		
		return _encode();
	}
	
	public void decode(byte[] data){
		this.readStream = new BufferedReadStream(data);
		
		if(this.readStream.readByte() != getType())
			new IOException("Packet types mismatch").printStackTrace();
		
		_decode(data);
	}
	
	
	public abstract byte getType();
	
	protected abstract byte[] _encode();
	
	protected abstract void _decode(byte[] data);
}