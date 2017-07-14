package de.marcely.sbenlib.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class BufferedWriteStream extends ByteArrayOutputStream {
	
	@Override
	public void write(byte[] array){
		try{
			super.write(array);
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void writeByte(byte b){
		final byte[] array = new byte[1];
		array[0] = b;
		
		write(array);
	}
	
	public void writeByteArray(byte[] b){
		writeSignedInt(b.length);
		write(b);
	}
	
	public void writeSignedInt(int i){
		write(ByteBuffer.allocate(4).putInt(i).array());
	}
	
	public void writeUnsignedInt(long i){
		writeSignedInt((int) ((long) i & 0x7FFFFFFF));
	}
	
	public void writeSignedShort(short s){
		write(ByteBuffer.allocate(2).putShort(s).array());
	}
	
	public void writeUnsignedShort(int s){
		writeSignedInt(s & 0x3FFF);
	}
	
	public void writeSignedFloat(float f){
		write(ByteBuffer.allocate(4).putFloat(f).array());
	}
	
	public void writeSignedDouble(double d){
		write(ByteBuffer.allocate(8).putDouble(d).array());
	}
	
	public void writeSignedLong(long l){
		write(ByteBuffer.allocate(8).putLong(l).array());
	}
	
	public void writeString(String s){
		writeByteArray(s.getBytes());
	}
	
	public void writeBoolean(boolean b){
		writeByte(b == true ? (byte) 1 : (byte) 0);
	}
}
