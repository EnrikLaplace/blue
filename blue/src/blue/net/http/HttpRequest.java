package blue.net.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import blue.util.AsyncStreamCopy;
import blue.util.Base64;
import blue.util.ClassUtils;
import blue.util.StreamUtils;
import sun.net.www.MessageHeader;

public class HttpRequest {
	
	// ======================================
	
	private HttpURLConnection conn;
	
	private Proxy proxy;
	private Cookies cookies;
	private HttpFiles files = new HttpFiles();
	private HttpQuery post = new HttpQuery();
	private InputStream stream;
	private int respCode = -1;
	private Map<String, String> reqHead = new HashMap<String, String>();
	private HttpMethod method = HttpMethod.GET;
	
	// tollerance to 5xx error
	private int tollerance5xx = 3; // 3 retry for error 5xx
	
	// ======================================
	
	public HttpRequest(String uri){
		try {
		    URL convUri = new URL(uri);
			init(convUri);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public HttpRequest(URL uri){
		init(uri);
	}
	
	// ----------------------------------
	
	private void retry(URL uri){
		// allow http/https
		String protocol = uri.getProtocol().toLowerCase();
		if(!protocol.startsWith("http")) {
			throw new UnsupportedProtocolException(protocol);
		}
		
		// valid protocol
		try {
			if(proxy != null) {
				conn = (HttpURLConnection) uri.openConnection(proxy);
			}else {
				conn = (HttpURLConnection) uri.openConnection();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void init(URL uri) {
		retry(uri);
		
		// initialize connection
		setUseBrowser(true);
		setFollowRedirect(true);
		
		// cookies
		cookies = new Cookies();
		reqHead.clear();
	}
	
	public long getContentLength(){
		try{
			return conn.getContentLength();
		} catch(Exception e){
			return -1;
		}
	}
	
	// ======================================

	/**
	 * WARNING change method will reset internal connection
	 * @param proxy
	 * @return
	 */
	public HttpRequest setMethod(HttpMethod method){
//		conn.setRequestMethod(method.toString());
		this.method = method;
		return this;
	}
	
	/**
	 * Change tollerance to 5xx error
	 * 
	 * @param retry
	 * @return
	 */
	public HttpRequest set5xxTollerance(int retry){
		this.tollerance5xx = retry;
		return this;
	}

	public HttpRequest setHttpProxy(String host, int port){
		return setHttpProxy(new InetSocketAddress(host, port));
	}

	/**
	 * WARNING change proxy will reset internal connection
	 * @param proxy
	 * @return
	 */
	public HttpRequest setHttpProxy(InetSocketAddress addr){
		return setProxy(new Proxy(Type.HTTP, addr));
	}
	
	/**
	 * WARNING change proxy will reset internal connection
	 * @param proxy
	 * @return
	 */
	public HttpRequest setProxy(Proxy proxy){
		this.proxy = proxy;
		init(getUrl());
		return this;
	}

	// edit header info
	public HttpRequest setHeader(String key, String value){
		if(value == null) {
			reqHead.remove(key);
			return this;
		}
//		conn.setRequestProperty(key, value);
		reqHead.put(key, value);
		return this;
	}
	

	public HttpRequest setCookie(String key, String value){
		cookies.set(key, value);
		return this;
	}

	/**
	 * WARNING change query will reset internal connection
	 * @param proxy
	 * @return
	 */
	public HttpRequest setQueryField(String field, String value){
		HttpQuery query = new HttpQuery(conn.getURL().getQuery());
		query.put(field, value);
		return setQuery(query);
	}

	/**
	 * WARNING change query will reset internal connection
	 * @param proxy
	 * @return
	 */
	public HttpRequest setQuery(HttpQuery query){
		URL url = conn.getURL();
		// convert map to string
		// update uri
		URL uri = null;
		try {
			uri = new URI(url.getProtocol(), url.getAuthority(), url.getPath(), query.toString(),null).toURL();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		} catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
		init(uri);
		return this;
	}

	public HttpRequest setPostQuery(Map<String,String> query){
		post.clear();
		for(Entry<String, String> q:query.entrySet()) {
			post.put(q.getKey(), q.getValue());
		}
		return this;
	}

	public HttpRequest setPost(String key, String val){
		post.put(key, val);
		return this;
	}
	
	public HttpRequest setFollowRedirect(boolean val){
		conn.setInstanceFollowRedirects(val);
		return this;
	}
	
	// ---------------- sc --------------------
	
	public HttpRequest setOrigin(URI origin){
		if(origin == null){
			return this;
		}
		return setHeader("Origin", origin.toString()).
				setHeader("Referer", origin.toString());
	}
	
	public HttpRequest setKeepAlive(boolean keepAlive){
		return setHeader("Connection", keepAlive?"Keep-Alive":"close");
	}

    public HttpRequest auth(String name, String password) {
        return setHeader("Authorization", "Basic " + new String(Base64.encodeBase64String((name+":"+password).getBytes())));
    }
	
	public HttpRequest setUseBrowser(boolean val){
		if(val) {
			return setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
		} else {
			return setHeader("User-Agent", null);
		}
	}
	
	public HttpRequest attachFile(String name, String fileName, InputStream fileData){
		files.add(name, fileName, fileData);
		return this;
	}

	
	// ======================================
	
	public int getRespCode() {
		return respCode;
	}
	
	public URL getUrl() {
		return conn.getURL();
	}
	
	public InputStream open(){
		try {
			if(respCode > 0){
				return stream;
			}
			
			int maxRetry = conn.getInstanceFollowRedirects()? 10:1;
			while(maxRetry-- > 0) {
				
				// method
				conn.setRequestMethod(method.toString());
				
				// head info
				for(Entry<String, String> pp:reqHead.entrySet()){
					conn.setRequestProperty(pp.getKey(), pp.getValue());
				}
				
				// set cookie
				if(cookies.size() > 0) {
					conn.setRequestProperty("Cookie", cookies.toString());
				}
				// set post data
				if(post.size() > 0) {
					conn.setDoOutput(true);
					conn.getOutputStream().write(post.toString().getBytes());
				}
				// set files
				files.write(conn);
				// connect
				conn.connect();
				// read response
				respCode = conn.getResponseCode();
				// tollerance to 500
				switch(respCode) {
				case 502:
				case 503:
					if(tollerance5xx > 0){
						tollerance5xx--;
						maxRetry++;
						retry(getUrl());
						continue;
					}
					break;
				}
				// redirect?
				if(conn.getInstanceFollowRedirects()) {
					switch(respCode) {
					case 301:
					case 302:
					case 303:
						// follow iteration
						try {
						    URL location = new URL(conn.getHeaderField("Location"));
							init(location);
						} catch (MalformedURLException e) {
							e.printStackTrace();
							// invalid redirect
							maxRetry=0;
						}
						continue;
					}
				}
				
				// ok
				break;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		// return stream
		try {
			stream = conn.getInputStream();
			MessageHeader respHeaders = ((MessageHeader)ClassUtils.getField(conn, "responses"));
			if(respHeaders != null){
				Map<String,List<String>> header = respHeaders.getHeaders();
				for(Entry<String, List<String>> hdr:header.entrySet()){
					if(hdr.getKey() != null && hdr.getKey().equalsIgnoreCase("Set-Cookie")){
						cookies.set(hdr.getValue());
					}
				}
			}
//			cookies.update();
			return stream;
		} catch (IOException e) {
			// no input stream
			e.printStackTrace();
			stream = null;
			return null;
		}
	}
	
	public byte[] read() {
		return StreamUtils.read(open());
	}
	
	public long download(OutputStream out) {
		return StreamUtils.copy(open(), out);
	}
	
	public AsyncStreamCopy asyncDownload(OutputStream out){
		InputStream in = open();
		long len = getContentLength();
		AsyncStreamCopy str = new AsyncStreamCopy(in, out, len);
		str.setCloseIn(true);
		str.setCloseOut(true);
		return str;
	}
	
	public String readString() {
		byte[] data = read();
		if(data == null) {
			return null;
		}
		return new String(data);
	}
	
	public boolean test(){
	    // read response
	    try{
	        read();
	    }catch(Exception e){
	        return false;
	    }
	    // return code
	    return (""+respCode).substring(0, 1).equals("2");
	}
	
	// ======================================
	
	public static HttpRequest get(URL uri){
		return new HttpRequest(uri).setMethod(HttpMethod.GET);
	}
	
	public static HttpRequest post(URL uri){
		return new HttpRequest(uri).setMethod(HttpMethod.POST);
	}

	public Cookie getCookie(String key) {
		return cookies.get(key);
	}
}
