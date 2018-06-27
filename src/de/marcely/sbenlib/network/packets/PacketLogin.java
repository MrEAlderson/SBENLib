package de.marcely.sbenlib.network.packets;

import de.marcely.sbenlib.network.Network;
import de.marcely.sbenlib.util.BufferedReadStream;
import de.marcely.sbenlib.util.BufferedWriteStream;

public class PacketLogin extends Packet {

	public byte[] security_id;
	public int version_protocol;
	
	@Override
	public byte getType(){
		return TYPE_LOGIN;
	}

	@Override
	protected void _encode(BufferedWriteStream stream){
		stream.write(this.security_id);
		stream.writeUnsignedShort(this.version_protocol);
	}

	@Override
	protected void _decode(BufferedReadStream stream){
		this.security_id = stream.read(Network.SECURITYID_LENGTH);
		this.version_protocol = stream.readUnsignedShort();
	}
}