package de.marcely.sbenlib.network.packets;

import de.marcely.sbenlib.util.Util;

public class PacketData extends Packet {
	
	public static final byte PRIORITY_LOW = (byte) 0x0; // don't care if delivered or not
	public static final byte PRIORITY_HIGH = (byte) 0x1; // care if delivered
	public static final byte PRIORITY_VERY_HIGH = (byte) 0x2; // care if delivered and if it's in the correct order
	
	public int id = Util.RAND.nextInt(255);
	
	@Override
	public byte getType(){
		return 0;
	}
	
	@Override
	protected byte[] _encode(){
		return null;
	}
	
	@Override
	protected void _decode(byte[] data){
		
	}
}