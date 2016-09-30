package blue.secure.aes;

import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public final class KeysAES {

	private static KeysAES key128;
	private static KeysAES key256;
	
	static {
		key128 = new KeysAES(128);
		key256 = new KeysAES(256);
	}
	
	public static SecretKey generate128() {
		return key128.generate();
	}

	public static SecretKey generate256() {
		return key256.generate();
	}
	
	// ===============================================

	// static elements
    private KeyGenerator keyGen;
    
    private KeysAES(int keySize){
		try {
	    	keyGen = KeyGenerator.getInstance("AES");
	    	keyGen.init(keySize);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
    }
    
    private SecretKey generate(){
    	 return keyGen.generateKey();
    }
}
