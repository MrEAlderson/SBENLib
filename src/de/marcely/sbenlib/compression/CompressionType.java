package de.marcely.sbenlib.compression;

import lombok.Getter;

public enum CompressionType {
	
	None(null),
	ZLib(new ZLibCompression());
	
	@Getter private final Compressor instance;
	
	private CompressionType(Compressor instance){
		this.instance = instance;
	}
}
