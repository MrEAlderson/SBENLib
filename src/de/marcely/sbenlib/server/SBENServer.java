package de.marcely.sbenlib.server;

import de.marcely.sbenlib.network.ConnectionInfo;
import de.marcely.sbenlib.network.PacketsData;
import lombok.Getter;
import lombok.Setter;

public abstract class SBENServer {
	
	@Getter private final ConnectionInfo connectionInfo;
	@Getter private final SocketHandler socketHandler;
	@Getter @Setter private PacketsData packetsData = new PacketsData();
	
	public SBENServer(ConnectionInfo connInfo, int maxClients){
		this.connectionInfo = connInfo;
		this.socketHandler = new SocketHandler(this, 0);
	}
	
	public boolean isRunning(){
		return this.socketHandler.isRunning();
	}
	
	public int getMaxClients(){
		return this.socketHandler.getMaxClients();
	}
	
	public ServerStartInfo run(){
		return socketHandler.run();
	}
	
	public boolean close(){
		return socketHandler.close();
	}
	
	public abstract void onSessionRequest(Session session);
}