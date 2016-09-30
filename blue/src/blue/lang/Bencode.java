package blue.lang;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import blue.com.Serial.ISerial;
import blue.util.ArrayUtils;
import blue.util.Convert;
import blue.util.Utils;

public final class Bencode implements ISerial {

	// =====================================================
	//					BcType
	// =====================================================
	
	private enum BcType {
		Bytes,
		Int,
		List,
		Map
	}

	// =====================================================
	//					BcObject
	// =====================================================
	
	
	public abstract class BcObject {
		
		// type of data
		private BcType type;
		
		protected BcObject owner = null;
		
		private BcObject(BcType type){
			this.type = type;
		}

		/**
		 * @return the type
		 */
		public BcType getType() {
			return type;
		}

		/**
		 * @return the owner
		 */
		public BcObject getOwner() {
			return owner;
		}
		
		/**
		 * Encode to Bencode
		 * 
		 * @return
		 */
		public abstract byte[] encode();
		
		public Bencode toBencode(){
			return new Bencode(encode());
		}
		
		@Override
		public String toString() {
			return Convert.toString(encode(), charset());
		}
		// =============================
	}

	// =====================================================
	//					BcList
	// =====================================================
	
	public class BcList extends BcObject implements Iterable<BcObject> {

		// treemap ordered by data entry
		private TreeMap<String, BcObject> data;
		private Vector<String> entryOrder;
		
		// =========================================
		
		private BcList(BcType type) {
			super(type);
			data = new TreeMap<String, BcObject>();
			entryOrder = new Vector<String>();
		}
		
		public void add(BcObject obj){
			assert getType() == BcType.List;
			obj.owner = this;
			String idx = String.format("%030d", data.size());
			data.put(idx, obj);
			entryOrder.add(idx);
		}
		
		public void add(String key, BcObject obj){
			assert getType() == BcType.Map;
			obj.owner = this;
			data.put(key, obj);
			entryOrder.add(key);
		}
		
		// =========================================

		/**
		 * search index as list
		 * 
		 * @param index
		 * @return
		 */
		public boolean has(int index){
			return data.containsKey(index+"");
		}
		
		/**
		 * Get object by index as list
		 * 
		 * @param index
		 * @return
		 */
		public BcObject get(int index){
			return data.get(index + "");
		}

		/**
		 * search key as map
		 * 
		 * @param index
		 * @return
		 */
		public boolean has(String key){
			return data.containsKey(key);
		}
		
		
		/**
		 * get object by key as map
		 * 
		 * @param key
		 * @return
		 */
		public BcObject get(String key){
			return data.get(key);
		}

		/**
		 * get object as map
		 * 
		 * @param string
		 * @return
		 */
		public BcList getList(String key) {
			return (BcList) data.get(key);
		}
		
		/**
		 * Stored object as list
		 * 
		 * @return
		 */
		public Iterator<BcObject> iterator(){
			return data.values().iterator();
		}
		
		/**
		 * get object as int
		 * 
		 * @param key
		 * @return
		 */
		public int getInt(String key){
			return ((BcData)data.get(key)).asInt();
		}
		
		/**
		 * get object as long
		 * 
		 * @param key
		 * @return
		 */
		public long getLong(String key){
			return ((BcData)data.get(key)).asLong();
		}
		
		/**
		 * get object as string
		 * 
		 * @param key
		 * @return
		 */
		public String getString(String key){
			return ((BcData)data.get(key)).asString();
		}
		
		/**
		 * get object as byte array
		 * 
		 * @param key
		 * @return
		 */
		public byte[] getBytes(String key){
			return ((BcData)data.get(key)).asBytes();
		}

		public String[] keySet() {
			Set<String> st = data.keySet();
			return st.toArray(new String[st.size()]);
		}
		
		// ============================================

