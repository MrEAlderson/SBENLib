package de.marcely.sbenlib.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Nullable;

import de.marcely.sbenlib.network.packets.Packet;
import de.marcely.sbenlib.network.packets.PacketAck;
import de.marcely.sbenlib.network.packets.PacketNack;
import de.marcely.sbenlib.network.packets.PacketPing;
import de.marcely.sbenlib.util.BufferedReadStream;
import de.marcely.sbenlib.util.BufferedWriteStream;

public abstract class PacketTransmitter {
	
	private static final int PACKET_PART_SIZE = 512;
	private static final int MAX_WINDOWS = 128;
	
	private final PacketsData packets;
	private byte currentSendWindow = 0, currentReceiveWindow = 0;
	private Map<Byte, byte[]> notAckedSendPackets = new HashMap<>();
	private Map<Byte, byte[]> queueSplitReceivePackets = new TreeMap<>();
	private List<Byte> missingPackets = new ArrayList<>();
	
	public PacketTransmitter(PacketsData packets){
		this.packets = packets;
	}
	
	public void sendNacks(){
		if(missingPackets.size() == 0) return;
		
		final PacketNack packet = new PacketNack();
			
		packet.windows = missingPackets.toArray(new Byte[this.missingPackets.size()]);
		
		sendPacket(packet, false);
	}
	
	public boolean sendPacket(Packet packet, boolean needACK){
		final byte[] totalBuffer = packet.encode();
		int parts = totalBuffer.length/PACKET_PART_SIZE;
		
		if(totalBuffer.length%PACKET_PART_SIZE != 0) parts++;
		
		if(parts + notAckedSendPackets.size() >= MAX_WINDOWS){
			new OutOfMemoryError("Out of available windows").printStackTrace();
			return false;
		}
		
		final BufferedWriteStream stream = new BufferedWriteStream(PACKET_PART_SIZE+1);
		
		for(int i=0; i<parts; i++){
			final int remaining = totalBuffer.length-i*PACKET_PART_SIZE;
			
			stream.write(currentSendWindow | (i+1 == parts ? 1 : 0) << 7);
			stream.write(totalBuffer, i*PACKET_PART_SIZE, remaining > PACKET_PART_SIZE ? PACKET_PART_SIZE : remaining);
			
			final byte[] buffer = stream.toByteArray();
			
			if(packet._needAck)
				notAckedSendPackets.put(getNextSendWindow(), buffer);
			
			send(buffer);
			
			if(i+1 != parts)
				stream.reset();
		}
		
		try{
			stream.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		return true;
	}
	
	public void receiveAck(byte window){
		notAckedSendPackets.remove(window);
	}
	
	public void receiveNack(byte window){
		final byte[] data = notAckedSendPackets.get(window);
		
		if(data != null)
			send(data);
		else{
			final PacketPing packet = new PacketPing();
			
			packet.time = 0;
			
			sendPacket(packet, false);
		}
	}
	
	public void handlePacket(byte[] buffer){
		BufferedReadStream stream = new BufferedReadStream(buffer);
		final byte header = stream.readByte();
		final boolean isFinal = header >> 7 == -1;
		final byte window = (byte) (header ^ (isFinal ? 0x80 : 0x00));
		final byte[] data = stream.read(stream.available());
		
		try{
			stream.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		// do window check
		if(window != currentReceiveWindow){
			if(window > currentReceiveWindow){
				for(byte i=currentReceiveWindow; i<window; i++)
					missingPackets.add(i);
			
			}else{
				for(byte i=currentReceiveWindow; i<MAX_WINDOWS; i++)
					missingPackets.add(i);
				for(byte i=0; i<window; i++)
					missingPackets.add(i);
			}
			
			currentReceiveWindow = (byte) (window+1);
			
			if(currentReceiveWindow >= MAX_WINDOWS)
				currentReceiveWindow = 0;
		}
		
		// handle packet
		Byte[] windows = null;
		
		if(isFinal){
			if(queueSplitReceivePackets.size() >= 1){
				queueSplitReceivePackets.put(window, data);
				
				final BufferedWriteStream stream2 = new BufferedWriteStream(queueSplitReceivePackets.size()*PACKET_PART_SIZE);
				
				for(byte[] d:queueSplitReceivePackets.values())
					stream2.write(d);
				
				final byte[] b = stream2.toByteArray();
				
				try{
					stream2.close();
					
					final Packet packet = PacketDecoder.decode(this.packets, b);
					
					if(packet != null){
						receive(packet);
						
						if(packet._needAck)
							windows = queueSplitReceivePackets.keySet().toArray(new Byte[queueSplitReceivePackets.size()]);
					}
				}catch(Exception e){
					e.printStackTrace();
				}
				
				queueSplitReceivePackets.clear();
				
			}else{
				try{
					final Packet packet = PacketDecoder.decode(this.packets, data);
					
					if(packet != null){
						receive(packet);
						
						if(packet._needAck)
							windows = new Byte[]{ window };
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		
		}else
			queueSplitReceivePackets.put(window, data);
		
		// send ack
		if(windows != null){
			final PacketAck nPacket = new PacketAck();
			
			nPacket.windows = windows;
			
			sendPacket(nPacket, false);
		}
	}
	
	private @Nullable Byte getNextSendWindow(){
		currentSendWindow++;
		
		if(currentSendWindow >= MAX_WINDOWS) currentSendWindow = 0;
		
		if(notAckedSendPackets.containsKey(currentSendWindow))
			new OutOfMemoryError("Failed to optain the next window").printStackTrace();
		
		return currentSendWindow;
	}
	
	protected abstract void send(byte[] data);
	
	public abstract void receive(Packet packet);
}