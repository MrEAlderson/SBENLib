package de.marcely.sbenlib.test;

import de.marcely.sbenlib.client.SBENServerConnection;
import de.marcely.sbenlib.compression.CompressionType;
import de.marcely.sbenlib.network.ConnectionInfo;
import de.marcely.sbenlib.network.ConnectionState;
import de.marcely.sbenlib.network.PacketsData;
import de.marcely.sbenlib.network.ProtocolType;
import de.marcely.sbenlib.network.packets.data.DataPacket;
import de.marcely.sbenlib.server.SBENServer;
import de.marcely.sbenlib.server.Session;
import de.marcely.sbenlib.server.SessionEventListener;
import de.marcely.sbenlib.util.Util;

public class Test {
	
	public static void main(String[] args){
		final ConnectionInfo connInfo = new ConnectionInfo("192.168.178.59", 6234, ProtocolType.TCP, CompressionType.ZLib);
		final PacketsData packets = new PacketsData();
		
		// register packets
		packets.addPacket(new TestNormalPacket());
		packets.addPacket(new TestSecuredPacket());
		
		// create server
		SBENServer server = new SBENServer(connInfo, 1){
			public void onSessionRequest(final Session session){
				session.registerListener(new SessionEventListener(){
					public void onStateChange(ConnectionState state){
						if(state == ConnectionState.Connected){
							final TestNormalPacket packet = new TestNormalPacket();
							
							session.sendPacket(packet);
						}
					}
					
					public void onPacketReceive(DataPacket dataPacket){
						System.out.println("received packet as server");
					}
					
					public void onDisconnect(String reason){
						
					}
				});
			}
		};
		server.setPacketsData(packets);
		server.run();
		
		// create client
		final SBENServerConnection client = new SBENServerConnection(connInfo){
			public void onStateChange(ConnectionState state){
				if(state == ConnectionState.Connected){
					final TestNormalPacket packet = new TestNormalPacket();
					
					sendPacket(packet);
				}
			}

			public void onPacketReceive(DataPacket packet){
				System.out.println("received packet as client");
			}

			public void onDisconnect(String reason){
				System.out.println("client disconnected because: " + reason);
			}
		};
		client.setPacketsData(packets);
		client.run();
		
		Util.sleep(500*5);
		
		System.out.println("END");
		server.close();
	}
}
