package de.marcely.sbenlib.compression;

public enum CompressionType {
	
	ZLib,
	GLib;
	
	public Compresser getInstance(){
		switch(this){
		case ZLib:
			return ZLib.getInstance();
		case GLib:
			return GLib.getInstance();
		default:
			return null;
		}
	}
}
