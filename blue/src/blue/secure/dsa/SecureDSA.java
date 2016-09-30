package blue.secure.dsa;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class SecureDSA {
	
	private static Signature dsaCheck;
	
	public static PublicKey generatePublicKey(byte[] enc){
		try {
			return KeyFactory.getInstance("DSA").generatePublic(new X509EncodedKeySpec(enc));
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
			return null;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static synchronized boolean check(PublicKey key, InputStream str, byte[] sign){
		try {
			if(dsaCheck == null){
				dsaCheck = Signature.getInstance("SHA1withDSA", "SUN");
			}
			dsaCheck.initVerify(key);
			byte[] buffer = new byte[1024];
			int len;
			while (str.available() != 0) {
			    len = str.read(buffer);
			    dsaCheck.update(buffer, 0, len);
			};
			return dsaCheck.verify(sign);
		} catch (SignatureException e) {
			throw new RuntimeException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (NoSuchProviderException e) {
			throw new RuntimeException(e);
		} catch (InvalidKeyException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static synchronized boolean check(PublicKey key, byte[] data, byte[] sign){
		try {
			if(dsaCheck == null){
				dsaCheck = Signature.getInstance("SHA1withDSA", "SUN");
			}
			dsaCheck.initVerify(key);
			dsaCheck.update(data);
			return dsaCheck.verify(sign);
		} catch (SignatureException e) {
			throw new RuntimeException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (NoSuchProviderException e) {
			throw new RuntimeException(e);
		} catch (InvalidKeyException e) {
			throw new RuntimeException(e);
		}
	}
	
	// =================================================

	private KeyPairGenerator keyGen;
	private SecureRandom rnd;
	private Signature dsa;
	private KeyPair keys;
	
	// =================================================
	
	public SecureDSA(){
		try {
			keyGen = KeyPairGenerator.getInstance("DSA", "SUN");
			rnd = SecureRandom.getInstance("SHA1PRNG", "SUN");
			init();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (NoSuchProviderException e) {
			throw new RuntimeException(e);
		} catch (InvalidKeyException e) {
			throw new RuntimeException(e);
		}
	}
	
	// =================================================
	
	private void init() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException{
        keyGen.initialize(1024, rnd);
        keys = keyGen.generateKeyPair();
        
		dsa = Signature.getInstance("SHA1withDSA", "SUN");
		dsa.initSign(keys.getPrivate());
	}
	
	// =================================================
	
	public PublicKey getPublicKey(){
		return keys.getPublic();
	}
	
	public PrivateKey getPrivateKey(){
		return keys.getPrivate();
	}
	
	public synchronized byte[] sign(byte[] data){
		try {
			dsa.update(data);
			return dsa.sign();
		} catch (SignatureException e) {
			e.printStackTrace();
			return null;
		}
	}
	
//	
//	
//	public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException {
//		SecureDSA sec = new SecureDSA();
//		byte[] signed = sec.sign("ciao".getBytes());
//		byte[] publ = sec.getPublicKey().getEncoded();
//		
//		
//		System.out.println(SecureDSA.check(SecureDSA.generatePublicKey(publ), "ciao".getBytes(), signed));
//	}
}
