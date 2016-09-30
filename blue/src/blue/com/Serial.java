package blue.com;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import blue.lang.Assert;
import blue.util.Convert;
import blue.util.NetUtils;
import blue.util.Primitives;
import blue.util.Utils;

/**
 * Net parser for generic simple bean objects
 * 
 * @author Matteo
 *
 */
public final class Serial {
	
	// =====================================
	
	public interface ISerial {
		byte[] toBytes();
		void fromBytes(byte[] data);
	}
	
	// =====================================

	public static final byte TYPE_NULL = 0;
	
	public static final byte TYPE_OBJECT = 1;
	public static final byte TYPE_STRING = 2;
	public static final byte TYPE_INT = 3;
	public static final byte TYPE_BYTE = 4;
	public static final byte TYPE_LONG = 5;
	public static final byte TYPE_CHAR = 6;
	public static final byte TYPE_SHORT = 7;
	public static final byte TYPE_FLOAT = 8;
	public static final byte TYPE_DOUBLE = 9;

	public static final byte TYPE_ARRAY = 10;
	public static final byte TYPE_BIG_ARRAY = 11;
	public static final byte TYPE_PRIMITIVE_ARRAY = 20;
	public static final byte TYPE_BIG_PRIMITIVE_ARRAY = 21;

	public static final byte TYPE_BIG_STRING = 102;

	public static final byte TYPE_SERIALIZED = 99;
	
	private static final Map<String,Class<?>> primitives = new HashMap<String,Class<?>>();
	private static final Map<Byte, Class<?>> primitives_byte = new HashMap<Byte,Class<?>>();
	static {
		primitives.put("char", char.class);
		primitives.put("byte", byte.class);
		primitives.put("short", short.class);
		primitives.put("int", int.class);
		primitives.put("long", long.class);
		primitives.put("float", float.class);
		primitives.put("double", double.class);

		primitives_byte.put(TYPE_CHAR, char.class);
		primitives_byte.put(TYPE_BYTE, byte.class);
		primitives_byte.put(TYPE_SHORT, short.class);
		primitives_byte.put(TYPE_INT, int.class);
		primitives_byte.put(TYPE_LONG, long.class);
		primitives_byte.put(TYPE_FLOAT, float.class);
		primitives_byte.put(TYPE_DOUBLE, double.class);
	}
	
	// =====================================
	
	private Serial(){}
	
	// =====================================
	
	public static final <T> byte[] encode(T bean) throws ParseException{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		produce(out, bean);
		return out.toByteArray();
	}
	
	@SuppressWarnings("unchecked")
	public static final <T> T decode(byte[] data) throws ParseException{
		ByteBuffer buff = ByteBuffer.wrap(data);
		Object o = parse(buff);
		return (T) o;
	}
	
	// =====================================

