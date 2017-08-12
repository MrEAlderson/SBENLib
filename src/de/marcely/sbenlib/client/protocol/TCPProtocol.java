package de.marcely.sbenlib.client.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import de.marcely.sbenlib.network.ConnectionInfo;
import de.marcely.sbenlib.network.ProtocolType;
import de.marcely.sbenlib.client.ServerEventListener;
import de.marcely.sbenlib.client.SocketHandler;

public class TCPProtocol extends Protocol {
	
	private Socket socket;
	
	public TCPProtocol(ConnectionInfo connInfo, SocketHandler socketHandler, ServerEventListener listener){
		super(connInfo, socketHandler, listener);
	}

	@Override
	public ProtocolType getType(){
		return ProtocolType.TCP;
	}

	@Override
	public boolean run(){
		if(!running){
			
			try{
				socket = new Socket(connectionInfo.IP, connectionInfo.PORT);
				
				this.thread = new Thread(){
					public void run(){
						try{
							final InputStream inStream = socket.getInputStream();
							
							while(running){
								if(inStream.available() >= 1){
									final byte[] packet = new byte[inStream.available()];
									inStream.read(packet);
									
									listener.onPacketReceive(packet);
								}
							}
						}catch(IOException e){
							final String msg = e.getMessage();
							
							if(msg != null && (msg.equals("Stream closed.")))
								return;
							
							e.printStackTrace();
						}
					}
				};
				this.thread.start();
				
			}catch(IOException e){
				final String msg = e.getMessage();
				
				if(msg != null && (msg.equals("Connection refused: connect")))
					return false;
				
				e.printStackTrace();
				
				return false;
			}
			
			this.running = true;
			return true;
		}else
			return false;
	}

	@Override
	public boolean close(){
		if(running){
			
			try{
				socket.close();
				this.running = false;
			}catch(IOException e){
				e.printStackTrace();
				return false;
			}
			
			return true;
		}else
			return false;
	}

	@Override
	protected boolean _sendPacket(byte[] packet){
		if(running){
			
			try{
				socket.getOutputStream().write(packet);
				
				return true;
			}catch(IOException e){
				final String msg = e.getMessage();
				
				if(msg != null && (msg.equals("Connection reset by peer: socket write error")))
					close();
				else
					e.printStackTrace();
				
				return false;
			}
			
		}else
			return false;
	}
}
