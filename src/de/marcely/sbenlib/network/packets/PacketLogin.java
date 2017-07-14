package de.marcely.sbenlib.network.packets;

import de.marcely.sbenlib.network.Network;

public class PacketLogin extends Packet {

	public byte[] security_id;
	public int version_protocol;
	
	@Override
	public byte getType(){
		return TYPE_LOGIN;
	}

	@Override
	protected byte[] _encode(){
		this.writeStream.write(this.security_id);
		this.writeStream.writeUnsignedShort(this.version_protocol);
		
		return this.writeStream.toByteArray();
	}

	@Override
	protected void _decode(byte[] data){
		this.security_id = this.readStream.read(Network.SECURITYID_LENGTH);
		this.version_protocol = this.readStream.readUnsignedShort();
	}
}