package de.marcely.sbenlib.network.packets;

import de.marcely.sbenlib.util.BufferedReadStream;
import de.marcely.sbenlib.util.BufferedWriteStream;

public class PacketClose extends Packet {
	
	public String reason = "UNKOWN";
	
	@Override
	public byte getType(){
		return TYPE_CLOSE;
	}

	@Override
	protected void _encode(BufferedWriteStream stream){
		stream.writeString(reason);
	}

	@Override
	protected void _decode(BufferedReadStream stream){
		this.reason = stream.readString();
	}
}
