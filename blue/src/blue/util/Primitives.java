package blue.util;

import java.util.HashMap;
import java.util.Map;

public final class Primitives {
	private Primitives(){}
	
	// =====================================================================
	
	private static final Map<Class<?>,Class<?>> primitives = new HashMap<Class<?>,Class<?>>();
	private static final Map<Class<?>, Object> DEFAULTS = new HashMap<Class<?>, Object>(16);
	private static final Map<Class<?>, Class<?>> primitives_wrapper = new HashMap<Class<?>,Class<?>>();

	static {
		DEFAULTS.put(boolean.class, false);
		DEFAULTS.put(Boolean.class, false);

		DEFAULTS.put(char.class, '\0');
		DEFAULTS.put(Character.class, '\0');

		DEFAULTS.put(byte.class, (byte) 0);
		DEFAULTS.put(Byte.class, (byte) 0);

		DEFAULTS.put(short.class, (short) 0);
		DEFAULTS.put(Short.class, (short) 0);

		DEFAULTS.put(int.class, 0);
		DEFAULTS.put(Integer.class, 0);

		DEFAULTS.put(long.class, 0L);
		DEFAULTS.put(Long.class, 0L);

		DEFAULTS.put(float.class, 0f);
		DEFAULTS.put(Float.class, 0f);

		DEFAULTS.put(double.class, 0d);
		DEFAULTS.put(Double.class, 0d);
	}

	static {
		primitives.put(char.class, Character.class);
		primitives.put(byte.class, Byte.class);
		primitives.put(short.class, Short.class);
		primitives.put(int.class, Integer.class);
		primitives.put(long.class, Long.class);
		primitives.put(float.class, Float.class);
		primitives.put(double.class, Double.class);
		primitives.put(boolean.class, Boolean.class);

		primitives_wrapper.put(Character.class, char.class);
		primitives_wrapper.put(Byte.class, byte.class);
		primitives_wrapper.put(Short.class, short.class);
		primitives_wrapper.put(Integer.class, int.class);
		primitives_wrapper.put(Long.class, long.class);
		primitives_wrapper.put(Float.class, float.class);
		primitives_wrapper.put(Double.class, double.class);
		primitives_wrapper.put(Boolean.class, boolean.class);
	}
	
	// =====================================================================
	
	public static final boolean isPrimitive(Class<?> c){
		return primitives.containsKey(c);
	}

	public static final boolean isPrimitiveWrapper(Class<?> c){
		return primitives_wrapper.containsKey(c);
	}
	
	@SuppressWarnings("unchecked")
	public static final <K,T> Class<T> getPrimitiveType(Class<K> wrapperClass){
		return (Class<T>) primitives_wrapper.get(wrapperClass);
	}
	
	@SuppressWarnings("unchecked")
	public static final <K,T> Class<T> getWrapperType(Class<K> primitiveType){
		return (Class<T>) primitives.get(primitiveType);
	}

	/**
	 * Returns the default value of {@code type} as defined by JLS --- {@code 0}
	 * for numbers, {@code
	 * false} for {@code boolean} and {@code '\0'} for {@code char}. For
	 * non-primitive types and {@code void}, null is returned.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getDefault(Class<T> type) {
		return (T) DEFAULTS.get(type);
	}
}
