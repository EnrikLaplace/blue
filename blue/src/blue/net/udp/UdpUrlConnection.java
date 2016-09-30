package blue.net.udp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.AlreadyConnectedException;

import blue.util.NetUtils;

public class UdpUrlConnection extends URLConnection {
	
	private Udp com;
	public UdpUrlConnection(URL url) {
		super(url);
	}
	
	@Override
	public void connect() throws IOException {
		if(com != null){
			throw new AlreadyConnectedException();
		}
		this.com = new Udp(NetUtils.toAddress(url));
	}
	
	/**
	 * Open new input stream
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		if(com == null){
			connect();
		}
		return new ByteArrayInputStream(com.receiveBytes());
	}
}
