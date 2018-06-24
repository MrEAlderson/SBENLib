package de.marcely.sbenlib.network;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import de.marcely.sbenlib.network.packets.Packet;
import de.marcely.sbenlib.util.BufferedReadStream;
import de.marcely.sbenlib.util.BufferedWriteStream;

public abstract class PacketTransmitter {
	
	private static final int PACKET_PART_SIZE = 512;
	private static final byte MAX_WINDOWS = (byte) 128;
	
	private final PacketsData packets;
	private byte currentSendWindow = 0, currentReceiveWindow = 0;
	private Map<Byte, byte[]> notAckedSendPackets = new HashMap<>();
	
	public PacketTransmitter(PacketsData packets){
		this.packets = packets;
	}
	
	public boolean sendPacket(Packet packet, PacketPriority priority){
		final byte[] totalBuffer = packet.encode();
		int parts = totalBuffer.length/PACKET_PART_SIZE;
		
		if(parts%PACKET_PART_SIZE != 0) parts++;
		
		if(parts + notAckedSendPackets.size() >= MAX_WINDOWS){
			new OutOfMemoryError("Out of available windows").printStackTrace();
			return false;
		}
		
		final BufferedWriteStream stream = new BufferedWriteStream(PACKET_PART_SIZE+1);
		
		for(int i=0; i<parts; i++){
			stream.write(currentSendWindow | (i+1 == parts ? 1 : 0 << 7));
			stream.write(totalBuffer, i*PACKET_PART_SIZE, PACKET_PART_SIZE);
			
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
		send(notAckedSendPackets.get(window));
	}
	
	public void receiveNack(byte window){
		notAckedSendPackets.remove(window);
	}
	
	public void handlePacket(byte[] buffer){
		final BufferedReadStream stream = new BufferedReadStream(buffer);
		final byte header = stream.readByte();
		final boolean isFinal = header >> 7 == -1;
		final byte window = (byte) (header ^ (isFinal ? 0x80 : 0x00));
		
		System.out.println(window + " " + isFinal);
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