		@Override
		public byte[] encode() {
			byte[] ret = new byte[]{
				(byte) (getType() == BcType.List?'l':'d')
			};

			for(String key:entryOrder){
				BcObject val = data.get(key);
				// ignore null values
				if(val == null)
					continue;
				if(getType() == BcType.Map){
					ret = ArrayUtils.concat(ret, encodeString(key));
				}
				ret = ArrayUtils.concat(ret, val.encode());
			}
			ret = ArrayUtils.concat(ret, new byte[]{'e'});
			return ret;
		}
	}


	// =====================================================
	//					BcData
	// =====================================================
	
	
	public class BcData extends BcObject {

		// stored data
		private Object data;
		
		// ====================================
		
		// 
		private BcData(BcType type, Object data) {
			super(type);
			this.data = data;
		}
		
		// ====================================

		/**
		 * @return the data
		 */
		public Object getData() {
			return data;
		}

		public String asString() {
			if(getType() == BcType.Int){
				return data+"";
			}
			return Convert.toString((byte[]) data, charset());
		}
		
		@Override
		public String toString() {
			return asString();
		}
		
		public int asInt(){
			return Integer.parseInt(asString());
		}
		
		public long asLong(){
			return Long.parseLong(asString());
		}

		public byte[] asBytes() {
			return (byte[]) data;
		}

		@Override
		public byte[] encode() {
			if(getType() == BcType.Int)
				return encodeLong((Number)data);
			return encodeBytes((byte[]) data);
		}
	}

	// =====================================================

	private byte[] data;
	
	// ------------------------------------------------------
	
	private Bencode() {
		this(new byte[0]);
	}
	
	public Bencode(byte[] data) {
		this.data = data;
	}
	
	public Bencode(Object obj) {
		this.data = _do_encode(obj).encode();
	}

	// =====================================================
	
	public BcObject toObject() {
		return _do_decode(data);
	}

	@Override
	public byte[] toBytes() {
		return data;
	}

	@Override
	public void fromBytes(byte[] data) {
		this.data = data;
	}
	
	/**
	 * Decode string Bencode
	 * 
	 * @param encoded
	 * @return
	 */
	public static BcObject decode(String encoded){
		return new Bencode()._do_decode(Convert.toBytes(encoded, charset()));
	}
	
	/**
	 * Check data format validity
	 * 
	 * @param data
	 * @return
	 */
	public static boolean isValid(byte[] data){
		return new Bencode()._do_decode(data) != null;
	}

	/**
	 * Converts byte array to decoded Bencode
	 * 
	 * @param data
	 * @return
	 */
	private BcObject _do_decode(byte[] data) {
		if(Arrays.equals(data, new byte[data.length])){
			return null;
		}
		try{
			return _decode((byte) -1, new ByteArrayInputStream(data));
		}catch(Exception e){
			return null;
		}
	}

	// =====================================================
	
	@SuppressWarnings("rawtypes")
	private BcObject _do_encode(Object obj) {

		// array
		if (obj.getClass().isArray()) {

			if (obj.getClass().getComponentType().equals(byte.class)) {
				return new BcData(BcType.Bytes, obj);
			}
			int len = Array.getLength(obj);
			BcList ret = new BcList(BcType.List);
			for (int i = 0; i < len; i++) {
				ret.add(_do_encode(Array.get(obj, i)));
			}
			return ret;
		}
		if (BcObject.class.isAssignableFrom(obj.getClass())) {
			return (BcObject) obj;
		}
		if (List.class.isAssignableFrom(obj.getClass())) {
			BcList ret = new BcList(BcType.List);
			for (Object e : (List) obj) {
				ret.add(_do_encode(e));
			}
			return ret;
		}
		if (String.class.isAssignableFrom(obj.getClass())) {
			byte[] bs = Convert.toBytes((String) obj, charset());
			return new BcData(BcType.Bytes, bs);
		}
		if (Character.class.isAssignableFrom(obj.getClass())) {
			return new BcData(BcType.Bytes,
					new byte[] { (byte) ((Character) obj).charValue() });
		}
		if (Integer.class.isAssignableFrom(obj.getClass())) {
			return new BcData(BcType.Int, obj);
		}
		if (Long.class.isAssignableFrom(obj.getClass())) {
			return new BcData(BcType.Int, obj);
		}
		if (Map.class.isAssignableFrom(obj.getClass())) {
			BcList ret = new BcList(BcType.Map);

			for (Object key : ((Map) obj).keySet()) {
				Object val = ((Map) obj).get(key);
				ret.add(key.toString(), _do_encode(val));
			}
			return ret;
		}
		return null;
	}

