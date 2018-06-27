package de.marcely.sbenlib.network.packets;

import javax.crypto.spec.SecretKeySpec;

import de.marcely.sbenlib.network.PacketsData;
import de.marcely.sbenlib.network.packets.data.DataPacket;
import de.marcely.sbenlib.util.BufferedReadStream;
import de.marcely.sbenlib.util.BufferedWriteStream;

public class PacketData extends Packet {
	
	public DataPacket data;
	public PacketsData packetsData;
	public SecretKeySpec _key;
	
	@Override
	public byte getType(){
		return TYPE_DATA;
	}
	
	@Override
	protected void _encode(BufferedWriteStream stream){
		stream.writeByte(data.getPacketID());
		data.encode(stream);
	}
	
	@Override
	protected void _decode(BufferedReadStream stream){
		final byte packetId = stream.readByte();
		
		this.data = packetsData.getPacket(packetId);
		
		if(this.data != null){
			this.data.set_key(_key);
			this.data.decode(stream);
		}
	}
}