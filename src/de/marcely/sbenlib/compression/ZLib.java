package de.marcely.sbenlib.compression;

import lombok.Getter;

public class ZLib implements Compresser {
	
	@Getter public static ZLib instance = new ZLib();
	
	@Override
	public CompressionType getType(){
		return CompressionType.ZLib;
	}

	@Override
	public byte[] encode(byte[] data){
		return null;
	}

	@Override
	public byte[] decode(byte[] data){
		return null;
	}
}
