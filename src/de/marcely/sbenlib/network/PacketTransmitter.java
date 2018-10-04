package de.marcely.sbenlib.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Nullable;
import javax.crypto.spec.SecretKeySpec;

import de.marcely.sbenlib.compression.Compressor;
import de.marcely.sbenlib.network.packets.Packet;
import de.marcely.sbenlib.network.packets.PacketAck;
import de.marcely.sbenlib.network.packets.PacketNack;
import lombok.Setter;

public abstract class PacketTransmitter {
	
	private static final int PACKET_HEADER_SIZE = 2;
	
	private static final int WINDOW_HIGHEST = 127;
	private static final int WINDOW_LOWEST = -127;
	
	private static final byte SORT_TYPE_START = 0x0;
	private static final byte SORT_TYPE_MIDDLE = 0x1;
	private static final byte SORT_TYPE_END = 0x2;
	private static final byte SORT_TYPE_SOLO = 0x3;
	
	private final PacketsData packets;
	private final int packetSize;
	private final Compressor compressor;
	
	private byte currentSendWindow = 0, currentReceiveWindow = 1;
	private Map<Byte, byte[]> notAckedSendPackets = new HashMap<>();
	private Map<Byte, byte[]> queueSplitReceivePackets = new TreeMap<>();
	private List<Byte> missingPackets = new ArrayList<>();
	
	@Setter private SecretKeySpec key;
	
	public PacketTransmitter(PacketsData packets, int packetSize, Compressor compressor){
		this.packets = packets;
		this.packetSize = packetSize - PACKET_HEADER_SIZE;
		this.compressor = compressor;
	}
	
