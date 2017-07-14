package de.marcely.sbenlib.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import de.marcely.sbenlib.network.ConnectionInfo;
import de.marcely.sbenlib.network.ConnectionState;
import de.marcely.sbenlib.network.packets.PacketData;
import lombok.Getter;

public abstract class SBENServerConnection {
	
	@Getter private final ConnectionInfo connectionInfo;
	private final SocketHandler socketHandler;
	
	@Getter private ConnectionState connectionState = ConnectionState.NotStarted;
	private final List<Timer> registredTimers = new ArrayList<Timer>();
	
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
	
	public boolean close(){
		return this.socketHandler.close();
	}
	
	public void setConnectionState(ConnectionState state){
		if(state == ConnectionState.Disconnected){
			for(Timer t:registredTimers)
				t.cancel();
			
			registredTimers.clear();
		}
		
		onStateChange(state);
		this.connectionState = state;
	}
	
	public void registerTimer(Timer timer){
		this.registredTimers.add(timer);
	}
	
	public void unregisterTimer(Timer timer){
		this.registredTimers.remove(timer);
	}
	
	
	public abstract void onStateChange(ConnectionState state);
	
	public abstract void onPacketReceive(PacketData packet);
}