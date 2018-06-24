package de.marcely.sbenlib.network.packets.data;

import javax.crypto.spec.SecretKeySpec;

import de.marcely.sbenlib.client.SBENServerConnection;
import de.marcely.sbenlib.compression.AES;

public abstract class SecuredPacket extends NormalPacket {
	
	private SecretKeySpec key;
	
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
		if(key == null){
			new NullPointerException("Missing key/session").printStackTrace();
			return this.writeStream.toByteArray();
		}
		
		return AES.encrypt(this.writeStream.toByteArray(), key);
	}

	@Override
	public byte[] getReadBytes(byte[] bytes){
		if(key == null){
			new NullPointerException("Missing key/session").printStackTrace();
			return bytes;
		}
		
		return AES.decrypt(bytes, key);
	}
	
	public void setConnection(SBENServerConnection conn){
		this.key = conn.getKey();
	}
}
