package de.marcely.sbenlib.exception;

public class PacketDecodeException extends Exception {
	private static final long serialVersionUID = -4465236423625520776L;
	
	public PacketDecodeException(){
		super("Failed to decode a packet");
	}
}
