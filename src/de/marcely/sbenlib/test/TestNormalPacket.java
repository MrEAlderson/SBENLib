package de.marcely.sbenlib.test;

import de.marcely.sbenlib.network.packets.data.NormalPacket;
import de.marcely.sbenlib.util.BufferedReadStream;
import de.marcely.sbenlib.util.BufferedWriteStream;

public class TestNormalPacket extends NormalPacket {
	
	public String testString = "This is a normal packet!";
	
	@Override
	public byte getPacketID(){
		return 0x0;
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
