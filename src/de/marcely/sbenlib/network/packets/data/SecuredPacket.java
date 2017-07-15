package de.marcely.sbenlib.network.packets.data;

public abstract class SecuredPacket extends NormalPacket {
	
	@Override
	public byte getTypeID(){
		return DataPacket.TYPE_SECURED;
	}
	
	@Override
	public abstract byte getPacketID();

	@Override
	public abstract void encode();

	@Override
	public abstract void decode();

	@Override
	public byte[] getWriteBytes(){
		return this.writeStream.toByteArray();
	}

	@Override
	public byte[] getReadBytes(byte[] bytes){
		return bytes;
	}
}
