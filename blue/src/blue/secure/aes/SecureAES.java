package blue.secure.aes;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import blue.secure.Crypt;

/**
 * basic secure simmetric
 * @author Matteo
 *
 */
public class SecureAES implements Crypt {
	
    // ============================================
    
    private Cipher cipherEnc;
	private Cipher cipherDec;
	private byte[] key;
	private byte[] rndIv;
    
    // ============================================
    
    public SecureAES(){
    	this.key = KeysAES.generate256().getEncoded();
    	this.rndIv = generateIV(32);
    	try{
        	init();
    	}catch(InvalidKeyException e){
        	this.key = KeysAES.generate128().getEncoded();
        	this.rndIv = generateIV(16);
        	try {
				init();
			} catch (InvalidKeyException e1) {
				throw new RuntimeException(e);
			}
    	}
    }
    
    public SecureAES(byte[] key, byte[] iv){
    	this.key = key;
    	this.rndIv = iv;
    	try {
			init();
		} catch (InvalidKeyException e) {
			throw new RuntimeException(e);
		}
    }
    
    // ============================================
    
    private void init() throws InvalidKeyException{
        try {
        	SecretKeySpec secretSpec = new SecretKeySpec(key, "AES");
        	IvParameterSpec ivParam = new IvParameterSpec(rndIv);
	        
	        // init cipher enc
	        cipherEnc = Cipher.getInstance("AES/CBC/PKCS5Padding");
	        cipherEnc.init(Cipher.ENCRYPT_MODE, secretSpec, ivParam);
	        
	        // init decrypt
	        cipherDec = Cipher.getInstance("AES/CBC/PKCS5Padding");
	        cipherDec.init(Cipher.DECRYPT_MODE, secretSpec, ivParam);
	        
		}catch(InvalidKeyException ex){
			throw ex;
		}
        catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
    
    private byte[] generateIV(int blockSize){
    	SecureRandom randomSecureRandom;
		try {
			randomSecureRandom = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
    	byte[] iv = new byte[blockSize];
    	randomSecureRandom.nextBytes(iv);
    	return iv;
    }

    // ===========================================================
    
    public byte[] encryptUTF8(String text){
    	try {
			return encrypt(String.valueOf(text).getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
    }
    
    public byte[] encrypt(byte[] data){
        try {
			return cipherEnc.doFinal(data);
		} catch (IllegalBlockSizeException e) {
			throw new RuntimeException(e);
		} catch (BadPaddingException e) {
			throw new RuntimeException(e);
		}
    }
    
    // ----------------------------------

    public byte[] decrypt(byte[] data){
        try {
			return cipherDec.doFinal(data);
		} catch (IllegalBlockSizeException e) {
			throw new RuntimeException(e);
		} catch (BadPaddingException e) {
			throw new RuntimeException(e);
		}
    }

    public byte[] getKey() {
		return key;
	}
    
    public byte[] getIV() {
		return rndIv;
	}
    
    public boolean setup(byte[] key, byte[] iv){
    	this.key = key;
    	this.rndIv = iv;
    	try {
			init();
			return true;
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			return false;
		}
    }
    
    
    
    
    
    
    
    
    public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidParameterSpecException {
    	SecureAES sec1 = new SecureAES();
		SecureAES sec2 = new SecureAES();
		sec2.setup(sec1.getKey(), sec1.getIV());
		
    	
		System.out.println(new String(sec2.decrypt(sec1.encrypt("asdasd asd sad sa das ".getBytes()))));
    	
    	
    	
    	
	}
}
