package blue.lang;

public class Assert {
	
	/**
	 * Assert non-nullable object
	 * 
	 * @param notNull
	 */
	public static void notNull(Object notNull){
		if(notNull == null){
			throw new AssertionError();
		}
	}
	
	/**
	 * Assert null object
	 * 
	 * @param notNull
	 */
	public static void isNull(Object notNull){
		if(notNull != null){
			throw new AssertionError();
		}
	}
	
	public static void major(int major, int minor){
		major(major,minor,false);
	}

	public static void major(int major, int minor, boolean strict) {
		minor(minor,major,strict);
	}
	
	public static void minor(int minor, int major){
		minor(minor,major,false);
	}

	public static void minor(int minor, int major, boolean strict) {
		if(major < minor || (strict && major == minor)){
			throw new AssertionError();
		}
	}

	public static void test(boolean conditon) {
		if(!conditon){
			throw new AssertionError();
		}
	}
}
