package de.marcely.sbenlib.util;

import de.marcely.sbenlib.client.SBENServerConnection;
import de.marcely.sbenlib.compression.CompressionType;
import de.marcely.sbenlib.network.ConnectionInfo;
import de.marcely.sbenlib.network.ConnectionState;
import de.marcely.sbenlib.network.ProtocolType;
import de.marcely.sbenlib.network.packets.PacketData;
import de.marcely.sbenlib.server.SBENServer;

public class Test {
	
	public static void main(String[] args){
		final ConnectionInfo connInfo = new ConnectionInfo("192.168.178.59", 6234, ProtocolType.UDP, CompressionType.ZLib);
		
		SBENServer server = new SBENServer(connInfo, 1);
		System.out.println(server.run());
		SBENServerConnection client = new SBENServerConnection(connInfo){
			public void onStateChange(ConnectionState state){
				System.out.println(state);
			}

			public void onPacketReceive(PacketData packet){
				
			}

			public void onDisconnect(String reason){
				System.out.println("disconnect: " + reason);
			}
		};
		System.out.println(client.run());
		
		Util.sleep(500*5);
		
		System.out.println("END");
	}
}
