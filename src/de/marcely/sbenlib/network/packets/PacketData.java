package de.marcely.sbenlib.network.packets;

import de.marcely.sbenlib.network.PacketsData;
import de.marcely.sbenlib.network.packets.data.DataPacket;
import de.marcely.sbenlib.util.BufferedWriteStream;
import de.marcely.sbenlib.util.Util;

public class PacketData extends Packet {
	
	public static final byte PRIORITY_LOW = (byte) 0x0; // don't care if delivered or not
	public static final byte PRIORITY_HIGH = (byte) 0x1; // care if delivered
	public static final byte PRIORITY_VERY_HIGH = (byte) 0x2; // care if delivered and if it's in the correct order
	
	public int id = Util.RAND.nextInt(255);
	public byte priority = PRIORITY_LOW;
	
	public DataPacket data;
	public PacketsData packetsData;
	
	@Override
	public byte getType(){
		return TYPE_DATA;
	}
	
	@Override
	protected byte[] _encode(){
		this.writeStream.writeUnsignedShort(id);
		this.writeStream.writeByte(priority);
		this.writeStream.writeByte(data.getPacketID());
		
		data.setWriteStream(new BufferedWriteStream());
		data.encode();
		this.writeStream.write(data.getWriteBytes());
		
		return this.writeStream.toByteArray();
	}
	
	@Override
	protected void _decode(byte[] data){
		this.id = this.readStream.readUnsignedShort();
		this.priority = this.readStream.readByte();
		
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