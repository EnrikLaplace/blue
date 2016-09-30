package blue.secure;

public interface Crypt {
	byte[] encrypt(byte[] data);
	byte[] decrypt(byte[] data);
}
