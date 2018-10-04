package de.marcely.sbenlib.network;

import java.net.InetAddress;

import de.marcely.sbenlib.compression.CompressionType;
import de.marcely.sbenlib.util.Util;

public class ConnectionInfo {
	
	public final InetAddress ip;
	public final int port;
	public final ProtocolType protocol;
	public final CompressionType compression;
	
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
		this.ip = ip;
		this.port = port;
		this.protocol = protocol;
		this.compression = compression;
	}
}
