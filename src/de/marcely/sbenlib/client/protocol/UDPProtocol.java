package de.marcely.sbenlib.client.protocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

import de.marcely.sbenlib.client.ServerEventListener;
import de.marcely.sbenlib.client.SocketHandler;
import de.marcely.sbenlib.network.ConnectionInfo;
import de.marcely.sbenlib.network.ProtocolType;
import de.marcely.sbenlib.util.SThread;
import de.marcely.sbenlib.util.SThread.ThreadType;

public class UDPProtocol extends Protocol {
	
	private DatagramSocket socket;
	
	public UDPProtocol(ConnectionInfo connInfo, SocketHandler socketHandler, ServerEventListener listener){
		super(connInfo, socketHandler, listener);
	}

	@Override
	public ProtocolType getType(){
		return ProtocolType.UDP;
	}

	@Override
	public boolean run(){
		if(!running){
			this.running = true;
			
			try{
				socket = new DatagramSocket();
				
				this.thread = new SThread(ThreadType.Protocol_UDP_Client){
					protected void _run(){
						while(running){
							final DatagramPacket packet = new DatagramPacket(new byte[512], 512);
							
							try{
								socket.receive(packet);
								
								if(!packet.getAddress().equals(connectionInfo.IP) ||
								    packet.getPort() != connectionInfo.PORT) return;
								
								listener.onPacketReceive(Arrays.copyOfRange(packet.getData(), packet.getOffset(), packet.getLength()));
							}catch(IOException e){
								final String reason = e.getMessage();
								
								if(reason != null){
									if(reason.equals("socket closed")){
										socketHandler.close();
										return;
									}
								}
								
								e.printStackTrace();
							}
						}
					}
				};
				this.thread.start();
				
			}catch(SocketException e){
				e.printStackTrace();
				return false;
			}
			
			return true;
		}else
			return false;
	}

	@Override
	public boolean close(){
		if(running){
			this.running = false;
			
			socket.close();
			
			return true;
		}else
			return false;
	}

	@Override
	protected boolean _sendPacket(byte[] packet){
		if(running){
			
			try{
				socket.send(new DatagramPacket(packet, packet.length, connectionInfo.IP, connectionInfo.PORT));
			
			}catch(IOException e){
				e.printStackTrace();
				return false;
			}
			
			return true;
		}else
			return false;
	}
}
