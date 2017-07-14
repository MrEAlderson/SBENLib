package de.marcely.sbenlib.network.packets;

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
	protected byte[] _encode(){
		this.writeStream.writeByte(reply);
		
		return this.writeStream.toByteArray();
	}

	@Override
	protected void _decode(byte[] data){
		
	}
}