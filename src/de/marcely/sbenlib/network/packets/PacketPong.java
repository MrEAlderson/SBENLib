package de.marcely.sbenlib.network.packets;

public class PacketPong extends Packet {
	
	public long time;
	
	@Override
	public byte getType(){
		return TYPE_PONG;
	}

	@Override
	protected byte[] _encode(){
		this.writeStream.writeSignedLong(time);
		
		return this.writeStream.toByteArray();
	}

	@Override
	protected void _decode(byte[] data){
		time = this.readStream.readSignedLong();
	}
}
