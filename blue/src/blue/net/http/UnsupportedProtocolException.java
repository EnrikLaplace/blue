package blue.net.http;

public class UnsupportedProtocolException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2097407206453248050L;
	private String protocol;
	
	public UnsupportedProtocolException(String protocol){
		super("Not supported " + protocol);
		this.protocol = protocol;
	}
	
	public UnsupportedProtocolException(String protocol, String message){
		super(message);
		this.protocol = protocol;
	}
	
	@Override
	public String getMessage() {
		// TODO Auto-generated method stub
		return super.getMessage();
	}
	
	public String getProtocol() {
		return protocol;
	}
}
