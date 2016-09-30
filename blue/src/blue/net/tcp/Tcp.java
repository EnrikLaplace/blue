package blue.net.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import blue.net.Com;
import blue.util.Convert;
import blue.util.StreamUtils;

public class Tcp extends Com {
	
	// ================================
	
	private Socket socket;
	private InetSocketAddress addr;
	private ServerSocket server;

	private InputStream instream;
	private OutputStream outstream;
	
	// option: append message size
	private int timeout  = 0; // default
	private boolean opt_append_size;
	
	// ================================
	
	private Tcp(Socket socket){
		this.socket = socket;
		this.addr = new InetSocketAddress(socket.getInetAddress(),socket.getPort());
	}
	
	public Tcp(int port){
		try {
			server = new ServerSocket(port);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Tcp(InetSocketAddress addr){
		this.addr = addr;
	}
	
	// ================================

	public void close() throws IOException {
		if(socket != null){
			socket.close();
			socket = null;
		}
	}

	@Override
	public boolean sendBytes(byte[] data) {
		try {
			OutputStream strout = openStreamOut();
			// connect error?
			if(strout == null){
				return false;
			}
			if(opt_append_size){
				byte[] app = Convert.toBytes((long)data.length);
				strout.write(app, 0, 8);
			}
			strout.write(data, 0, data.length);
			return true;
		} catch (IOException e) {
//			e.printStackTrace();
			return false;
		}
	}

	@Override
	protected boolean connect(int timeout) {
		try{
			this.timeout = timeout;
			if(socket == null) {
				if(addr == null) {
					waitNextConnection();
				}else {
					socket = new Socket();
					socket.connect(addr, timeout);
					return socket.isConnected();
				}
			}
			return true;
		}catch(Exception e) {
//			e.printStackTrace();
			return false;
		}
	}
	
	public Tcp openConnection(int timeout){
		try {
			this.timeout = timeout;
			if(server != null) {
				waitNextConnection();
				Tcp newTcp = new Tcp(socket);
				newTcp.setOptAppendSize(opt_append_size);
				return newTcp;
			} else {
				checkConnection();
				return this;
			}
		} catch (IOException e) {
//			e.printStackTrace();
			return null;
		}
	}
	
	public synchronized InputStream openStreamIn() {
		try {
			checkConnection();
			if(instream == null) {
				checkConnection();
				instream = socket.getInputStream();
			}
			return instream;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public synchronized OutputStream openStreamOut() {
		try {
			checkConnection();
			if(outstream == null) {
				checkConnection();
				outstream = socket.getOutputStream();
			}
			return outstream;
		} catch (IOException e) {
//			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * receive all input bytes until EOF or end of stream
	 * @return
	 */
	public synchronized byte[] receiveBytes(){
		InputStream str = openStreamIn();
		if(opt_append_size){
			byte[] rdsz = StreamUtils.read(str,8);
			// error
			if(rdsz == null){
				return null;
			}
			long bsize = Convert.toInt64(rdsz);
			return StreamUtils.read(str, bsize);
		}
		return StreamUtils.read(str,false);
	}
	
	// ================================
	
	protected void waitNextConnection() throws IOException{
		socket = server.accept();
	}
	
	private synchronized void checkConnection() throws ConnectException{
		if((socket == null || socket.isClosed() || !socket.isConnected()) && !connect(timeout)){
			throw new ConnectException();
		}
	}
	
	
	// =======================================
	
	public void setOptAppendSize(boolean val) {
		this.opt_append_size = val;
	}
	
	public boolean isOptAppendSize() {
		return opt_append_size;
	}

	@Override
	public boolean isServerMode() {
		return addr == null;
	}
}
