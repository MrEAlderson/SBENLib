package de.marcely.sbenlib.server;

import java.net.InetAddress;
import java.util.List;

public interface ServerEventListener {
	
	public void onClientRequest(Session session);
	
	public void onClientDisconnect(Session session);
	
	public void onPacketReceive(Session session, byte[] packet);
	
	public List<Session> getSessions();
	
	public Session getSession(InetAddress address, int port);
}