package de.marcely.sbenlib.util;

import de.marcely.sbenlib.compression.Base64;

public class Test {
	
	public static void main(String[] args){
		final byte[] b = new byte[1];
		b[0] = new String("L").getBytes()[0];
		
		Base64.encode(b);
		/*final ConnectionInfo connInfo = new ConnectionInfo("192.168.178.59", 6234, ProtocolType.UDP, CompressionType.ZLib);
		
		SBENServer server = new SBENServer(connInfo, 1);
		System.out.println(server.run());
		SBENServerConnection client = new SBENServerConnection(connInfo);
		System.out.println(client.run());*/
	}
}
