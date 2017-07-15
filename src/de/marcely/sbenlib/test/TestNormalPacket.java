package de.marcely.sbenlib.test;

import de.marcely.sbenlib.network.packets.data.NormalPacket;

public class TestNormalPacket extends NormalPacket {
	
	public String testString = "This is a normal packet!";
	
	@Override
	public byte getPacketID(){
		return 0x0;
	}

	@Override
	public void encode(){
		this.writeStream.writeString(testString);
	}

	@Override
	public void decode(){
		System.out.println("DECODE");
		System.out.println(this.readStream.readString());
	}
}
