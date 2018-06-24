package de.marcely.sbenlib.network.packets;

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
	public static final byte TYPE_NACK = (byte) 0x6;
	public static final byte TYPE_CLOSE = (byte) 0x7; // max is 0x7
	
	@Getter protected BufferedWriteStream writeStream;
	@Getter protected BufferedReadStream readStream;
	
	public boolean _needAck = false;
	
	public byte[] encode(){
		this.writeStream = new BufferedWriteStream();
		
		// do some magic and create a header
		byte header = 0x0;
		
		header = (byte) (header | (getType() << 1)); //type
		header = (byte) (header | (_needAck  ? 1 : 0)); //need ack
		
		this.writeStream.writeByte(header);
		
		return _encode();
	}
	
	public void decode(byte[] data){
		this.readStream = new BufferedReadStream(data);
		
		final byte header = this.readStream.readByte();
		
		this._needAck = (byte) (header << 7) == -128;
		
		_decode(data);
	}
	
	public static byte getTypeOfHeader(byte header){
		return (byte) ((header >> 1) & 0x07);
	}
	
	
	public abstract byte getType();
	
	protected abstract byte[] _encode();
	
	protected abstract void _decode(byte[] data);
}