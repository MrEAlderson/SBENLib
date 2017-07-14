package de.marcely.sbenlib.client;

import de.marcely.sbenlib.network.ConnectionInfo;
import lombok.Getter;

public class SBENServerConnection {
	
	@Getter private final ConnectionInfo connectionInfo;
	private final SocketHandler socketHandler;
	
	public SBENServerConnection(ConnectionInfo connInfo){
		this.connectionInfo = connInfo;
		this.socketHandler = new SocketHandler(this);
	}
	
	public boolean isRunning(){
		return this.socketHandler.isRunning();
	}
	
	public boolean run(){
		return this.socketHandler.run();
	}
}
