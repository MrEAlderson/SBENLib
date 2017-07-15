package de.marcely.sbenlib.network.packets;

public class PacketClose extends Packet {
	
	public String reason = "UNKOWN";
	
	@Override
	public byte getType(){
		return TYPE_CLOSE;
	}

	@Override
	protected byte[] _encode(){
		this.writeStream.writeString(reason);
		
		return this.writeStream.toByteArray();
	}

	@Override
	protected void _decode(byte[] data){
		this.reason = this.readStream.readString();
	}
}
