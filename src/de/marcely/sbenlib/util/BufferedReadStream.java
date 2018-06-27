package de.marcely.sbenlib.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class BufferedReadStream extends ByteArrayInputStream {

	public BufferedReadStream(byte[] array){
		super(array);
	}
	
	@Override
	public int read(byte[] array){
		// close automaticly if buffer is empty
		if(this.available() == 0){
			try{
				this.close();
				this.finalize();
			}catch(Throwable e){
				e.printStackTrace();
			}
			
			return 0;
		}
		
		// read
		try{
			return super.read(array);
		}catch(IOException e){
			e.printStackTrace();
		}
		
		return 0;
	}
	
	public byte[] read(int length){
		final byte[] array = new byte[length];
		read(array);
		
		return array;
	}
	
	
	/**
	 * 
	 * @return Reads a byte
	 */
	public byte readByte(){
		return read(1)[0];
	}
	
	/**
	 * 
	 * @return Reads a byte array
	 */
	public byte[] readByteArray(){
		final int length = readSignedInt();
			
		return read(length);
	}
	
	/**
	 * 
	 * @return Reads a signed integer (-2147483648 - 2147483648)
	 */
	public int readSignedInt(){
		return ByteBuffer.wrap(read(4)).getInt();
	}
	
	/**
	 * 
	 * @return Reads a unsigned integer (0 - 4294967295)
	 */
	public long readUnsignedInt(){
		return readSignedInt() & 0x00000000ffffffffL;
	}
	
	/**
	 * 
	 * @return Reads a signed short (-128 - 128)
	 */
	public short readSignedShort(){
		return ByteBuffer.wrap(read(2)).getShort();
	}
	
	/**
	 * 
	 * @return Reads a unsigned short (0 - 255)
	 */
	public int readUnsignedShort(){
		return readSignedShort() & 0x00ff;
	}
	
	/**
	 * 
	 * @return Reads a signed float
	 */
	public float readFloat(){
		return ByteBuffer.wrap(read(4)).getFloat();
	}
	
	/**
	 * 
	 * @return Reads a signed double
	 */
	public double readDouble(){
		return ByteBuffer.wrap(read(8)).getDouble();
	}
	
	/**
	 * 
	 * @return Reads a signed long
	 */
	public long readSignedLong(){
		return ByteBuffer.wrap(read(8)).getLong();
	}
	
	/**
	 * 
	 * @return Reads a byte array which will get converted to a string
	 */
	public String readString(){
		final byte[] bytes = readByteArray();
		
		return bytes != null ? new String(bytes) : null;
	}
	
	/**
	 * 
	 * @return Reads a byte and returns it as a boolean
	 */
	public boolean readBoolean(){
		return readByte() == (byte) 1;
	}
}
