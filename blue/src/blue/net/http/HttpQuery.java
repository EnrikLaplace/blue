package blue.net.http;

import java.util.TreeMap;

import blue.util.Utils;

public class HttpQuery extends TreeMap<String, String> {
	private static final long serialVersionUID = -127014583741238650L;

	public HttpQuery(){
		// empty
	}
	
	public HttpQuery(String query){
		if(query == null) {
			return;
		}
		String[] data = query.split("&");
		for(String d:data) {
			if(d.indexOf("=") <= 0) {
				continue;
			}
			String key = Utils.splitBefore(d, "=");
			if(key.length() == 0){
				continue;
			}
			String val = Utils.splitAfter(d, "=");
			if(val.length() == 0){
				val = null;
			}
			put(key, val);
		}
	}
	
	@Override
	public String toString() {
		String qr = "";
		for(String hk:keySet()) {
			// ignore null
			if(hk == null) {
				continue;
			}
			if(qr.length() > 0) {
				qr += "&";
			}
			qr += hk + "=" + (get(hk) != null ? get(hk):"");
		}
		return qr;
	}
	
}
