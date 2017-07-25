package de.marcely.sbenlib.compression;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.annotation.Nullable;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class AES {
	
	public static @Nullable byte[] encrypt(byte[] data, SecretKeySpec key){
		try{
			final Cipher cipher = Cipher.getInstance("AES");
			
			cipher.init(Cipher.ENCRYPT_MODE, key);
			return cipher.doFinal(data);
		}catch(NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e){
			e.printStackTrace();
			return null;
		}
	}
	
	public static @Nullable byte[] decrypt(byte[] data, SecretKeySpec key){
		try{
			final Cipher cipher = Cipher.getInstance("AES");
			
			cipher.init(Cipher.DECRYPT_MODE, key);
			return cipher.doFinal(data);
		}catch(NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e){
			e.printStackTrace();
			return null;
		}
	}
}
