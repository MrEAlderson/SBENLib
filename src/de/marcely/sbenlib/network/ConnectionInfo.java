package de.marcely.sbenlib.network;

import java.net.InetAddress;

import de.marcely.sbenlib.compression.CompressionType;
import de.marcely.sbenlib.util.Util;

public class ConnectionInfo {
	
	public final InetAddress IP;
	public final int PORT;
	public final ProtocolType PROTOCOL;
	public final CompressionType COMPRESSION;
	
	public ConnectionInfo(String ip, int port){
		this(Util.getInetAddress(ip), port);
	}
	
	public ConnectionInfo(InetAddress ip, int port){
		this(ip, port, ProtocolType.TCP, CompressionType.ZLib);
	}
	
	public ConnectionInfo(String ip, int port, ProtocolType protocol, CompressionType compression){
		this(Util.getInetAddress(ip), port, protocol, compression);
	}
	
	public ConnectionInfo(InetAddress ip, int port, ProtocolType protocol, CompressionType compression){
		this.IP = ip;
		this.PORT = port;
		this.PROTOCOL = protocol;
		this.COMPRESSION = compression;
	}
}
