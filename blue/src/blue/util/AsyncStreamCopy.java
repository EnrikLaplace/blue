package blue.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import blue.lang.Operation;

public class AsyncStreamCopy extends Operation {
	
	// ==============================================

	private long length = -1;
	private long opLength = -1;
	private int buffSize;
	private InputStream in;
	private OutputStream out;
	
	private boolean closeIn = false;
	private boolean closeOut = false;
	
	// ==============================================
	
	public AsyncStreamCopy(InputStream in, OutputStream out){
		this(in, out,-1, StreamUtils.DEFAULT_BUFFER);
	}
	
	public AsyncStreamCopy(InputStream in, OutputStream out, long length){
		this(in, out,length, StreamUtils.DEFAULT_BUFFER);
	}
	
	public AsyncStreamCopy(InputStream in, OutputStream out, long length, int buffSize){
		this.in = in;
		this.out = out;
		this.length = length;
		this.buffSize = buffSize;
	}
	
	// ==============================================

	@Override
	protected void work() throws Throwable {
		if(length == 0){
			return;
		}
		byte[] buff = new byte[buffSize];
		opLength = 0;
		while(opLength < length || length == -1){
			try {
			    int toRead = (length == -1)? buff.length : Math.min(buff.length,(int)(length-opLength));
				int lastRead = in.read(buff, 0, toRead);
				if(lastRead == -1){
					break;
				}
				out.write(buff,0,lastRead);
				opLength += lastRead;
				if(length > 0) {
					setPerc(((double)opLength) / ((double)length));
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		if(closeIn){
			in.close();
		}
		if(closeOut){
			out.close();
		}
		setResult(0);
	}
	
	@Override
	protected synchronized void threadStart() {
		super.threadStart();
	}
	
	@Override
	protected synchronized void threadEnd() {
		super.threadEnd();
	}
	
	public void setCloseIn(boolean closeIn) {
		this.closeIn = closeIn;
	}
	
	public void setCloseOut(boolean closeOut) {
		this.closeOut = closeOut;
	}
	
}
