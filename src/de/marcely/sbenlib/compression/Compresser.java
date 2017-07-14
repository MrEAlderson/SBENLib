package de.marcely.sbenlib.compression;

public interface Compresser {
	
	public abstract CompressionType getType();
	
	public abstract byte[] encode(byte[] data);
	
	public abstract byte[] decode(byte[] data);
}
