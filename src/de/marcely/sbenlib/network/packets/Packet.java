package de.marcely.sbenlib.network.packets;

import java.io.IOException;

import de.marcely.sbenlib.util.BufferedReadStream;
import de.marcely.sbenlib.util.BufferedWriteStream;

public abstract class Packet {
	
	public static final byte TYPE_LOGIN = (byte) 0x0;
	public static final byte TYPE_LOGIN_REPLY = (byte) 0x1;
	public static final byte TYPE_DATA = (byte) 0x2;
	public static final byte TYPE_PING = (byte) 0x3;
	public static final byte TYPE_PONG = (byte) 0x4;
	public static final byte TYPE_ACK = (byte) 0x5;
	public static final byte TYPE_NACK = (byte) 0x6;
	public static final byte TYPE_CLOSE = (byte) 0x7; // max is 0x7
	
	public byte[] encode(){
		final BufferedWriteStream stream = new BufferedWriteStream();
		
		// do some magic and create a header
		byte header = 0x0;
		
		header = (byte) (header | (getType() << 1)); //type
		// header = (byte) (header | (_needAck  ? 1 : 0)); //need ack
		
		stream.writeByte(header);
		
		// final
		 _encode(stream);
		 
		 try{
			stream.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		 
		 return stream.toByteArray();
	}
	
	public void decode(byte[] data){
		final BufferedReadStream stream = new BufferedReadStream(data);
		/*final byte header = */stream.readByte();
		
		// this._needAck = (byte) (header << 7) == -128;
		
		_decode(stream);
		
		try{
			stream.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public static byte getTypeOfHeader(byte header){
		return (byte) ((header >> 1) & 0x07);
	}
	
	
	public abstract byte getType();
	
	protected abstract void _encode(BufferedWriteStream stream);
	
	protected abstract void _decode(BufferedReadStream stream);
}