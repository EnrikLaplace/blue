package blue.net;

import java.io.Closeable;
import java.text.ParseException;

import blue.com.Serial;
import blue.util.Convert;

public abstract class Com implements Closeable {
	
	public <T> boolean sendObject(T obj){
		try {
			return sendBytes(Serial.encode(obj));
		} catch (ParseException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public <T> T receiveObject(){
		try {
			return Serial.decode(receiveBytes());
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void sendShort(Number num){
		sendBytes(Convert.toBytes(num.shortValue()));
	}
	
	public void sendDouble(Number num){
		sendBytes(Convert.toBytes(num.doubleValue()));
	}
	
	public void sendLong(Number num){
		sendBytes(Convert.toBytes(num.longValue()));
	}
	
	public void sendInt(Number num){
		sendBytes(Convert.toBytes(num.intValue()));
	}
	
	// ===============================================
	
	protected void finalize() throws Throwable {
		close();
	};
	
	// ===============================================
	
	public abstract boolean sendBytes(byte[] data);
	public abstract byte[] receiveBytes();
	public abstract boolean isServerMode();
	
	protected abstract boolean connect(int timeout);
	
	protected boolean connect(){
		return connect(0); // default
	}
}
