package blue.secure;

import java.io.IOException;
import java.net.InetSocketAddress;

import blue.net.tcp.Tcp;
import blue.secure.aes.SecureAES;
import blue.secure.rsa.SecureRSA;

public class SecureTcp {
	
	// =================================
	
	private Tcp tcp;
	private SecureRSA rsa;
	private SecureAES aes;
	private boolean serverMode;
	
	// =================================
	
	// server mode
	public SecureTcp(int port){
		this.tcp = new Tcp(port);
		this.tcp.setOptAppendSize(true);
		this.aes = new SecureAES();
		this.rsa = new SecureRSA();
		this.serverMode = true;
	}
	
	// open connection
	public SecureTcp(InetSocketAddress addr){
		tcp = new Tcp(addr);
		tcp.setOptAppendSize(true);
		this.aes = new SecureAES();
		this.rsa = new SecureRSA();
		this.serverMode = false;
	}
	
	private SecureTcp(Tcp conn){
		this.tcp = conn;
		this.aes = new SecureAES();
		this.rsa = new SecureRSA();
	}
	
	// =================================
	
	public SecureTcp connect(){
		return connect(0);
	}
	
	public SecureTcp connect(int timeout){
		if(serverMode){
			Tcp conn = tcp.openConnection(timeout);
			SecureTcp chx = new SecureTcp(conn);
			chx.rsa.setupPublicKey(conn.receiveBytes());
			conn.sendBytes(chx.rsa.getPublicKey().getEncoded());
			byte[] recKey = chx.rsa.decrypt(conn.receiveBytes());
			byte[] recIv = chx.rsa.decrypt(conn.receiveBytes());
			chx.aes.setup(recKey, recIv);
			return chx;
		}else{
			try {
				tcp.close();
			} catch (IOException e) {
			}
			if(tcp.openConnection(timeout) == null){
				return null; // error
			}
			// send  RSA public
			tcp.sendBytes(rsa.getPublicKey().getEncoded());
			// receive config
			rsa.setupPublicKey(tcp.receiveBytes());
			// send AES key
			tcp.sendBytes(rsa.encrypt(aes.getKey()));
			tcp.sendBytes(rsa.encrypt(aes.getIV()));
			return this;
		}
	}
	
	public boolean send(byte[] data){
		byte[] msg = aes.encrypt(data);
		return tcp.sendBytes(msg);
	}
	
	public byte[] receive() {
		byte[] msg = tcp.receiveBytes();
		byte[] dec = aes.decrypt(msg);
		return dec;
	}
	
//	
//	public static void main(String[] args) throws InterruptedException, UnsupportedEncodingException {
//
//		
//		new Task() {
//
//			@Override
//			protected void work() throws Throwable {
//				SecureTcp tcp = new SecureTcp(8099);
//				
//				SecureTcp client = tcp.connect();
//				System.out.println(new String(client.receive()));
//				System.out.println(new String(client.receive()));
//
//				SecureTcp client2 = tcp.connect();
//				System.out.println(new String(client2.receive()));
//				System.out.println(new String(client2.receive()));
//			}
//			
//		}.start();
//		
//		SecureTcp tcp = new SecureTcp(NetUtils.getLocalAddress(8099));
//		System.out.println(tcp.connect());
//		tcp.send("11 ciao".getBytes());
//		tcp.send("11 csd fsdf ssdfffffffffffffffffffiaonf".getBytes("UTF-8"));
////		Thread.sleep(1000);
//		SecureTcp tcp2 = new SecureTcp(NetUtils.getLocalAddress(8099));
//		System.out.println(tcp2.connect());
//		tcp2.send("22 csd fsdf ssdfffffffffffffsdf  sd fsdf ffffffiaofg".getBytes("UTF-8"));
//		tcp2.send("22 csd fsdf ssdfasf fffffffffffffffffffffsdf sdf sd gggggggfsdf ffffffiaonf".getBytes("UTF-8"));
//		
//	}
}