	private static Object parse(ByteBuffer buff) throws ParseException {
		try{
			byte type = buff.get();
			
			if(type == TYPE_NULL) {
				return null;
			}
			
			// primitive
			if(type >= 3 && type <= 9){
				return parsePrimitive(buff, type);
			}
			
			if(type == TYPE_STRING) {
				return readString(buff);
			}
			
			if(type == TYPE_BIG_STRING) {
				return readBigString(buff);
			}
	
			if(type == TYPE_ARRAY || type == TYPE_BIG_ARRAY) {
				String typeName = readString(buff);
				Class<?> targetClass = Class.forName(typeName);
				
				int size = type == TYPE_ARRAY ? (buff.get() & 0xFF) : (buff.getInt());
				
				Object obj = Array.newInstance(targetClass, size);
				for(int i=0; i<size; i++){
				    try{
					Object val = parse(buff);
					val = handleAutoboxing(val,targetClass);
					Array.set(obj, i, val);
				    }catch(Exception e){
				        throw new RuntimeException("Unable to parse array line " + i, e);
				    }
				}
				return obj;
			}
	
			if(type == TYPE_PRIMITIVE_ARRAY || type == TYPE_BIG_PRIMITIVE_ARRAY) {
				byte arrType = buff.get();
				Class<?> targetClass = primitives_byte.get(arrType);
				
				int size = type == TYPE_PRIMITIVE_ARRAY ? (buff.get() & 0xFF) : (buff.getInt());
				Object obj = Array.newInstance(targetClass, size);
				
				// 1 byte data
				if(arrType == TYPE_BYTE) {
					byte[] data = new byte[size];
					buff.get(data, 0, size);
					return data;
				}
				
				// 1 byte data (char)
				if(arrType == TYPE_CHAR) {
					byte[] data = new byte[size];
					buff.get(data, 0, size);
					for(int i=0 ;i<data.length; i++){
						Array.set(obj, i, (char)data[i]);
					}
					return data;
				}
				
				// 2 byte data (short)
				if(arrType == TYPE_SHORT) {
					byte[] data = new byte[size*2];
					buff.get(data, 0, data.length);
					short[] shorts = new short[size];
					ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
					return shorts;
				}
				
				// 4 byte data (int)
				if(arrType == TYPE_INT || arrType == TYPE_FLOAT) {
					byte[] data = new byte[size*4];
					buff.get(data, 0, data.length);
					if(arrType == TYPE_INT){
						int[] ret = new int[size];
						ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(ret);
						return ret;
					}
					if(arrType == TYPE_FLOAT){
						float[] ret = new float[size];
						ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().get(ret);
						return ret;
					}
				}
				
				// 8 byte data (long/double/float)
				if(arrType == TYPE_DOUBLE || arrType == TYPE_LONG) {
					byte[] data = new byte[size*8];
					buff.get(data, 0, data.length);
					if(arrType == TYPE_LONG){
						long[] ret = new long[size];
						ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asLongBuffer().get(ret);
						return ret;
					}
					if(arrType == TYPE_DOUBLE){
						double[] ret = new double[size];
						ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asDoubleBuffer().get(ret);
						return ret;
					}
				}
				throw new ParseException("Unable to parse primitive type " + arrType, buff.position());
			}
			
			// serialized
			if(type == TYPE_SERIALIZED) {
				byte[] nsize = new byte[4];
				buff.get(nsize);
				int size = Convert.toInt32(nsize);
				byte[] data = new byte[size];
				buff.get(data, 0, data.length);
				return deserialize(data);
			}
	
			// object
			String className = readString(buff);
			Class<?> targetClass = Class.forName(className);
			Object obj = getDefaultInstance(targetClass);
			
			// custom constructor
			if(obj instanceof ISerial){
				byte[] pars = new byte[buff.getInt()];
				buff.get(pars);
				((ISerial)obj).fromBytes(pars);
				return obj;
			}
			
			int nParams = buff.get();
			for(int i=0; i<nParams; i++){
				String fieldName = readString(buff);
				Object value = parse(buff);
				
				// set
				Class<?> ww = targetClass;
				Field f = null;
				while(ww.getSuperclass() != null){
					try{
						f = ww.getDeclaredField(fieldName);
					}catch(NoSuchFieldException ff){
						ww = ww.getSuperclass();
						continue;
					}
					break;
				}
				f.setAccessible(true);
				
				// special autoboxing case
				value = handleAutoboxing(value, f.getType());
				if(obj != null){
					f.set(obj, value);
				}
			}
			return obj;
		} catch (Exception e) {
//			log.error(e);
			e.printStackTrace();
			throw new ParseException("Unable to parse bean", buff.position());
		}
	}

