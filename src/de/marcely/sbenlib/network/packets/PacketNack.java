package de.marcely.sbenlib.network.packets;

public class PacketNack extends Packet {
	
	public Byte[] windows;
	
	@Override
	public byte getType(){
		return Packet.TYPE_ACK;
	}

	@Override
	protected byte[] _encode(){
		for(byte window:windows)
			this.writeStream.write(window);
		
		return this.writeStream.toByteArray();
	}

	@Override
	protected void _decode(byte[] data){
		this.windows = new Byte[this.readStream.available()];
		
		for(int i=0; i<this.windows.length; i++)
			this.windows[i] = this.readStream.readByte();
	}
}