	// =====================================================
	// 					DECODE
	// =====================================================

	private BcObject _decode(byte char0, ByteArrayInputStream stream) {
		byte cc = (byte) ((char0 == (byte) -1) ? stream.read() : char0);

		switch ((char) cc) {
		case 'l':
			return _decodeList(stream);
		case 'd':
			return _decodeMap(stream);
		case 'i':
			return _decodeInt(stream);
		default:
			return _decodeBytes(cc, stream);
		}
	}

	private BcData _decodeBytes(byte char0, ByteArrayInputStream ben) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		String strSize = "" + (char) char0;
		boolean isSizeReached = false;

		int curr;
		int size = -1;
		int countRead = 0;
		while ((curr = ben.read()) != -1) {
			byte cc = (byte) curr;
			if (!isSizeReached && ((char) curr) == ':') {
				isSizeReached = true;
				size = Integer.parseInt(strSize);
				if (size == 0)
					return new BcData(BcType.Bytes, new byte[0]);
				continue;
			}
			if (!isSizeReached) {
				strSize += (char) curr;
			} else {
				bos.write(cc);
				countRead++;

				if (countRead == size) {
					return new BcData(BcType.Bytes, bos.toByteArray());
				}
			}
		}

		// error
		return null;
	}

	private BcData _decodeInt(ByteArrayInputStream ben) {
		String strNum = "";

		int curr;
		while ((curr = ben.read()) != -1) {
			char cc = (char) curr;
			if (cc == 'e') {
				return new BcData(BcType.Int, Long.parseLong(strNum));
			}
			strNum += cc;
		}

		// error
		return null;
	}

	private BcList _decodeList(ByteArrayInputStream ben) {
		BcList ret = new BcList(BcType.List);

		int curr;
		while ((curr = ben.read()) != -1) {
			byte cc = (byte) curr;
			if (((char) cc) == 'e') {
				return ret;
			}
			ret.add(_decode(cc, ben));
		}

		// error
		return null;
	}

	private BcList _decodeMap(ByteArrayInputStream ben) {
		BcList ret = new BcList(BcType.Map);

		int curr;
		while ((curr = ben.read()) != -1) {
			byte cc = (byte) curr;
			if (((char) cc) == 'e') {
				return ret;
			}
			String key = _decodeBytes(cc, ben).asString();
			ret.add(key, _decode((byte) -1, ben));
		}

		// error
		return null;
	}

	// =====================================================
	
	private static Charset charset(){
		return Utils.charsetASCII();
	}

	private static byte[] encodeString(String str) {
		return encodeBytes(Convert.toBytes(str, charset()));
	}

	private static byte[] encodeBytes(byte[] data) {
		ByteArrayOutputStream ret = new ByteArrayOutputStream();
		byte[] indexToWrite = Convert.toBytes(String.valueOf(data.length), charset());
		ret.write(indexToWrite, 0, indexToWrite.length);
		ret.write(':');
		ret.write(data, 0, data.length);

		return ret.toByteArray();
	}

	private static byte[] encodeLong(Number data) {
		ByteArrayOutputStream ret = new ByteArrayOutputStream();
		
		byte[] toWrite = Convert.toBytes(String.valueOf(data), charset());
		
		ret.write('i');
		ret.write(toWrite, 0, toWrite.length);
		ret.write('e');
		
		return ret.toByteArray();
	}
	
	@Override
	public String toString() {
		return toObject().toString();
	}

}