	private static void produce(ByteArrayOutputStream out, Object bean) throws ParseException{
		try{
			if(bean == null){
				out.write(TYPE_NULL);
				return;
			}
			
			// bean name
			Class<?> type = bean.getClass();
			String name = type.getName();
			
			// byte
			if(type.equals(Byte.class) || type.equals(byte.class)){
				out.write(TYPE_BYTE);
				out.write((Byte)bean);
				return;
			}
			
			// char
			if(type.equals(Character.class) || type.equals(char.class)){
				out.write(TYPE_CHAR);
				out.write((Character)bean);
				return;
			}
			
			// int
			if(type.equals(Integer.class) || type.equals(int.class)){
				out.write(TYPE_INT);
				out.write(Convert.toBytes((Integer)bean));
				return;
			}
			
			// long
			if(type.equals(Long.class) || type.equals(long.class)){
				out.write(TYPE_LONG);
				out.write(Convert.toBytes((Long)bean));
				return;
			}
			
			// double
			if(type.equals(Double.class) || type.equals(double.class)){
				out.write(TYPE_DOUBLE);
				out.write(Convert.toBytes((Double)bean));
				return;
			}
			
			// float
			if(type.equals(Float.class) || type.equals(float.class)){
				out.write(TYPE_FLOAT);
				out.write(Convert.toBytes((Float)bean));
				return;
			}
			
			// string
			if(type.equals(String.class)){
				if(((String)bean).length() >= 256){
					out.write(TYPE_BIG_STRING);
					writeWithBigSize(out, NetUtils.asBytes((String)bean));
				}else{
					out.write(TYPE_STRING);
					writeWithSize(out, NetUtils.asBytes((String)bean));
				}
				return;
			}
			
			// primitive array
			if(type.isArray() && (type.getComponentType().isPrimitive() || Primitives.isPrimitiveWrapper(type.getComponentType()))){
				
				// size
				int size = Array.getLength(bean);
				
				boolean isBig = size > 255;
				out.write(isBig?TYPE_BIG_PRIMITIVE_ARRAY:TYPE_PRIMITIVE_ARRAY);

				// type
				Class<?> arrType = type.getComponentType();
				if(Primitives.isPrimitiveWrapper(arrType)){
					Class<?> newType = Primitives.getPrimitiveType(arrType);
					
					// convert array
					Object convArr = Array.newInstance(newType, size);
					for(int i=0; i<size; i++){
						Array.set(convArr, i, Array.get(bean, i));
					}
					bean = convArr;					
					arrType = newType;
					
				}
				
				// write type
				byte typeCode = Utils.getFirstKeyByValue(primitives_byte, arrType);
				out.write(typeCode);
				if(isBig){
					out.write(Convert.toBytes(size));
				}else{
					out.write((byte)size);
				}
				
				// 1. byte array
				if(typeCode == TYPE_BYTE){
					out.write((byte[]) bean, 0, size);
					return;
				}
				if(typeCode == TYPE_CHAR){
					ByteBuffer conv = ByteBuffer.allocate(size);
					conv.asCharBuffer().put((char[]) bean);
					out.write(conv.array());
					return;
				}
				if(typeCode == TYPE_SHORT){
					ByteBuffer conv = ByteBuffer.allocate(size*2);
					conv.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put((short[]) bean);
					out.write(conv.array());
					return;
				}
				if(typeCode == TYPE_INT){
					ByteBuffer conv = ByteBuffer.allocate(size*4);
					conv.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().put((int[]) bean);
					out.write(conv.array());
					return;
				}
				if(typeCode == TYPE_LONG){
					ByteBuffer conv = ByteBuffer.allocate(size*8);
					conv.order(ByteOrder.LITTLE_ENDIAN).asLongBuffer().put((long[]) bean);
					out.write(conv.array());
					return;
				}
				if(typeCode == TYPE_DOUBLE){
					ByteBuffer conv = ByteBuffer.allocate(size*2);
					conv.order(ByteOrder.LITTLE_ENDIAN).asDoubleBuffer().put((double[]) bean);
					out.write(conv.array());
					return;
				}
				if(typeCode == TYPE_FLOAT){
					ByteBuffer conv = ByteBuffer.allocate(size*2);
					conv.order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().put((float[]) bean);
					out.write(conv.array());
					return;
				}
				throw new ParseException("Unable to parse primitive class " + arrType,  out.size());
			}
			
			// generic array
			if(type.isArray()){
				// size
				int size = Array.getLength(bean);
				boolean isBig = size > 255;
				out.write(isBig?TYPE_BIG_ARRAY:TYPE_ARRAY);
				
				// type
				Class<?> arrType = type.getComponentType();
				writeWithSize(out, NetUtils.asBytes(arrType.getName()));
				
				if(isBig){
					out.write(Convert.toBytes(size));
				}else{
					out.write((byte)size);
				}
				
				// elements
				for(int i=0; i<size; i++){
					Object el = Array.get(bean, i);
					produce(out,el);
				}
				return;
			}
			
			// is serializable?
			if(getDefaultInstance(type) == null) {
				// use default serializer
				out.write(TYPE_SERIALIZED);
				byte[] enc = serialize(bean).toByteArray();
				out.write(Convert.toBytes(enc.length));
				out.write(enc);
				return;
			}
			
			// others
			out.write(TYPE_OBJECT);
			
			// parameters
			
			ArrayList<Field> allFields = new ArrayList<Field>();
			Class<?> ww = type;
			while(ww.getSuperclass() != null){
				for(Field f:ww.getDeclaredFields()){
					allFields.add(f);
				}
				ww = ww.getSuperclass();
			}
			
			// parsing
			
			// writing
			writeWithSize(out, NetUtils.asBytes(name));
			
			// custom parser
			if(ISerial.class.isAssignableFrom(type)){
				byte[] enc = ((ISerial)bean).toBytes();
				out.write(Convert.toBytes(enc.length));
				out.write(enc);
				return;
			}
			
			// calc total params
			int nFields = 0;
			for(Field f:allFields){
				// ignore static
				if(Modifier.isStatic(f.getModifiers())){
					continue;
				}
				nFields++;
			}
			
			// how many parameters?
			out.write((byte)nFields);
			for(Field f:allFields){
				// ignore static
				if(Modifier.isStatic(f.getModifiers())){
					continue;
				}
				
				// value
				f.setAccessible(true);
				Object val = f.get(bean);
				writeWithSize(out, NetUtils.asBytes(f.getName()));
				produce(out,val);
			}
		} catch (Exception e) {
//			log.error(e);
			throw new ParseException("Unable to parse bean", out.size());
		}
	}
	
