package de.marcely.sbenlib.client;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.crypto.spec.SecretKeySpec;

import de.marcely.sbenlib.network.ConnectionInfo;
import de.marcely.sbenlib.network.ConnectionState;
import de.marcely.sbenlib.network.PacketsData;
import de.marcely.sbenlib.network.packets.data.DataPacket;
import de.marcely.sbenlib.network.packets.data.SecuredPacket;
import de.marcely.sbenlib.util.SThread.ThreadType;
import de.marcely.sbenlib.util.TickTimer;
import de.marcely.sbenlib.util.SThread;
import lombok.Getter;
import lombok.Setter;

public abstract class SBENServerConnection {
	
	public boolean addShutdownHook = true;
	
	@Getter private final ConnectionInfo connectionInfo;
	private final SocketHandler socketHandler;
	
	@Getter private ConnectionState connectionState = ConnectionState.NotStarted;
	@Getter @Setter private long ping = 0;
	private final List<TickTimer> registredTimers = new ArrayList<TickTimer>();
	@Getter @Setter private PacketsData packetsData = new PacketsData();
	@Getter public SecretKeySpec key = null;
	
	private Thread shutdownHook = null;
	
	public SBENServerConnection(ConnectionInfo connInfo){
		this.connectionInfo = connInfo;
		this.socketHandler = new SocketHandler(this);
	}
	
	public boolean isRunning(){
		return this.socketHandler.isRunning();
	}
	
	public boolean run(){
		final boolean result = this.socketHandler.run();
		
		if(addShutdownHook && result){
			shutdownHook = new SThread(ThreadType.ShutdownHook_Client){
				protected void _run(){
					if(isRunning())
						close("DISCONNECTED");
				}
			};
			Runtime.getRuntime().addShutdownHook(shutdownHook);
		}
		
		return result;
	}
	
	public boolean close(){
		return this.socketHandler.close();
	}
	
	public boolean close(@Nullable String reason){
		return this.socketHandler.close(reason);
	}
	
	public void setConnectionState(ConnectionState state){
		if(this.connectionState == state)
			return;
		
		if(state == ConnectionState.Disconnected){
			for(TickTimer t:registredTimers)
				t.stop();
			
			registredTimers.clear();
		}
		
		onStateChange(state);
		this.connectionState = state;
	}
	
	public void registerTimer(TickTimer timer){
		this.registredTimers.add(timer);
	}
	
	public void unregisterTimer(TickTimer timer){
		this.registredTimers.remove(timer);
	}
	
	public void sendPacket(DataPacket packet){
		sendPacket(packet, true);
	}
	
	public void sendPacket(DataPacket packet, boolean needACK){
		if(packet instanceof SecuredPacket)
			((SecuredPacket) packet).setConnection(this);
		
		this.socketHandler.sendPacket(packet, needACK);
	}
	
	public abstract void onStateChange(ConnectionState state);
	
	public abstract void onPacketReceive(DataPacket packet);
	
	public abstract void onDisconnect(String reason);
}