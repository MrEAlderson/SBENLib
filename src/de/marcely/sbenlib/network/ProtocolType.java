package de.marcely.sbenlib.network;

import de.marcely.sbenlib.server.SBENServer;
import lombok.Getter;

public enum ProtocolType {
	
	UDP(true, 65507),
	TCP(false, 1500);
	
	@Getter private final boolean requiresAckNack;
	@Getter private final int maxPacketSize;
	
	private ProtocolType(boolean requiresAckNack, int maxPacketSize){
		this.requiresAckNack = requiresAckNack;
		this.maxPacketSize = maxPacketSize;
	}
	
	public de.marcely.sbenlib.client.protocol.Protocol getClientInstance(ConnectionInfo conn, de.marcely.sbenlib.client.SocketHandler socketHandler, de.marcely.sbenlib.client.ServerEventListener listener){
		switch(this){
		case UDP:
			return new de.marcely.sbenlib.client.protocol.UDPProtocol(conn, socketHandler, listener);
		case TCP:
			return new de.marcely.sbenlib.client.protocol.TCPProtocol(conn, socketHandler, listener);
		default:
			return null;
		}
	}
	
	public de.marcely.sbenlib.server.protocol.Protocol getServerInstance(ConnectionInfo conn, SBENServer server, de.marcely.sbenlib.server.ServerEventListener listener, int maxClients){
		switch(this){
		case UDP:
			return new de.marcely.sbenlib.server.protocol.UDPProtocol(conn, server, listener, maxClients);
		case TCP:
			return new de.marcely.sbenlib.server.protocol.TCPProtocol(conn, server, listener, maxClients);
		default:
			return null;
		}
	}
}