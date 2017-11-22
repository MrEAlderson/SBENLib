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
	public static final byte TYPE_NAK = (byte) 0x6;
	public static final byte TYPE_CLOSE = (byte) 0x7; // max is 0x7
	public static final byte[] SEPERATOR;
	
	@Getter protected BufferedWriteStream writeStream;
	@Getter protected BufferedReadStream readStream;
	
	public byte _identifier; // 4 bits (0-15)
	public boolean _sendAck;
	
	static {
		SEPERATOR = new byte[1];
		SEPERATOR[0] = (byte) 0x0;
	}
	
	public byte[] encode(){
		this.writeStream = new BufferedWriteStream();
		
		// do some magic and create a header within one byte
		byte header = 0x0;
		
		//ack
		if(_sendAck)
			header = (byte) (header | (1 << 7));
		
		header = (byte) (header | (_identifier << 3)); //identifier
		header = (byte) (header | getType()); //type
		
		this.writeStream.writeByte(header);
		
		return _encode();
	}
	
	public void decode(byte[] data){
		this.readStream = new BufferedReadStream(data);
		
		final byte header = this.readStream.readByte();
		
		// read header
		this._identifier = (byte) (((header << 1) & 0xFF) >> 4);
		this._sendAck = (header & 0xFF) >> 7 == 0x1;
		
		_decode(data);
	}
	
	public static byte getTypeOfHeader(byte header){
		return (byte) (((header << 5) & 0xFF) >> 5);
	}
	
	
	public abstract byte getType();
	
	protected abstract byte[] _encode();
	
	protected abstract void _decode(byte[] data);
}