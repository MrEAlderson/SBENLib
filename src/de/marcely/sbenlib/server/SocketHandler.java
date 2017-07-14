package de.marcely.sbenlib.server;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;

import de.marcely.sbenlib.network.ByteArraysCombiner;
import de.marcely.sbenlib.network.packets.Packet;
import de.marcely.sbenlib.server.protocol.Protocol;
import lombok.Getter;

public class SocketHandler {
	
	@Getter private final SBENServer server;
	@Getter private final Protocol protocol;
	@Getter private final HashMap<String, Session> sessions = new HashMap<String, Session>();
	
	private ByteArraysCombiner combiner;
	
	public SocketHandler(SBENServer server, int maxClients){
		this.server = server;
		combiner = new ByteArraysCombiner(Packet.SEPERATOR[0]);
		
		this.protocol = server.getConnectionInfo().PROTOCOL.getServerInstance(server.getConnectionInfo(), new ServerEventListener(){
			public void onClientRequest(Session session){
				sessions.put(session.getIdentifier(), session);
				System.out.println("Somebody connected");
			}

			public void onClientDisconnect(Session session){
				sessions.remove(session.getIdentifier());
			}

			public void onPacketReceive(Session session, byte[] data){
				final List<byte[]> packets = combiner.addBytes(data);
				
				for(byte[] packet:packets){
					System.out.println(new String(packet));
				}
			}

			public List<Session> getSessions(){
				return (List<Session>) sessions.values();
			}

			public Session getSession(InetAddress address, int port){
				final String identifier = address.getHostAddress() + ":" + port;
				
				return sessions.get(identifier);
			}
			
		}, maxClients);
	}
	
	public boolean isRunning(){
		return this.protocol.isRunning();
	}
	
	public int getMaxClients(){
		return this.protocol.getMaxClients();
	}
	
	public ServerStartInfo run(){
		if(!isRunning())
			return protocol.run();
		else
			return ServerStartInfo.FAILED_ALREADYRUNNING;
	}
}
