package de.marcely.sbenlib.compression;

import de.marcely.sbenlib.util.MathUtil;

public class Base64 {
	public static byte[] encode(byte[] data){
		return encode(data, Integer.toBinaryString(data[0] & 0xFF).length());
	}
	
	public static byte[] encode(byte[] data, int bitSize){
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
		}
		
		// convert bits to bytes
		
		
		return data;
	}
	
	public static byte[] decode(byte[] data, int bitSize){
		return data;
	}
}
