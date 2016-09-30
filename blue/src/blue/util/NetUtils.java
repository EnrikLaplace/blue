package blue.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;

import blue.lang.Cache;

public class NetUtils {
    private static final Cache<String, Integer> protocolDefaultPort = new Cache<String, Integer>() {
        
        @Override
        protected Integer init(String protocol) {
            if(protocol.equalsIgnoreCase("svn")){
                return 80; // not supported
            }
            try {
                return new URL(protocol+"://exe.it").getDefaultPort();
            } catch (MalformedURLException e) {
                throw new RuntimeException("Invalid protocol: "+protocol);
            }
        }
    };
    
	private NetUtils() {}
	
	/**
	 * Calculate random free port emulating system config
	 * 
	 * @return
	 */
	public static int getRandomPort(){
		try {
			ServerSocket sck = new ServerSocket(0);
			int prt = sck.getLocalPort();
			sck.close();
			return prt;
		} catch (IOException e) {
			return -1;
		}
	}
	
	public static int getDefaultPort(String protocol){
	    return protocolDefaultPort.get(protocol);
	}

	public static InetAddress getLocalHost() {
		try {
			return InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Default NET charset is ASCII 7
	 * @return
	 */
	public static Charset defaultCharset(){
		return Charset.forName("ASCII7");
	}
	
	/**
	 * Converts string to byte array in ASCII 7
	 * 
	 * @param str
	 * @return
	 */
	public static byte[] asBytes(String str){
		return defaultCharset().encode(str).array();
	}
	
	/**
	 * Converts byte array to string in ASCII 7
	 * 
	 * @param arr
	 * @return
	 */
	public static String asBytes(byte[] arr){
		return defaultCharset().decode(ByteBuffer.wrap(arr)).toString();
	}

	/**
	 * Convert URL to address
	 * 
	 * @param url
	 * @return
	 */
	public static InetSocketAddress toAddress(URL url) {
		return new InetSocketAddress(url.getHost(), url.getPort() == -1? url.getDefaultPort():url.getPort());
	}

	public static InetSocketAddress getLocalAddress(int port) {
		return new InetSocketAddress(getLocalHost(), port);
	}
	
	public static byte[] resolve(String host){
		try {
			return InetAddress.getByName(host).getAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String[] getAvailableAddresses(){
		try {
			Enumeration<NetworkInterface> ielist;
			ArrayList<String> ret = new ArrayList<String>();
			ielist = NetworkInterface.getNetworkInterfaces();
			while(ielist.hasMoreElements()) {
				NetworkInterface netint = ielist.nextElement();
				Enumeration<InetAddress> ee = netint.getInetAddresses();
			    while (ee.hasMoreElements())
			    {
			        InetAddress i = (InetAddress) ee.nextElement();
			        ret.add(i.getCanonicalHostName());
			    }
			}
			return ret.toArray(new String[ret.size()]);
		} catch (SocketException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	
	
	public static void main(String[] args) {
		for(String s:getAvailableAddresses()) {
			System.out.println(s);
		}
	}
}
