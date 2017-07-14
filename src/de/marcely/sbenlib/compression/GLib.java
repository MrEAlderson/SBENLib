package de.marcely.sbenlib.compression;

import lombok.Getter;

public class GLib implements Compresser {
	
	@Getter public static GLib instance = new GLib();

	@Override
	public CompressionType getType(){
		return CompressionType.GLib;
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
