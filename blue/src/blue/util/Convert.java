package blue.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class Convert {
	
	/**
	 * string to SHORT
	 * 
	 * @param by
	 * @return
	 */
	public static int toInt16(String num){
		return Short.parseShort(num);
	}
	
	/**
	 * byte array to SHORT
	 * 
	 * @param by
	 * @return
	 */
	public static short toInt16(byte[] bytes){
		assert bytes.length == 2;
		return ByteBuffer.wrap(bytes).getShort();
	}

	/**
	 * string to INTEGER
	 * 
	 * @param by
	 * @return
	 */
	public static int toInt32(String num){
		return Integer.parseInt(num);
	}
	
	/**
	 * byte array to INTEGER
	 * 
	 * @param by
	 * @return
	 */
	public static int toInt32(byte[] bytes){
		assert bytes.length == 4;
		return ByteBuffer.wrap(bytes).getInt();
	}

	
	/**
	 * string to LONG
	 * 
	 * @param by
	 * @return
	 */
	public static long toInt64(String num){
		return Long.parseLong(num);
	}
	
	/**
	 * byte array To LONG
	 * 
	 * @param by
	 * @return
	 */
	public static long toInt64(byte[] bytes){
		assert bytes.length == 8;
		return ByteBuffer.wrap(bytes).getLong();
	}

	/**
	 * Convert bytes to HEX format
	 * 
	 * @param data
	 * @return
	 */
	public static String toHex(byte[] bytes){
		  String result = "";
		  for (int i=0; i < bytes.length; i++) {
		    result +=
		          Integer.toString( ( bytes[i] & 0xff ) + 0x100, 16).substring( 1 );
		  }
		  return result;
	}
	
	/**
	 * Convert bytes to SHA1 bytes
	 * 
	 * @param data
	 * @return
	 */
	public static byte[] toSHA1(byte[] data){
		 try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			return md.digest(data);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Convert string to byte array
	 * 
	 * @param string
	 * @param charset
	 * @return
	 */
	public static byte[] toBytes(String string, Charset charset){
		return charset.encode(string).array();
	}
	
	/**
	 * Convert string to byte array
	 * 
	 * @param string
	 * @param charset
	 * @return
	 */
	public static byte[] toBytes(String string, CharEncoding enc){
		try {
			return string.getBytes(enc.toString());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Convert string to byte array with default charset (ASCII7)
	 * 
	 * @param string
	 * @param charset
	 * @return
	 */
	public static byte[] toBytes(String string){
		return toBytes(string, Utils.charsetDef());
	}
	

//	public static byte[] toByte(char c, Charset charset){
//		
//	}
	
	/**
	 * Converts string to stream of bytes
	 * 
	 * @param string
	 * @return
	 */
	public static InputStream toByteStream(String string){
		return toStream(toBytes(string));
	}

	/**
	 * Converts byte array to byte stream
	 * @param file
	 * @return
	 */
	public static InputStream toStream(byte[] file) {
		return new ByteArrayInputStream(file);
	}


	/**
	 * Converts byte array to byte stream
	 * @param file
	 * @return
	 * @throws FileNotFoundException 
	 */
	public static InputStream toStream(File file) throws FileNotFoundException {
		return new FileInputStream(file);
	}
	
	/**
	 * Convert boolean array to byte representation
	 * 
	 * @param arr
	 * @return
	 */
	public static byte toByte(boolean[] arr)
	{
		assert arr.length <= 8;
		byte val = 0;
		for(boolean b :arr){
		     val <<= 1;
		     if (b) val |= 1;
		}
		return val;
	}

	/**
	 * Convert boolean array to byte representation
	 * 
	 * @param arr
	 * @return
	 */
	public static byte[] toBytes(boolean[] arr){
		byte[] bytes = new byte[(int) Math.ceil(((double)arr.length) / 8d)];
	    for (int i = 0; i < bytes.length; i++) {
	    	bytes[i] = toByte(ArrayUtils.copyOfRange(arr, i*8, i*8+Math.min(8,arr.length-i*8)));
	    }
	    return bytes;
	}
	
	/**
	 * Converts short to bytes (BE)
	 * 
	 * @param s
	 * @return
	 */
	public static byte[] toBytes(short n){
		byte[] ret = new byte[2];
		ret[0] = (byte) ((n & 0x0000FF00) >> 8);
		ret[1] = (byte) ((n & 0x000000FF) >> 0);
		return ret;
//		return ByteBuffer.allocate(2).putShort(s).array();
	}
	
	/**
	 * Converts integer to bytes (BE)
	 * 
	 * @param s
	 * @return
	 */
	public static byte[] toBytes(int n){
		byte[] ret = new byte[4];
		ret[0] = (byte) ((n & 0xFF000000) >> 24);
		ret[1] = (byte) ((n & 0x00FF0000) >> 16);
		ret[2] = (byte) ((n & 0x0000FF00) >> 8);
		ret[3] = (byte) ((n & 0x000000FF) >> 0);
		return ret;
//		return ByteBuffer.allocate(4).putInt(n).array();
	}
	
	/**
	 * Converts float to bytes (BE)
	 * 
	 * @param s
	 * @return
	 */
	public static byte[] toBytes(float f){
		return ByteBuffer.allocate(4).putFloat(f).array();
	}
	
	/**
	 * Converts long to bytes (BE)
	 * 
	 * @param s
	 * @return
	 */
	public static byte[] toBytes(long n){
		byte[] ret = new byte[8];
		ret[0] = (byte) ((n & 0xFF000000) >> 56);
		ret[1] = (byte) ((n & 0x00FF0000) >> 48);
		ret[2] = (byte) ((n & 0x0000FF00) >> 40);
		ret[3] = (byte) ((n & 0x000000FF) >> 32);
		ret[4] = (byte) ((n & 0xFF000000) >> 24);
		ret[5] = (byte) ((n & 0x00FF0000) >> 16);
		ret[6] = (byte) ((n & 0x0000FF00) >> 8);
		ret[7] = (byte) ((n & 0x000000FF) >> 0);
		return ret;
//		return ByteBuffer.allocate(8).putLong(n).array();
	}
	
	/**
	 * Converts double to bytes (BE)
	 * 
	 * @param d
	 * @return
	 */
	public static byte[] toBytes(double d){
		return ByteBuffer.allocate(8).putDouble(d).array();
	}

	/**
	 * Reads byte array from file
	 * 
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	public static byte[] toBytes(File file){
		try {
			FileInputStream stream = new FileInputStream(file);
			return toBytes(stream);
		} catch (FileNotFoundException e) {
			return new byte[0];
		} catch (IOException e) {
			return new byte[0];
		}
	}
	
	/**
	 * Reads byte array from stream
	 * 
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	public static byte[] toBytes(InputStream stream) throws IOException{
		ByteArrayOutputStream list = new ByteArrayOutputStream();

        byte[] buffer = new byte[4096];
        int c;
		while ((c = stream.read(buffer)) != -1) {
			if(c > 0){
				list.write(buffer, 0, c);
			}
        }
		list.flush();
		stream.close();
        
        return list.toByteArray();
	}

	/**
	 * Byte array to string with specified charset
	 * 
	 * @param encode
	 * @param charset
	 * @return
	 */

	public static String toString(byte[] arr, CharEncoding enc) {
		try {
			return new String(arr,enc.toString());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String toStringASCII(byte[] arr) {
		return toString(arr, CharEncoding.US_ASCII);
	}

	public static String toStringUtf8(byte[] arr) {
		return toString(arr, CharEncoding.UTF_8);
	}

	public static String toStringUTF16be(byte[] arr) {
		return toString(arr, CharEncoding.UTF_16BE);
	}


	public static String toStringUTF16le(byte[] arr) {
		return toString(arr, CharEncoding.UTF_16LE);
	}
	/**
	 * Byte array to string with specified charset
	 * 
	 * @param encode
	 * @param charset
	 * @return
	 */
	public static String toString(byte[] arr, Charset charset) {
		return charset.decode(ByteBuffer.wrap(arr)).toString();
	}

	/**
	 * Byte array to string with default charset ASCII7
	 * 
	 * @param encode
	 * @param charset
	 * @return
	 */
	public static String toString(byte[] arr) {
		return toString(arr, Utils.charsetDef());
	}

	
	/**
	 * byte array booleans rappresentation
	 * 
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	public static boolean[] toBooleans(byte[] data) {
		return toBooleans(data,-1);
	}

	
	/**
	 * byte array booleans representation
	 * 
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	public static boolean[] toBooleans(byte[] data, int cap) {
		boolean[] bits = new boolean[data.length * 8];
	    for (int i = 0; i < data.length * 8; i++) {
	      if ((data[i / 8] & (1 << (7 - (i % 8)))) > 0)
	        bits[i] = true;
	    }
	    if(cap > 0){
	    	return ArrayUtils.copyOf(bits, cap);
	    }
	    return bits;
	}
	
	/**
	 * Save byte array to file
	 * 
	 * @param byteArray
	 * @param dest
	 * @return
	 */
	public static boolean toFile(byte[] bytes, File dest){
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(dest);
			fos.write(bytes);
			fos.close();
			return true;
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			return false;
		} finally {
			try {
				if(fos != null)	fos.close();
			} catch (Exception e) {
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T[] toArray(List<T> arr, Class<T> c){
		return arr.toArray((T[]) Array.newInstance(c, arr.size()));
	}
    
    public static BufferedReader toReader(InputStream stream){
        return toReader(stream, Utils.charsetDef());
    }
	
	public static BufferedReader toReader(InputStream stream, Charset charset){
	    return new BufferedReader(new InputStreamReader(stream, charset));
	}
    
    public static BufferedReader toReader(URI uri){
        try {
            return toReader(uri.toURL().openStream());
        } catch (MalformedURLException e) {
//            e.printStackTrace();
            return null;
        } catch (IOException e) {
//            e.printStackTrace();
            return null;
        }
    }
    
    
    //  --------------- base64 ---------------
    
    /**
     * Encode to Base64
     * 
     * @param input
     * @return
     */
    public static byte[] toBase64(byte[] input){
        return new Base64().encode(input);
    }

    /**
     * Encode to Base64
     * 
     * @param input
     * @return
     */
    public static long toBase64(InputStream input, OutputStream output){
        return StreamUtils.write(toBase64(StreamUtils.read(input)), output);
    }
    
    /**
     * Encode to Base64
     * 
     * @param input
     * @return
     */
    public static String toBase64(String input, Charset charset){
        return new String(toBase64(input.getBytes(charset)),charset);
    }
    
    /**
     * Encode to Base64 using UTF-8
     * 
     * @param input
     * @return
     */
    public static String toBase64(String input){
        return toBase64(input, CharEncoding.UTF_8.getCharset());
    }
    
    /**
     * Decode from Base64
     * 
     * @param input
     * @return
     */
    public static byte[] fromBase64(byte[] input){
        return new Base64().decode(input);
    }

    /**
     * Decode from Base64
     * 
     * @param input
     * @return
     */
    public static long fromBase64(InputStream input, OutputStream output){
        return StreamUtils.write(fromBase64(StreamUtils.read(input)), output);
    }
    
    /**
     * Decode from Base64
     * 
     * @param input
     * @return
     */
    public static String fromBase64(String input, Charset charset){
        return new String(fromBase64(input.getBytes(charset)),charset);
    }
    
    /**
     * Decode from Base64 using UTF-8
     * 
     * @param input
     * @return
     */
    public static String fromBase64(String input){
        return fromBase64(input, CharEncoding.UTF_8.getCharset());
    }

}
