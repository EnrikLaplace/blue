package blue.secure.rsa;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import blue.secure.Crypt;

/**
 * secure asymmetric 
 * 
 * @author Matteo
 *
 */
public class SecureRSA implements Crypt{
	
	// =========================================
	
	private static KeyPair generateKeys(){
		try {
			return KeyPairGenerator.getInstance("RSA").generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	// =========================================

	private Cipher cipherEnc;
	private Cipher cipherDec;
	private KeyPair keys;
	private PublicKey key;
	
	// =========================================
	
	public SecureRSA() {
		this.keys = generateKeys();
		init();
		this.setupPublicKey(this.keys.getPublic());
	}
	
	public SecureRSA(PublicKey key) {
		this.keys = generateKeys();
		init();
		this.setupPublicKey(key);
	}
	
	// =========================================

	private void init(){
		try {
			cipherDec = Cipher.getInstance("RSA");
			cipherDec.init(Cipher.DECRYPT_MODE, keys.getPrivate());
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (NoSuchPaddingException e) {
			throw new RuntimeException(e);
		} catch (InvalidKeyException e) {
			throw new RuntimeException(e);
		}
	}
	
	// ==================================================
	
	public byte[] encrypt(byte[] data) {
		try {
			return cipherEnc.doFinal(data);
		} catch (IllegalBlockSizeException e) {
			throw new RuntimeException(e);
		} catch (BadPaddingException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	public byte[] decrypt(byte[] data) {
		try {
			return cipherDec.doFinal(data);
		} catch (IllegalBlockSizeException e) {
			throw new RuntimeException(e);
		} catch (BadPaddingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void setupPublicKey(byte[] key){
		try {
			setupPublicKey(KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(key)));
		} catch (InvalidKeySpecException e) {
			throw new RuntimeException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void setupPublicKey(PublicKey key){
		try {
			cipherEnc = Cipher.getInstance("RSA");
			cipherEnc.init(Cipher.ENCRYPT_MODE, key);
			this.key = key;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (NoSuchPaddingException e) {
			throw new RuntimeException(e);
		} catch (InvalidKeyException e) {
			throw new RuntimeException(e);
		}
	}

	public PublicKey getPublicKey(){
		return keys.getPublic();
	}
	
	public PrivateKey getPrivateKey() {
		return keys.getPrivate();
	}
	
	public PublicKey getKey() {
		return key;
	}
//	
//	
//	
//	
//	
//	public static void main(String[] args) {
//		SecureRSA secure1 = new SecureRSA();
//		SecureRSA secure2 = new SecureRSA(secure1.getPublicKey());
//		secure1.setupPublicKey(secure2.getPublicKey());
//		System.out.println(new String(secure1.decrypt(secure2.encrypt("blagggggggggggggggggggggggggggggggggggggggggggggdg fdgd gdfg fdgdfg dggggga".getBytes()))));
//		System.out.println(new String(secure2.decrypt(secure1.encrypt("blagggggggggggggggggggggggggggggggggggggggggggggdg fdgd gdfg fdgdfg dggggga".getBytes()))));
//	}
}
