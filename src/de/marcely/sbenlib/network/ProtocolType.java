package de.marcely.sbenlib.network;

public enum ProtocolType {
	
	UDP,
	TCP;
	
	public de.marcely.sbenlib.client.protocol.Protocol getClientInstance(ConnectionInfo conn, de.marcely.sbenlib.client.ServerEventListener listener){
		switch(this){
		case UDP:
			return new de.marcely.sbenlib.client.protocol.UDPProtocol(conn, listener);
		case TCP:
			return new de.marcely.sbenlib.client.protocol.TCPProtocol(conn, listener);
		default:
			return null;
		}
	}
	
	public de.marcely.sbenlib.server.protocol.Protocol getServerInstance(ConnectionInfo conn, de.marcely.sbenlib.server.ServerEventListener listener, int maxClients){
		switch(this){
		case UDP:
			return new de.marcely.sbenlib.server.protocol.UDPProtocol(conn, listener, maxClients);
		case TCP:
			return new de.marcely.sbenlib.server.protocol.TCPProtocol(conn, listener, maxClients);
		default:
			return null;
		}
	}
}