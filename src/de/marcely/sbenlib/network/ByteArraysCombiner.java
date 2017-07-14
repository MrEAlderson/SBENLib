package de.marcely.sbenlib.network;

import java.util.ArrayList;
import java.util.List;

public class ByteArraysCombiner {
	
	private final byte splitByte;
	
	private List<Byte> bytes = new ArrayList<Byte>();
	
	public ByteArraysCombiner(byte splitByte){
		this.splitByte = splitByte;
	}
	
	public List<byte[]> addBytes(byte[] data){
		final List<byte[]> datas = new ArrayList<byte[]>();
		
		for(byte b:data){
			if(b != splitByte)
				bytes.add(b);
			else{
				if(bytes.size() == 0)
					continue;
				
				final byte[] bytes1 = new byte[bytes.size()];
				
				int i=0;
				for(Byte b1:bytes)
					bytes1[i++] = b1;
				
				datas.add(bytes1);
				bytes.clear();
			}
		}
		
		return datas;
	}
}
