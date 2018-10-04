package de.marcely.sbenlib.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import lombok.Getter;

public class ZLibCompression implements Compressor {
	
	@Getter public static ZLibCompression instance = new ZLibCompression();
	
	@Override
	public CompressionType getType(){
		return CompressionType.ZLib;
	}

	@Override
	public byte[] encode(byte[] data) throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final DeflaterOutputStream dos = new DeflaterOutputStream(baos);
        
        dos.write(data);
        dos.flush();
        dos.close();
        
        return baos.toByteArray();
	}

	@Override
	public byte[] decode(byte[] data) throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final InputStream is = new InflaterInputStream(new ByteArrayInputStream(data));
		
        int d = -1;
        
        while((d = is.read()) != -1)
        	baos.write(d);
        
        is.close();
        
		return baos.toByteArray();
	}
}
