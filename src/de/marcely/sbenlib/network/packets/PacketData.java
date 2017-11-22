package de.marcely.sbenlib.network.packets;

import de.marcely.sbenlib.network.PacketsData;
import de.marcely.sbenlib.network.packets.data.DataPacket;
import de.marcely.sbenlib.util.BufferedWriteStream;

public class PacketData extends Packet {
	
	public DataPacket data;
	public PacketsData packetsData;
	
	@Override
	public byte getType(){
		return TYPE_DATA;
	}
	
	@Override
	protected byte[] _encode(){
		this.writeStream.writeByte(data.getPacketID());
		
		data.setWriteStream(new BufferedWriteStream());
		data.encode();
		this.writeStream.write(data.getWriteBytes());
		
		return this.writeStream.toByteArray();
	}
	
	@Override
	protected void _decode(byte[] data){
		final byte packetId = this.readStream.readByte();
		final DataPacket packet = packetsData.getPacket(packetId);
		
		if(packet != null){
			this.data = packetsData.getPacket(packetId);
			this.data.setReadStream(this.readStream);
			this.data.decode();
		
		}else
			this.data = null;
	}
}