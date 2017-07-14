package de.marcely.sbenlib.client;

public interface ServerEventListener {
	
	public abstract void onPacketReceive(byte[] bytes);
}
