package de.marcely.sbenlib.test;

import de.marcely.sbenlib.network.packets.data.SecuredPacket;

public class TestSecuredPacket extends SecuredPacket {
	
	public String testString = "This is a cool secured packet!";
	
	@Override
	public byte getPacketID(){
		return 0x1;
	}

	@Override
	public void encode(){
		this.writeStream.writeString(testString);
	}

	@Override
	public void decode(){
		System.out.println(this.readStream.readString());
	}
}