	public void sendNacks(){
		if(missingPackets.size() == 0) return;
		
		final PacketNack packet = new PacketNack();
			
		packet.windows = missingPackets.toArray(new Byte[this.missingPackets.size()]);
		
		try{
			sendPacket(packet, false);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void sendPacket(Packet packet, boolean needACK) throws Exception {
		// check compression
		boolean compressed;
		byte[] data;
		final byte[] uncompressedData = packet.encode();
		
		if(compressor != null){
			final byte[] compressedData = compressor.encode(uncompressedData);
			
			if(compressedData.length < uncompressedData.length){
				compressed = true;
				data = compressedData;
			}else{
				compressed = false;
				data = uncompressedData;
			}
		
		}else{
			compressed = false;
			data = uncompressedData;
		}
		
		// split data
		byte[][] chunks = new byte[(int) Math.ceil((double) data.length/this.packetSize)][];
		
		if(chunks.length == 0)
			chunks = new byte[1][PACKET_HEADER_SIZE];
		else{
			for(int i=0; i<chunks.length; i++){
				int chunkSize = data.length - i*this.packetSize;
				
				if(chunkSize > this.packetSize)
					chunkSize = this.packetSize;
				
				chunks[i] = new byte[chunkSize+PACKET_HEADER_SIZE];
				
				System.arraycopy(data, i*this.packetSize, chunks[i], PACKET_HEADER_SIZE, chunkSize);
			}
		}
		
		if(chunks.length >= 2){
			needACK = true;
			
			// make sure that the window won't reset itself to min within the packet. else an evil error'd occur
			if(this.currentSendWindow+chunks.length > WINDOW_HIGHEST)
				this.currentSendWindow = WINDOW_LOWEST-1;
		}
		
		// build packets
		for(int i=0; i<chunks.length; i++){
			// get sort type
			byte sortType = SORT_TYPE_SOLO;
			
			if(chunks.length >= 2){
				if(i == 0)
					sortType = SORT_TYPE_START;
				else if(i+1 == chunks.length)
					sortType = SORT_TYPE_END;
				else
					sortType = SORT_TYPE_MIDDLE;
			}
			
			// header
			chunks[i][0] = (byte) (sortType | ((needACK ? 1 : 0) << 2) | ((compressed ? 1 : 0) << 3));
			chunks[i][1] = needACK ? getNextSendWindow() : 0;
			
			// packet is ready!
			send(chunks[i]);
			
			if(needACK)
				this.notAckedSendPackets.put(chunks[i][1], chunks[i]);
		}
	}
	
	public void receiveAck(byte window){
		notAckedSendPackets.remove(window);
	}
	
	public void receiveNack(byte window){
		final byte[] data = notAckedSendPackets.get(window);
		
		if(data != null)
			send(data);
		/*else{
			final PacketPing packet = new PacketPing();
			
			packet.time = 0;
			
			try{
				sendPacket(packet, false);
			}catch(Exception e){
				e.printStackTrace();
			}
		}*/
	}
	
	public void handlePacket(byte[] buffer) throws Exception {
		if(buffer.length < PACKET_HEADER_SIZE) return;
		
		// read header
		final boolean isCompressed = (buffer[0] & 0x8) == 0x8;
		final boolean needACK = (buffer[0] & 0x4) == 0x4;
		final byte sortType = (byte) (buffer[0] & 0x3);
		final byte window = buffer[1];
		
		// send ack
		if(needACK){
			final PacketAck packet = new PacketAck();
			
			packet.windows = new Byte[]{ window };
			
			sendPacket(packet, false);
		}
		
		// read data
		byte[] data = new byte[buffer.length-PACKET_HEADER_SIZE];
		
		System.arraycopy(buffer, PACKET_HEADER_SIZE, data, 0, data.length);
		
		if(isCompressed && compressor != null)
			data = compressor.decode(data);
		
		// check window
		if(needACK){
			if(window != currentReceiveWindow){
				// Packets have been skipped
				if(window > currentReceiveWindow){
					for(byte i=currentReceiveWindow; i<window; i++)
						this.missingPackets.add(i);
					
					this.currentReceiveWindow = getNextWindow(window);
					
				}else{
					// hooray! found lost packet
					if(missingPackets.contains(window))
						missingPackets.remove((Byte) window);
				}
			
			}else
				this.currentReceiveWindow = getNextWindow(window);
		}
		
		// handle data
		if(sortType == SORT_TYPE_SOLO)
			handleData(data);
		else{
			Byte startWindow = null, endWindow = null;
			
			if(sortType == SORT_TYPE_START)
				startWindow = window;
			else if(sortType == SORT_TYPE_END)
				endWindow = window;
			
			if(startWindow == null){
				startWindow = window;
				
				while((startWindow--) >= WINDOW_LOWEST){
					byte[] sData = queueSplitReceivePackets.get(startWindow);
					
					if(sData == null){
						queueSplitReceivePackets.put(window, buffer);
						return;
					}
					
					final byte ssortType = (byte) (sData[0] & 0x3);
					
					if(ssortType == SORT_TYPE_START)
						break;
				}
			}
			
			if(endWindow == null){
				endWindow = window;
				
				while((endWindow++) <= WINDOW_HIGHEST){
					byte[] sData = queueSplitReceivePackets.get(endWindow);
					
					if(sData == null){
						queueSplitReceivePackets.put(window, buffer);
						return;
					}
					
					final byte ssortType = (byte) (sData[0] & 0x3);
					
					if(ssortType == SORT_TYPE_END)
						break;
				}
			}
			
			// found them all!! now combine them
			if(sortType == SORT_TYPE_END)
				queueSplitReceivePackets.put(window, buffer);
			
			final int amount = endWindow-startWindow+1;
			final byte[] nBuffer = new byte[(amount-1)*this.packetSize + queueSplitReceivePackets.get(endWindow).length-PACKET_HEADER_SIZE];
			
			for(byte i=startWindow; i<=endWindow; i++){
				final byte[] chunkData = queueSplitReceivePackets.remove(i);
				
				System.arraycopy(chunkData, PACKET_HEADER_SIZE, nBuffer, (i-startWindow)*this.packetSize, chunkData.length-PACKET_HEADER_SIZE);
			}
			
			// yea
			handleData(nBuffer);
		}
	}
	
	private void handleData(byte[] buffer) throws Exception {
		final Packet packet = PacketDecoder.decode(this.packets, this.key, buffer);
		
		if(packet != null)
			receive(packet);
	}
	
	private @Nullable Byte getNextSendWindow(){
		this.currentSendWindow = getNextWindow(this.currentSendWindow);
		
		//if(notAckedSendPackets.containsKey(currentSendWindow))
		//	new OutOfMemoryError("Failed to optain the next window (Reasons for that could be that the network is lagging too much and or the packet is just too big)").printStackTrace();
		
		return currentSendWindow;
	}
	
	private byte getNextWindow(byte window){
		return (byte) (window+1);
	}
	
	protected abstract void send(byte[] data);
	
	public abstract void receive(Packet packet);
}