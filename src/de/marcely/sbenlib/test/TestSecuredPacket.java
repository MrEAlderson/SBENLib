package de.marcely.sbenlib.test;

import de.marcely.sbenlib.network.packets.data.SecuredPacket;
import de.marcely.sbenlib.util.BufferedReadStream;
import de.marcely.sbenlib.util.BufferedWriteStream;

public class TestSecuredPacket extends SecuredPacket {
	
	public String testString = "This is a cool secured packet!";
	
	@Override
	public byte getPacketID(){
		return 0x1;
	}

	@Override
	public void write(BufferedWriteStream stream){
		stream.writeString(testString);
	}

	@Override
	public void read(BufferedReadStream stream){
		System.out.println("DECODE");
		System.out.println(stream.readString());
	}
}
