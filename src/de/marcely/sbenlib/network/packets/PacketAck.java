package de.marcely.sbenlib.network.packets;

import de.marcely.sbenlib.util.BufferedReadStream;
import de.marcely.sbenlib.util.BufferedWriteStream;

public class PacketAck extends Packet {
	
	public Byte[] windows;
	
	@Override
	public byte getType(){
		return Packet.TYPE_ACK;
	}

	@Override
	protected void _encode(BufferedWriteStream stream){
		// do some magic
		// byte b = identifier;
		// b = (byte) (b | (identifier2 << 4));
		
		for(byte window:windows)
			stream.write(window);
	}

	@Override
	protected void _decode(BufferedReadStream stream){
		this.windows = new Byte[stream.available()];
		
		for(int i=0; i<this.windows.length; i++)
			this.windows[i] = stream.readByte();
		
		// this.identifier = (byte) ((d << 4 & 0xFF) >> 4);
		// this.identifier2 = (byte) ((d & 0xFF) >> 4);
	}
}