	// =====================================
	
	private static Object parsePrimitive(ByteBuffer buff, byte type){
		if(type == TYPE_INT) {
			int val = buff.getInt();
			return val;
		}

		if(type == TYPE_LONG) {
			long val = buff.getLong();
			return val;
		}
		
		if(type == TYPE_BYTE) {
			byte val = buff.get();
			return val;
		}
		
		if(type == TYPE_CHAR) {
			byte val = buff.get();
			return (char)val;
		}
		
		if(type == TYPE_DOUBLE) {
			double val = buff.getDouble();
			return val;
		}
		
		if(type == TYPE_FLOAT) {
			float val = buff.getFloat();
			return val;
		}
		
		if(type == TYPE_SHORT) {
			float val = buff.getShort();
			return val;
		}
		
		return null;
	}
	
	private static void writeWithBigSize(ByteArrayOutputStream out, byte[] data){
		int size = data.length;
		byte[] conv = Convert.toBytes(size);
		out.write(conv,0,conv.length);
		out.write(data, 0, data.length);
	}
	
	private static void writeWithSize(ByteArrayOutputStream out, byte[] data){
		byte size = (byte) data.length;
		out.write(size);
		out.write(data, 0, data.length);
	}
	
	private static String readBigString(ByteBuffer buff){
		byte[] nsize = new byte[4];
		buff.get(nsize);
		int size = Convert.toInt32(nsize);
		byte[] strBuff = new byte[size];
		buff.get(strBuff);
		return NetUtils.asBytes(strBuff);
	}
	
	private static String readString(ByteBuffer buff){
		byte size = buff.get();
		byte[] strBuff = new byte[size & 0xFF ];
		buff.get(strBuff);
		return NetUtils.asBytes(strBuff);
	}
	
	private static Object handleAutoboxing(Object arr, Class<?> targetType){
		if(arr == null){
			// not permitted null value in primitive
			if(targetType.isPrimitive()){
				return Primitives.getDefault(targetType);
			}
			return null;
		}
		if(targetType.isArray()){
			if(arr.getClass().isArray() && arr.getClass().getComponentType().isPrimitive() && !targetType.getComponentType().isPrimitive()){
				// simulate autoboxing
				int size = Array.getLength(arr);
				Object destArr = Array.newInstance(targetType.getComponentType(), size);
				for(int j=0; j<size; j++){
					Array.set(destArr, j, Array.get(arr, j));
				}
				arr = destArr;
			}
		}
		return arr;
	}
	
	private static Object getDefaultInstance(Class<?> type){
		int minParams = Integer.MAX_VALUE;
		Constructor<?> cmin = null;
		for(Constructor<?> c:type.getDeclaredConstructors()){
			if(c.getParameterTypes().length < minParams){
				minParams = c.getParameterTypes().length;
				cmin = c;
			}
		}
		Assert.notNull(cmin);
		Class<?>[] params = cmin.getParameterTypes();
		Object[] initargs = new Object[params.length];
		int i=0;
		for(Class<?> p:params){
			if(p.isPrimitive()){
				initargs[i] = Primitives.getDefault(p);
			}else{
				initargs[i] = null;
			}
			i++;
		}
		try {
			cmin.setAccessible(true);
			return cmin.newInstance(initargs);
		} catch (Exception e) {
			//log.error(e);
//			log.warning("Unable to instantiate " + type.getName());
			return null;
		}
	}
	
	private static Object deserialize(byte[] data) throws ParseException{
		ObjectInputStream str;
		try {
			str = new ObjectInputStream(new ByteArrayInputStream(data));
			return str.readObject();
		} catch (IOException e) {
			e.printStackTrace();
			throw new ParseException(e.getMessage(), 0);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new ParseException(e.getMessage(), 0);
		}
	}
	
	private static ByteArrayOutputStream serialize(Object obj) throws ParseException{
		ByteArrayOutputStream res = new ByteArrayOutputStream();
		try {
			ObjectOutputStream serialEnc = new ObjectOutputStream(res);
			serialEnc.writeObject(obj);
			return res;
		} catch (IOException e) {
			e.printStackTrace();
			throw new ParseException(e.getMessage(), 0);
		}
	}
//
//	
//	
//	public static void main(String[] args) throws ParseException {
//		byte[] enc = Serial.encode(new Bencode("ciao"));
//		System.out.println(enc.length);
//		System.out.println(Serial.decode(enc));
//	}
//	
}
