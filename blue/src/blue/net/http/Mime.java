package blue.net.http;

import java.util.Properties;

import blue.util.Utils;

public class Mime {
	
	private static Properties MIME;
	
	// =======================================
	
	//
	static {
		// load mime
		MIME = Utils.getProperty(Mime.class.getPackage().getName().replaceAll("\\.", "/")+"/mime.properties");
	}
	
	public static String getFromExtension(String ext){
		return MIME.getProperty(ext);
	}
	
	// =======================================
	
	private Mime() {
		
	}
}
