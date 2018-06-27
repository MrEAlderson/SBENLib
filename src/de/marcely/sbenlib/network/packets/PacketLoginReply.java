package de.marcely.sbenlib.network.packets;

import de.marcely.sbenlib.util.BufferedReadStream;
import de.marcely.sbenlib.util.BufferedWriteStream;

public class PacketLoginReply extends Packet {
		
	public static final byte REPLY_SUCCESS = (byte) 0x0;
	public static final byte REPLY_FAILED_PROTOCOL_OUTDATED_CLIENT = (byte) 0x1;
	public static final byte REPLY_FAILED_PROTOCOL_OUTDATED_SERVER = (byte) 0x2;
	public static final byte REPLY_FAILED_UNKOWN = (byte) 0x3;
	
	public byte reply;

	@Override
	public byte getType(){
		return TYPE_LOGIN_REPLY;
	}

	@Override
	protected void _encode(BufferedWriteStream stream){
		stream.writeByte(reply);
	}

	@Override
	protected void _decode(BufferedReadStream stream){
		reply = stream.readByte();
	}
}