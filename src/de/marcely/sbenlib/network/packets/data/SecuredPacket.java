package de.marcely.sbenlib.network.packets.data;

import java.io.IOException;

import de.marcely.sbenlib.compression.AES;
import de.marcely.sbenlib.util.BufferedReadStream;
import de.marcely.sbenlib.util.BufferedWriteStream;

public abstract class SecuredPacket extends NormalPacket {
	
	@Override
	public byte getTypeID(){
		return DataPacket.TYPE_SECURED;
	}
	
	protected abstract void write(BufferedWriteStream stream);
	
	protected abstract void read(BufferedReadStream stream);
	
	public abstract byte getPacketID();
	
	public void encode(BufferedWriteStream stream){
		if(_key == null){
			new NullPointerException("Missing key/session").printStackTrace();
			return;
		}
		
		final BufferedWriteStream stream2 = new BufferedWriteStream();
		
		write(stream2);
		
		stream.write(AES.encrypt(stream2.toByteArray(), _key));
		
		try{
			stream2.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void decode(BufferedReadStream stream){
		if(_key == null){
			new NullPointerException("Missing key/session").printStackTrace();
			return;
		}
		
		final BufferedReadStream stream2 = new BufferedReadStream(AES.decrypt(stream.read(stream.available()), _key));
		
		read(stream2);
		
		try{
			stream2.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}
