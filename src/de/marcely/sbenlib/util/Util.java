package de.marcely.sbenlib.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

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
}
