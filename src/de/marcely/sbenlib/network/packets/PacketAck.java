package de.marcely.sbenlib.network.packets;

public class PacketAck extends Packet {
	
	public byte window;
	
	@Override
	public byte getType(){
		return Packet.TYPE_ACK;
	}

	@Override
	protected byte[] _encode(){
		// do some magic
		// byte b = identifier;
		// b = (byte) (b | (identifier2 << 4));
		
		this.writeStream.write(window);
		
		return this.writeStream.toByteArray();
	}

	@Override
	protected void _decode(byte[] data){
		this.window = this.readStream.readByte();
		
		// this.identifier = (byte) ((d << 4 & 0xFF) >> 4);
		// this.identifier2 = (byte) ((d & 0xFF) >> 4);
	}
}
