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
import de.marcely.sbenlib.util.SThread.ThreadType;
import de.marcely.sbenlib.util.TickTimer;
import de.marcely.sbenlib.util.SThread;

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
				this.thread = new SThread(ThreadType.Protocol_TCP_Server){
					protected void _run(){
						while(running){
							try{
								final Socket client = socket.accept();
								
								// packet updater
								final TickTimer t = new TickTimer(true, 100, 100){
									public void onRun(){
										// continue
										final Session session = listener.getSession(client.getInetAddress(), client.getPort());
										if(session == null)
											return;
										
										try{
											final InputStream inStream = client.getInputStream();
											
											if(inStream.available() >= 1){
												final byte[] packet = new byte[inStream.available()];
												inStream.read(packet);
												
												listener.onPacketReceive(session, packet);
											}
										}catch(IOException e){
											final String reason = e.getMessage();
											
											if(reason != null && (reason.equals("socket closed") || reason.equals("Stream closed.")) || reason.equals("Socket is closed")){
												stop();
												session.close();
											}
											
											e.printStackTrace();
										}
									}
								};
								t.start();
								
								// register session
								final Session session = new Session(server, client.getInetAddress(), client.getPort(), thread, client.getOutputStream(), t);
								
								listener.onClientRequest(session);
								
							}catch(IOException e){
								final String reason = e.getMessage();
								
								if(reason != null && (reason.equals("socket closed")))
									return;
								
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
				this.running = false;
				this.socket.close();
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
				((OutputStream) session.getObj()[0]).write(packet);
			}catch(IOException e){
				final String msg = e.getMessage();
				
				if(msg != null && (msg.equals("Connection reset by peer: socket write error"))){
					this.server.close();
					return false;
				}else if(msg == null || !(msg.equals("Software caused connection abort: socket write error")))
					e.printStackTrace();
				
				return false;
			}
			
			return true;
		}else
			return false;
	}

	@Override
	protected boolean _closeSession(Session session){
		((TickTimer) session.getObj()[1]).stop();
		
		if(isRunning() && session.isConnected()){
			try{
				((OutputStream) session.getObj()[0]).close();
			}catch(IOException e){
				e.printStackTrace();
				return false;
			}
			
			return true;
		}else
			return false;
	}
}