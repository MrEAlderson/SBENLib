package de.marcely.sbenlib.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

import de.marcely.sbenlib.network.Network;

public class Util {
	
	public static final Random RAND = new Random();
	
	public static InetAddress getInetAddress(String ip){
		try{
			return InetAddress.getByName(ip);
		}catch(UnknownHostException e){
			e.printStackTrace();
			return null;
		}
	}
	
	public static void sleep(long millis){
		try{
			Thread.sleep(millis);
		}catch(InterruptedException e){
			e.printStackTrace();
		}
	}
	
	public static byte[] generateRandomSecurityID(){
		final byte[] bytes = new byte[Network.SECURITYID_LENGTH];
		
		for(int i=0; i<bytes.length; i++)
			bytes[i] = (byte) RAND.nextInt(255);
		
		return bytes;
	}
}
