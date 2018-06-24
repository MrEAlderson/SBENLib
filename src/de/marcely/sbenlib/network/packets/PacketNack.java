package de.marcely.sbenlib.network.packets;

public class PacketNack extends Packet {
	
	public byte window;
	
	@Override
	public byte getType(){
		return Packet.TYPE_ACK;
	}

	@Override
	protected byte[] _encode(){
		this.writeStream.write(window);
		
		return this.writeStream.toByteArray();
	}

	@Override
	protected void _decode(byte[] data){
		this.window = this.readStream.readByte();
	}
}
