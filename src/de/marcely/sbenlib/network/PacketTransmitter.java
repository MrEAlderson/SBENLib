package de.marcely.sbenlib.network;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import javax.annotation.Nullable;

import de.marcely.sbenlib.network.packets.Packet;
import de.marcely.sbenlib.network.packets.PacketAck;
import de.marcely.sbenlib.util.TickTimer;

public abstract class PacketTransmitter {
	
	public long rate = 10, resendFrequency = 100;
	
	// send queue
	private final Queue<QueuedPacket> sendQueue = new LinkedList<>();
	private final Map<Byte, QueuedPacket> sendQueue2 = new HashMap<>();
	private byte cIdentifier = 0x0;
	
	// receive queue
	private final Map<Byte, Byte> receiveIgnoreIdentifiers = new HashMap<>();
	
	private TickTimer timer;
	
	public PacketTransmitter(){
		timer = new TickTimer(true, rate){
			public void onRun(){
				// send packets
				while(sendQueue.size() >= 1){
					final QueuedPacket packet = sendQueue.peek();
					
					// set identifier
					if(packet.packet._identifier == null){
						if(packet.priority == PacketPriority.NORMAL)
							packet.packet._identifier = (byte) 0x0;
						else{
							final Byte id = getNextBestIdentifier();
							
							if(id != null){
								packet.packet._identifier = id;
								sendQueue2.put(id, packet);
							}else
								continue;
						}
					}
					
					// send
					if(System.currentTimeMillis() - packet.lastTimeSend > resendFrequency)
						send(packet.packet, packet.data);
					
					// remove from queue
					if(packet.priority == PacketPriority.NORMAL)
						sendQueue.remove();
					// skip next ones cuz priority
					else if(packet.priority == PacketPriority.DONT_LOSE_AND_SORTED)
						return;
				}
			}
		};
	}
	
	public boolean isRunning(){
		return timer.isRunning();
	}
	
	public boolean run(){
		if(isRunning()) return false;
		
		timer.start();
		
		return true;
	}
	
	public boolean stop(){
		if(!isRunning()) return false;
		
		timer.stop();
		
		return true;
	}
	
	public boolean addToSendQueue(Packet packet, PacketPriority priority, Object... data){
		if(isRunning()) return false;
		
		final QueuedPacket qPacket = new QueuedPacket(packet, priority, data);
		
		sendQueue.add(qPacket);
		
		return true;
	}
	
	private @Nullable Byte getNextBestIdentifier(){
		for(byte i=0; i<16; i++){
			cIdentifier++;
			
			if(cIdentifier >= 16) cIdentifier = 0x0;
			
			if(!sendQueue2.containsKey(cIdentifier))
				return i;
		}
		
		return null;
	}
	
	public void onReceiveAck(PacketAck packet, boolean nak){
		if(!nak){
			final QueuedPacket qPacket = sendQueue2.get(packet._identifier);
			
			sendQueue.remove(qPacket);
			sendQueue2.remove(packet._identifier);
		}
	}
	
	public void onReceivePacket(Packet packet, Object... data){		
		if(!packet._sendAck){
			receive(packet, data);
			return;
		}
		
		Byte id2 = receiveIgnoreIdentifiers.get(packet._identifier);
		
		if(id2 != null && id2 != packet._identifer2){
			receiveIgnoreIdentifiers.remove(packet._identifier);
			id2 = null;
		}
		
		if(id2 == null){
			receiveIgnoreIdentifiers.put(packet._identifier, packet._identifer2);
			receive(packet, data);
		}
	}
	
	protected abstract void send(Packet packet, Object[] data);
	
	public abstract void receive(Packet packet, Object[] data);
}