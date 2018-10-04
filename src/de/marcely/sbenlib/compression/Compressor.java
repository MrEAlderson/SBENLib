package de.marcely.sbenlib.compression;

public interface Compressor {
	
	public abstract CompressionType getType();
	
	public abstract byte[] encode(byte[] data) throws Exception;
	
	public abstract byte[] decode(byte[] data) throws Exception;
}
