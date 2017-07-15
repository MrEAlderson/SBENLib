package de.marcely.sbenlib.server.protocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Arrays;

import de.marcely.sbenlib.network.ConnectionInfo;
import de.marcely.sbenlib.network.ProtocolType;
import de.marcely.sbenlib.server.SBENServer;
import de.marcely.sbenlib.server.ServerEventListener;
import de.marcely.sbenlib.server.ServerStartInfo;
import de.marcely.sbenlib.server.Session;

public class UDPProtocol extends Protocol {
	
	private DatagramSocket socket;
	
	public UDPProtocol(ConnectionInfo conn, SBENServer server, ServerEventListener listener, int maxClients){
		super(conn, server, listener, maxClients);
	}
	
	@Override
	public ProtocolType getType(){
		return ProtocolType.UDP;
	}
	
	@Override
	protected ServerStartInfo _run(){
		if(!running){
			try{
				
				socket = new DatagramSocket(new InetSocketAddress(connectionInfo.IP, connectionInfo.PORT));
				
				this.thread = new Thread(){
					public void run(){
						while(running){
							try{
								final DatagramPacket packet = new DatagramPacket(new byte[512], 512);
								
								socket.receive(packet);
								
								Session session = listener.getSession(packet.getAddress(), packet.getPort());
								
								if(session == null){
									session = new Session(server, packet.getAddress(), packet.getPort());
									listener.onClientRequest(session);
								}
								
								listener.onPacketReceive(session, Arrays.copyOfRange(packet.getData(), packet.getOffset(), packet.getLength()));
								
							}catch(IOException e){
								final String reason = e.getMessage();
								
								if(reason != null){
									if(reason.equals("socket closed")){
										return;
									}
								}
								
								e.printStackTrace();
							}
						}
					}
				};
				this.thread.start();
				
				this.running = true;
				return ServerStartInfo.SUCCESS;
			}catch(SocketException e){
				e.printStackTrace();
				return ServerStartInfo.FAILED_UNKOWN;
			}
		}else
			return ServerStartInfo.FAILED_ALREADYRUNNING;
	}

	@Override
	public boolean close(){
		if(running){
			
			socket.close();
			running = false;
			
			return true;
		}else
			return false;
	}

	@Override
	protected boolean _sendPacket(Session session, byte[] packet){
		if(running){
			try{
				socket.send(new DatagramPacket(packet, packet.length, session.getAddress(), session.getPort()));
			}catch(IOException e){
				e.printStackTrace();
				return false;
			}
			
			return true;
		}else
			return false;
	}

	@Override
	protected boolean _closeSession(Session session){
		return isRunning() && session.isConnected();
	}
}
