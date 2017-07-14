package de.marcely.sbenlib.compression;

import java.nio.charset.StandardCharsets;

import javax.xml.bind.DatatypeConverter;

public class Base64 {
	
	public static byte[] encode(byte[] data){
		return DatatypeConverter.printBase64Binary(data).getBytes(StandardCharsets.UTF_8);
	}
	
	public static byte[] decode(byte[] data){
		return DatatypeConverter.parseBase64Binary(new String(data, StandardCharsets.UTF_8));
	}
	
	/*public static byte[] encode(byte[] data, int bitSize){
		// convert array to bits
		final boolean[] bits = new boolean[data.length*bitSize];
		
		int index = 0;
		for(byte b:data){
			final String byteBitArray = Integer.toBinaryString(b & 0xFF); 
			
			for(int i=0; i<bitSize; i++)
				bits[index*bitSize + i] = byteBitArray.charAt(i) == '1';
		}
		
		// put new bits in to array
		int bitsRequiredPerByte = MathUtil.getSubtractable(bits.length, 6);
		final boolean[] newBits = new boolean[bitsRequiredPerByte * data.length];
		
		index = 0;
		while(index < data.length){
			int i;
			for(i=0; i<bitSize; i++){
				final int slot = index*bitSize + i;
				
				newBits[slot] = bits[slot];
			}
			
			for(i=bitSize; i<bitsRequiredPerByte; i++){
				final int slot = index*bitSize + i;
				
				newBits[slot] = false;
			}
			
			index++;
		}
		
		final byte[] buffer = new byte[newBits.length/6];
		for(int i=0; i<newBits.length/6; i++){
			final int slot = i*6;
			String binrary = "";
			
			for(int bit=0; bit<6; bit++)
				binrary += newBits[slot+bit] == true ? "1" : "0";
			
			buffer[i] = (byte) Integer.parseInt(binrary, 2);
		}
		
		System.out.println(new String(buffer));
		
		// convert bits to bytes
		
		
		return data;
	}*/
}
