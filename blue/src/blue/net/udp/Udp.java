package blue.net.udp;

import java.io.IOException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

import blue.net.Com;
import blue.util.ArrayUtils;

public class Udp extends Com {
	
	// =====================================================
	
	public static final int MAX_PACKET_SIZE = 512;
	
	// =====================================================
	
	private DatagramSocket socket;
	private InetSocketAddress addr;
	private boolean serverMode;
	
	// =====================================================
	
	public Udp(int port){
		try {
			socket = new DatagramSocket(port);
			serverMode = true;
		} catch (SocketException e) {
			throw new RuntimeException(e);
		} 
	}
	
	public Udp(InetSocketAddress addr){
		this.addr = addr;
		try {
			socket = new DatagramSocket();
			serverMode = false;
		} catch (SocketException e) {
			throw new RuntimeException(e);
		} 
	}
	
	// =====================================================

	public void close() throws IOException {
		socket.close();
	}
	
	public synchronized byte[] receiveBytes(){
		byte[] buff = new byte[MAX_PACKET_SIZE];
		DatagramPacket pkt = new DatagramPacket(buff, buff.length);
		try {
			socket.receive(pkt);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		addr = (InetSocketAddress) pkt.getSocketAddress();
		if(buff.length != pkt.getLength()) {
			buff = ArrayUtils.sub(buff, 0, pkt.getLength());
		}
		return buff;
	}

	@Override
	public boolean sendBytes(byte[] data) {
		try {
			checkConnection();
			DatagramPacket pkt = new DatagramPacket(data, data.length);
			if(addr != null) {
				pkt.setAddress(addr.getAddress());
				pkt.setPort(addr.getPort());
			}
			socket.send(pkt);
			return true;
		} catch (ConnectException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	// =====================================================
	
	private void checkConnection() throws ConnectException {
		if(socket.isClosed() && !connect()) {
			throw new ConnectException();
		}
	}

	@Override
	protected boolean connect(int timeout) {
		
		if(addr != null && !socket.isConnected()) {
			try {
				socket = new DatagramSocket();
				socket.setSoTimeout(timeout);
				socket.connect(addr);
			} catch (SocketException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		// already connected?
		return socket.isConnected();
	}

	@Override
	public boolean isServerMode() {
		return serverMode;
	}
	
}
