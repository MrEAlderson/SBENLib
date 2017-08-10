package de.marcely.sbenlib.server.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import de.marcely.sbenlib.network.ConnectionInfo;
import de.marcely.sbenlib.network.ProtocolType;
import de.marcely.sbenlib.server.SBENServer;
import de.marcely.sbenlib.server.ServerEventListener;
import de.marcely.sbenlib.server.ServerStartInfo;
import de.marcely.sbenlib.server.Session;

public class TCPProtocol extends Protocol {
	
	private ServerSocket socket;
	
	public TCPProtocol(ConnectionInfo conn, SBENServer server, ServerEventListener listener, int maxClients){
		super(conn, server, listener, maxClients);
	}
	
	@Override
	public ProtocolType getType(){
		return ProtocolType.TCP;
	}

	@Override
	protected ServerStartInfo _run(){
		if(!running){
			try{
				
				this.socket = new ServerSocket(connectionInfo.PORT, maxClients, connectionInfo.IP);
				
				// server client
				this.thread = new Thread(){
					public void run(){
						while(running){
							try{
								final Socket client = socket.accept();
								
								final Thread thread = new Thread(){
									public void run(){
										final Session session = listener.getSession(client.getInetAddress(), client.getPort());
										
										try{
											final InputStream inStream = client.getInputStream();
											
											while(running){
												if(inStream.available() >= 1){
													final byte[] packet = new byte[inStream.available()];
													inStream.read(packet);
													
													listener.onPacketReceive(session, packet);
												}
											}
										}catch(IOException e){
											final String reason = e.getMessage();
											
											if(reason != null && (reason.equals("socket closed") || reason.equals("Stream closed.")))
												return;
											
											e.printStackTrace();
										}
									}
								};
								
								final Session session = new Session(server, client.getInetAddress(), client.getPort(), thread, client.getOutputStream());
								
								listener.onClientRequest(session);
								thread.start();
								
							}catch(IOException e){
								final String reason = e.getMessage();
								
								if(reason != null){
									if(reason.equals("socket closed")){
										server.getSocketHandler().close();
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
			}catch(IOException e){
				return ServerStartInfo.FAILED_UNKOWN;
			}
			
		}else
			return ServerStartInfo.FAILED_ALREADYRUNNING;
	}

	@Override
	public boolean close(){
		if(running){
			
			try{
				this.socket.close();
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
	protected boolean _sendPacket(Session session, byte[] packet){
		if(running){
			try{
				((OutputStream) session.getObj()).write(packet);
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
		if(isRunning() && session.isConnected()){
			try{
				((OutputStream) session.getObj()).close();
			}catch(IOException e){
				e.printStackTrace();
				return false;
			}
			
			return true;
		}else
			return false;
	}
}