package de.marcely.sbenlib.network.packets;

import de.marcely.sbenlib.util.BufferedReadStream;
import de.marcely.sbenlib.util.BufferedWriteStream;

public class PacketNack extends Packet {
	
	public Byte[] windows;
	
	@Override
	public byte getType(){
		return Packet.TYPE_ACK;
	}

	@Override
	protected void _encode(BufferedWriteStream stream){
		for(byte window:windows)
			stream.write(window);
	}

	@Override
	protected void _decode(BufferedReadStream stream){
		this.windows = new Byte[stream.available()];
		
		for(int i=0; i<this.windows.length; i++)
			this.windows[i] = stream.readByte();
	}
}
