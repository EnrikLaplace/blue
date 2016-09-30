package blue.net.http;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import com.sun.net.httpserver.Headers;

import blue.util.Utils;

public class Cookies extends HashMap<String, Cookie>{
	private static final long serialVersionUID = -7890325406834361685L;
	
	// ===================================

	public static String HTTP_REQ_HEADER = "Cookie";
	public static String HTTP_RESP_HEADER = "Set-Cookie";
	
	// ===================================

	Cookies(){
		// 
	}

	public Cookies(Headers hds){

		// read cookies from client
		if(hds.containsKey(HTTP_REQ_HEADER)) {
			// parse
			for(String c:hds.get(HTTP_REQ_HEADER)) {
				for(String data:c.split("; ")) {
					this.set(data.substring(0, data.indexOf("=")), data.substring(data.indexOf("=")+1));
				}
			}
		}
	}
	
	public void update(Headers resp){
		resp.put(HTTP_RESP_HEADER, toList());
	}

	public Cookie set(String name, String value){
		return put(name, new Cookie(name, value));
	}
	
	public List<String> toList() {
		Vector<String> ret = new Vector<String>();
		for(Cookie c:this.values()) {
			ret.addElement(c.toString());
		}
		return ret;
	}

	public String getValue(String name) {
		if(!containsKey(name)) {
			return null;
		}
		return get(name).getValue();
	}
	
	@Override
	public String toString() {
		String ret = "";
		for(Cookie c:this.values()) {
			ret += c.toString() + "; ";
		}
		return ret;
	}

	public void update(String value) {
		// TODO Auto-generated method stub
		
	}

	public void set(List<String> value) {
		for(String vv:value){
			vv=vv.split(";")[0];
			set(Utils.splitBefore(vv, "="), Utils.splitAfter(vv, "="));
		}
	}
}
