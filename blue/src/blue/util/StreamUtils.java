package blue.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class StreamUtils {
	
	public static final int DEFAULT_BUFFER = 2048;
	
	private StreamUtils(){
		
	}
	
	/**
	 * Converts bytes to input stream
	 * @param data
	 * @return
	 */
	public static InputStream asStream(byte[] data){
		return new ByteArrayInputStream(data);
	}
    public static byte[] read(InputStream in){
        return read(in, true);
    }
	
	/**
	 * Read all stream in byte
	 * 
	 * @return
	 */
	public static byte[] read(InputStream in, boolean autoCloseInput){
		if(in == null) {
			return null;
		}
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		copy(in, bytes,autoCloseInput);
		return bytes.toByteArray();
	}
	
	/**
	 * Read all stream in byte
	 * 
	 * @return
	 */
	public static byte[] read(InputStream in, long length){
		if(in == null) {
			return null;
		}
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		copy(in, bytes,length);
		return bytes.toByteArray();
	}
	
	/**
	 * Read all stream in byte
	 * 
	 * @return
	 */
	public static long write(byte[] data, OutputStream out){
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		return copy(in, out);
	}

	/**
	 * Read stream IN to stream OUT
	 * automatic close input
	 * 
	 * @param in
	 * @param out
	 * @param length
	 * @param buffSize
	 * @return
	 */
	public static long copy(InputStream in, OutputStream out, boolean autoCloseInput){
		try{
			return copy(in,out);
		}catch(Exception e){
			throw new RuntimeException(e);
		}finally {
			if(autoCloseInput){
				try {
					in.close();
				} catch (IOException e) {
					
				}
			}
		}
	}

	/**
	 * Read stream IN to stream OUT
	 * 
	 * @param in
	 * @param out
	 * @param length
	 * @param buffSize
	 * @return
	 */
	public static long copy(InputStream in, OutputStream out){
		return copy(in,out,-1);
	}

	/**
	 * Read stream IN to stream OUT
	 * 
	 * @param in
	 * @param out
	 * @param length
	 * @param buffSize
	 * @return
	 */
	public static long copy(InputStream in, OutputStream out, long length){
		return copy(in,out,length,DEFAULT_BUFFER);
	}
	
	/**
	 * Read stream IN to stream OUT with specified amount of data with same size buffer
	 * 
	 * @param in
	 * @param out
	 * @return
	 */
	public static long fastCopy(InputStream in, OutputStream out, long length){
		return copy(in,out,length,(int)length);
	}
	
	/**
	 * Read stream IN to stream OUT
	 * 
	 * @param in
	 * @param out
	 * @param length
	 * @param buffSize
	 * @return
	 */
	public static long copy(InputStream in, OutputStream out, long length, int buffSize){
		if(length == 0){
			return 0;
		}
		byte[] buff = new byte[buffSize];
		long read = 0;
		while(read < length || length == -1){
			try {
			    int toRead = (length == -1)? buff.length : Math.min(buff.length,(int)(length-read));
				int lastRead = in.read(buff, 0, toRead);
				if(lastRead == -1){
					break;
				}
				out.write(buff,0,lastRead);
				read += lastRead;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return read;
	}

	/**
	 * 
	 * @param str
	 * @param tempFile
	 * @return 
	 */
    public static long save(InputStream in, File dest) {
        FileOutputStream out;
        try {
            out = new FileOutputStream(dest);
            long ret = copy(in, out);
            out.close();
            return ret;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return -1;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
