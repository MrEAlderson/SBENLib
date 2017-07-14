package de.marcely.sbenlib.network.packets;

import de.marcely.sbenlib.util.BufferedReadStream;
import de.marcely.sbenlib.util.BufferedWriteStream;

public abstract class Packet {
	
	public static final byte TYPE_LOGIN = (byte) 0x0;
	public static final byte TYPE_LOGIN_REPLY = (byte) 0x1;
	public static final byte TYPE_DATA = (byte) 0x2;
	public static final byte TYPE_PING = (byte) 0x3;
	public static final byte TYPE_ACK = (byte) 0x4;
	public static final byte TYPE_NAK = (byte) 0x5;
	public static final byte[] SEPERATOR;
	
	protected BufferedWriteStream writeStream;
	protected BufferedReadStream readStream;
	
	static {
		SEPERATOR = new byte[1];
		SEPERATOR[0] = (byte) 0x0;
	}
	
	public byte[] encode(){
		this.writeStream = new BufferedWriteStream();
		return _encode();
	}
	
	public void decode(byte[] data){
		this.readStream = new BufferedReadStream(data);
		_decode(data);
	}
	
	
	public abstract byte getType();
	
	protected abstract byte[] _encode();
	
	protected abstract void _decode(byte[] data);
}