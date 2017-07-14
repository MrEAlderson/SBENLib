package de.marcely.sbenlib.client.protocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

import de.marcely.sbenlib.client.ServerEventListener;
import de.marcely.sbenlib.network.ConnectionInfo;
import de.marcely.sbenlib.network.ProtocolType;

public class UDPProtocol extends Protocol {
	
	private DatagramSocket socket;
	
	public UDPProtocol(ConnectionInfo connInfo, ServerEventListener listener){
		super(connInfo, listener);
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
				
				this.thread = new Thread(){
					public void run(){
						while(running){
							final DatagramPacket packet = new DatagramPacket(new byte[512], 512);
							
							try{
								socket.receive(packet);
								
								listener.onPacketReceive(Arrays.copyOfRange(packet.getData(), packet.getOffset(), packet.getLength()));
							}catch(IOException e){
								e.printStackTrace();
							}
						}
					}
				};
				
				sendPacket(new String("111111111111111111111111111111111115511241421451421").getBytes());
				
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
			
			socket.close();
			
			this.running = false;
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
