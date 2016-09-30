package blue.net.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.AlreadyConnectedException;

import blue.util.NetUtils;

public class TcpUrlConnection extends URLConnection {
	
	private Tcp tcp;
	public TcpUrlConnection(URL url) {
		super(url);
	}
	
	@Override
	public void connect() throws IOException {
		if(tcp != null){
			throw new AlreadyConnectedException();
		}
		this.tcp = new Tcp(NetUtils.toAddress(url));
		this.tcp.openConnection(0);
	}
	
	/**
	 * Open new input stream
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		if(tcp == null){
			connect();
		}
		return tcp.openStreamIn();
	}
}
