/*
 * HttpUtils.java
 *
 * (C) 2015 - 2015 Cedac Software S.r.l.
 */
package blue.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Map;

public final class HttpUtils {
    private HttpUtils() {}

    public static HttpURLConnection getConnection(URI target, String method, Map<String,String> params, String username, String password){
        HttpURLConnection conn;
        try {
            conn = (HttpURLConnection) target.toURL().openConnection();
            conn.setRequestMethod(method);
            if(method.equalsIgnoreCase("post")){
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            }
            if(username != null && password != null){
                conn.setRequestProperty("Authorization", "Basic " + new String(new Base64().encode((username+":"+password).getBytes())));
            }
            
            if(params != null) {
                byte[] content = toHttpQuery(params).getBytes();
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Length",  content.length+ "");
                StreamUtils.write(content, conn.getOutputStream());
            }
            return conn;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String toHttpQuery(Map<String, String> params) {
        String ret = "";
        for(String k:params.keySet()){
            String v = params.get(k);
            if(v == null){
                v = "";
            }
            if(ret.length() > 0){
                ret += "&";
            }
            try {
                ret += k + "=" + URLEncoder.encode(v,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }
        }
        return ret;
    }
    
}
