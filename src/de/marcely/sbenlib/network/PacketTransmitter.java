package de.marcely.sbenlib.network;

import java.util.LinkedList;
import java.util.Queue;

import de.marcely.sbenlib.network.packets.Packet;
import de.marcely.sbenlib.util.TickTimer;

public abstract class PacketTransmitter {
	
	public long rate = 10, sendFrequency = 100;
	
	private final Queue<QueuedPacket> sendQueue = new LinkedList<>();
	private TickTimer timer;
	
	public PacketTransmitter(){
		
	}
	
	public boolean isRunning(){
		return timer != null;
	}
	
	public boolean run(){
		if(isRunning()) return false;
		
		timer = new TickTimer(true, rate){
			public void onRun(){
				while(sendQueue.size() >= 1){
					final QueuedPacket packet = sendQueue.peek();
					
					if(packet.removeFromQueue){
						sendQueue.remove();
						continue;
					}
					
					if(System.currentTimeMillis() - packet.lastTimeSend > sendFrequency)
						send(packet.packet);
					
					if(packet.priority == PacketPriority.NORMAL)
						sendQueue.remove();
				}
			}
		};
		
		return true;
	}
	
	public boolean addToSendQueue(Packet packet, PacketPriority priority){
		if(isRunning()) return false;
		
		sendQueue.add(new QueuedPacket(packet, priority));
		
		return true;
	}
	
	protected abstract void send(Packet packet);
}