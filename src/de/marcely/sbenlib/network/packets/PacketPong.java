package de.marcely.sbenlib.network.packets;

import de.marcely.sbenlib.util.BufferedReadStream;
import de.marcely.sbenlib.util.BufferedWriteStream;

public class PacketPong extends Packet {
	
	public long time;
	
	@Override
	public byte getType(){
		return TYPE_PONG;
	}

	@Override
	protected void _encode(BufferedWriteStream stream){
		stream.writeSignedLong(time);
	}

	@Override
	protected void _decode(BufferedReadStream stream){
		time = stream.readSignedLong();
	}
}
