package de.marcely.sbenlib.client.protocol;

import de.marcely.sbenlib.client.ServerEventListener;
import de.marcely.sbenlib.compression.Base64;
import de.marcely.sbenlib.network.ConnectionInfo;
import de.marcely.sbenlib.network.ProtocolType;
import de.marcely.sbenlib.network.packets.Packet;
import de.marcely.sbenlib.util.Util;
import lombok.Getter;

public abstract class Protocol {
	
	@Getter protected final ConnectionInfo connectionInfo;
	@Getter protected final ServerEventListener listener;
	
	@Getter protected Thread thread = null;
	
	@Getter protected boolean running = false;
	
	public Protocol(ConnectionInfo connInfo, ServerEventListener listener){
		this.connectionInfo = connInfo;
		this.listener = listener;
	}
	
	public boolean sendPacket(byte[] packet){
		_sendPacket(Base64.encode(packet));
		Util.sleep(10);
		return _sendPacket(Packet.SEPERATOR);
	}
	
	public boolean sendPacket(Packet packet){
		return sendPacket(packet.getWriteStream().toByteArray());
	}
	
	
	public abstract ProtocolType getType();
	
	public abstract boolean run();
	
	public abstract boolean close();
	
	protected abstract boolean _sendPacket(byte[] packet);
}
