package de.marcely.sbenlib.network;

import javax.annotation.Nullable;

import de.marcely.sbenlib.network.packets.Packet;
import de.marcely.sbenlib.network.packets.PacketAck;
import de.marcely.sbenlib.network.packets.PacketData;
import de.marcely.sbenlib.network.packets.PacketLogin;
import de.marcely.sbenlib.network.packets.PacketLoginReply;
import de.marcely.sbenlib.network.packets.PacketNack;
import de.marcely.sbenlib.network.packets.PacketPing;
import de.marcely.sbenlib.network.packets.PacketPong;

public class PacketDecoder {
	
	public static @Nullable Packet decode(PacketsData packetsData, byte[] data) throws Exception {
		try{
			final byte id = Packet.getTypeOfHeader(data[0]);
			
			switch(id){
			case Packet.TYPE_LOGIN:
				
				final PacketLogin packet_login = new PacketLogin();
				packet_login.decode(data);
				
				return packet_login;
			case Packet.TYPE_LOGIN_REPLY:
				
				final PacketLoginReply packet_loginReply = new PacketLoginReply();
				packet_loginReply.decode(data);
				
				return packet_loginReply;
			case Packet.TYPE_DATA:
				
				final PacketData packet_data = new PacketData();
				packet_data.packetsData = packetsData;
				packet_data.decode(data);
				
				return packet_data;
			case Packet.TYPE_ACK:
				
				final PacketAck packet_ack = new PacketAck();
				packet_ack.decode(data);
				
				return packet_ack;
			case Packet.TYPE_NACK:
				
				final PacketNack packet_nack = new PacketNack();
				packet_nack.decode(data);
				
				return packet_nack;
			case Packet.TYPE_PING:
				
				final PacketPing packet_ping = new PacketPing();
				packet_ping.decode(data);
				
				return packet_ping;
			case Packet.TYPE_PONG:
				
				final PacketPong packet_pong = new PacketPong();
				packet_pong.decode(data);
				
				return packet_pong;
			}
		}catch(Exception e){
			throw e;
		}
		
		return null;
	}
}
