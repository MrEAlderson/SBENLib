package de.marcely.sbenlib.network.packets.data;

import de.marcely.sbenlib.util.BufferedReadStream;
import de.marcely.sbenlib.util.BufferedWriteStream;

public abstract class NormalPacket extends DataPacket {
	
	@Override
	public byte getTypeID(){
		return DataPacket.TYPE_NORMAL;
	}
	
	public abstract byte getPacketID();
	
	protected abstract void write(BufferedWriteStream stream);
	
	protected abstract void read(BufferedReadStream stream);
	
	public void encode(BufferedWriteStream stream){
		write(stream);
	}
	
	public void decode(BufferedReadStream stream){
		read(stream);
	}
}