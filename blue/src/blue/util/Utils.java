package blue.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class Utils {
	// random functions
	private static final Random RND = new Random(System.currentTimeMillis());
	
	// utility class cannot be instantiated
	private Utils(){
		
	}
//	
//	/**
//	 * Decode GZIP
//	 * @param file
//	 * @return
//	 */
//	public static byte[] gzipDecode(byte[] file){
//		return Encoding.getDecoder(Encoding.ZIP).decode(file);
//	}
//	
//	/**
//	 * Encode GZIP
//	 * @param file
//	 * @return
//	 */
//	public static byte[] gzipEncode(byte[] file){
//		return Encoding.getEncoder(Encoding.ZIP).encode(file);
//	}

	/**
	 * Random function
	 * @return
	 */
	public static Random rnd(){
		return RND;
	}
	
	/**
	 * random INT
	 * 
	 * @param max
	 * @return
	 */
	public static int rndInt() {
		return RND.nextInt();
	}
	
	/**
	 * random INT
	 * 
	 * @param max
	 * @return
	 */
	public static long rndLong() {
		return RND.nextLong();
	}
	
	/**
	 * random INT
	 * 
	 * @param max
	 * @return
	 */
	public static int rndInt(int max) {
		return RND.nextInt(max);
	}

	/**
	 * Random byte
	 * 
	 * @return
	 */
	public static byte[] rndBytes(int size) {
		byte[] ret = new byte[size];
		RND.nextBytes(ret);
		return ret;
	}

	/**
	 * Calculate time delay in standard connection
	 * 
	 * @param base
	 * @param nTry
	 * @return
	 */
	public static int timeDelay(int base, int nTry) {
		return (int) (Math.pow(2, nTry) * base);
	}
	
	/**
	 * Read byte content
	 * 
	 * @param bytes
	 * @return
	 */
	public static String read(byte[] bytes){
		String ret= "";
		int max = 100;
		for(byte m:bytes){
			ret += ((int)m)  + " ";
			if(max-- < 0){
				break;
			}
		}
		return ret;
	}
	
	/**
	 * Print byte array as int
	 * @param strip 
	 */
	public static String printAsInt(byte[] bytes, int strip){
		String ret = "";
		int count = 0;
		int iter = 0;
		for(byte m:bytes){
			ret += ((int)m + " ");
			if(count++>100){
				ret += '\n';
				count=0;
			}
			if(strip > 0 && iter++ >= strip){
				return ret + " ...";
			}
		}
		return ret;
	}
	
	/**
	 * Calculate file hash
	 * @return
	 */
	public static byte[] calcHash(byte[] data){
		return Convert.toSHA1(data);
	}

	/**
	 * Calculate file extension without dot
	 * 
	 * @param file
	 * @return
	 */
	public static String getExtension(File file) {
        String filename = file.getName();
        return getExtension(filename);
	}
    /**
     * Calculate extension on generic path
     * 
     * @param file
     * @return
     */
    public static String getExtension(String path) {
        String extension = "";
        int dotPos = path.lastIndexOf(".");
        if (dotPos >= 0) {
            extension = path.substring(dotPos+1);
        }
        return extension.toLowerCase();
    }
    
    public static String getFileNameInPath(String path) {
        String fname = "";
        int dotPos = path.lastIndexOf("/");
        int dotPos2 = path.lastIndexOf("\\");
        if(dotPos2 > dotPos){
        	dotPos = dotPos2;
        }
        if (dotPos >= 0) {
        	fname = path.substring(dotPos+1);
        }
        return fname;
    }

    
    public static String getPath(String path) {
    	String fname = getFileNameInPath(path);
    	if(fname.length() == 0){
    		return path;
    	}
    	return path.substring(0, path.length()-fname.length());
    }

	/**
	 * Default charset (ASCII7)
	 * 
	 * @return
	 */
	public static Charset charsetDef(){
		return charsetASCII7();
	}
	
	/**
	 * Charset US-ASCII 	
	 * 
	 * Seven-bit ASCII, a.k.a. ISO646-US, a.k.a. the Basic Latin block of the Unicode
	 * character set
	 * @return
	 */
	public static Charset charsetASCII(){
		return Charset.forName("US-ASCII");
	}
	
	/**
	 * Charset ASCII7
	 * 
	 * @return
	 */
	public static Charset charsetASCII7(){
		return Charset.forName("ASCII7");
	}
	
	/**
	 * Charset ISO-8859-1   	
	 * 
	 * ISO Latin Alphabet No. 1, a.k.a. ISO-LATIN-1
	 * 
	 * @return
	 */
	public static Charset charsetISO8859(){
		return Charset.forName("ISO-8859-1");
	}

	/**
	 * Charset UTF-8 	
	 * 
	 * Eight-bit UCS Transformation Format
	 * @return
	 */
	public static Charset charsetUTF8(){
		return Charset.forName("UTF-8");
	}

	/**
	 * Charset UTF-16BE
	 * 
	 * Sixteen-bit UCS Transformation Format, big-endian byte order
	 * 
	 * @return
	 */
	public static Charset charsetUTF16be(){
		return Charset.forName("UTF-16BE");
	}
	

	/**
	 * Charset UTF-16LE
	 * 
	 * Sixteen-bit UCS Transformation Format, little-endian byte order
	 * 
	 * @return
	 */
	public static Charset charsetUTF16le(){
		return Charset.forName("UTF-16LE");
	}

	/**
	 * Charset UTF-16
	 * 
	 * Sixteen-bit UCS Transformation Format, byte order identified by an optional byte-order mark
	 * @return
	 */
	public static Charset charsetUTF16(){
		return Charset.forName("UTF-16");
	}
	
	/**
	 * Reverse search on map key-value pairs retrieving key by value
	 * 
	 * @param map
	 * @param value
	 * @return
	 */
	public static <K,V> K getFirstKeyByValue(Map<K,V> map, V value){
		for(K k:map.keySet()){
			if(map.get(k).equals(value)){
				return k;
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static <K> Class<K> getClass(String className){
		try{
			return (Class<K>) ClassLoader.getSystemClassLoader().loadClass(className);
		} catch(ClassNotFoundException e){
			return null; // not found
		}
	}

	/**
	 * Create new instance of class with specified parameters
	 * 
	 * @param objClass
	 * @param params
	 * @return
	 */
	public static <K> K newInstance(Class<K> objClass, Object ... params) {
		// get constructor types
		Class<?>[] types = new Class<?>[params.length];
		for(int i=0 ;i<types.length; i++){
			if(params[i] == null){
				types[i] = Object.class;
				continue;
			}
			types[i] = params[i].getClass();
		}
		try {
			Constructor<K> constr;
			try{
				constr = objClass.getDeclaredConstructor(types);
			} catch(NoSuchMethodException e){
				constr = getCompatibleConstructor(objClass,types); // 
			}
			constr.setAccessible(true);
			return constr.newInstance(params);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get a compatible constructor to the supplied parameter types.
	 *
	 * @param clazz
	 *            the class which we want to construct
	 * @param parameterTypes
	 *            the types required of the constructor
	 *
	 * @return a compatible constructor or null if none exists
	 */
	@SuppressWarnings("unchecked")
    public static <T> Constructor<T> getCompatibleConstructor(Class<T> clazz, Class<?>[] parameterTypes) {
		ArrayList<Constructor<?>> listConstr = new ArrayList<Constructor<?>>();
		listConstr.addAll(Arrays.asList(clazz.getConstructors()));
		listConstr.addAll(Arrays.asList(clazz.getDeclaredConstructors()));
		for (Constructor<?> csr:listConstr) {
			if (csr.getParameterTypes().length == (parameterTypes != null ? parameterTypes.length : 0)) {
				// If we have the same number of parameters there is a shot that
				// we have a compatible
				// constructor
				Class<?>[] constructorTypes = csr.getParameterTypes();
				boolean isCompatible = true;
				for (int j = 0; j < (parameterTypes != null ? parameterTypes.length : 0); j++) {
					if (!constructorTypes[j].isAssignableFrom(parameterTypes[j])) {
						// The type is not assignment compatible, however
						// we might be able to coerce from a basic type to a
						// boxed type
						if (constructorTypes[j].isPrimitive()) {
							if (!isAssignablePrimitiveToBoxed(constructorTypes[j], parameterTypes[j])) {
								isCompatible = false;
								break;
							}
						}
					}
				}
				if (isCompatible) {
					return (Constructor<T>) csr;
				}
			}
		}
		return null;
	}

	/**
	 * <p>
	 * Checks if a primitive type is assignable with a boxed type.
	 * </p>
	 *
	 * @param primitive
	 *            a primitive class type
	 * @param boxed
	 *            a boxed class type
	 *
	 * @return true if primitive and boxed are assignment compatible
	 */
	private static boolean isAssignablePrimitiveToBoxed(Class<?> primitive, Class<?> boxed) {
		if (primitive.equals(java.lang.Boolean.TYPE)) {
			if (boxed.equals(java.lang.Boolean.class))
				return true;
			else
				return false;
		} else {
			if (primitive.equals(java.lang.Byte.TYPE)) {
				if (boxed.equals(java.lang.Byte.class))
					return true;
				else
					return false;
			} else {
				if (primitive.equals(java.lang.Character.TYPE)) {
					if (boxed.equals(java.lang.Character.class))
						return true;
					else
						return false;
				} else {
					if (primitive.equals(java.lang.Double.TYPE)) {
						if (boxed.equals(java.lang.Double.class))
							return true;
						else
							return false;
					} else {
						if (primitive.equals(java.lang.Float.TYPE)) {
							if (boxed.equals(java.lang.Float.class))
								return true;
							else
								return false;
						} else {
							if (primitive.equals(java.lang.Integer.TYPE)) {
								if (boxed.equals(java.lang.Integer.class))
									return true;
								else
									return false;
							} else {
								if (primitive.equals(java.lang.Long.TYPE)) {
									if (boxed.equals(java.lang.Long.class))
										return true;
									else
										return false;
								} else {
									if (primitive.equals(java.lang.Short.TYPE)) {
										if (boxed.equals(java.lang.Short.class))
											return true;
										else
											return false;
									}
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Split string after first occurrence of search word
	 * 
	 * @param string target string to split
	 * @param search word split
	 * @return
	 */
	public static String splitAfter(String string, String search) {
		return string.substring(string.indexOf(search) + search.length());
	}

	public static String splitAfter(String string, String search, boolean searchLast) {
		int idx = searchLast?string.lastIndexOf(search) :  string.lastIndexOf(search);
		if(idx == -1){
			return string;
		}
		return string.substring(idx + search.length());
	}

	/**
	 * Split string before first occurrence of search word
	 * 
	 * @param string target string to split
	 * @param search word split
	 * @return
	 */
	public static String splitBefore(String string, String search) {
		int idx = string.indexOf(search);
		if(idx == -1) {
			return string;
		}
		return string.substring(0, idx);
	}

	/**
	 * Split string between first occurrence of search words
	 * 
	 * @param string target string to split
	 * @param bound1 start boundary word split
	 * @param bound2 end boundary word split
	 * @return
	 */
	public static String splitBetween(String string, String bound1, String bound2) {
		int off = string.indexOf(bound1)+bound1.length();
		int idx = string.substring(off).indexOf(bound2);
		if(idx == -1) {
			return string.substring(off);
		}
		return string.substring(off, idx + off);
	}

	public static InputStream getResource(String src) {
		try {
		    URL res = ClassLoader.getSystemResource(src);
		    if(res == null){
		        return null;
		    }
			return res.openStream();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Load properties file from system resource
	 * 
	 * @param src
	 * @return
	 */
	public static Properties getProperty(String src) {
		return getProperty(getResource(src));
	}

	/**
	 * Load properties file from input stream
	 * @param file
	 * @return
	 */
    public static Properties getProperty(File file) {
        FileInputStream rdr = null;
        try {
            Properties prop = new Properties();
            rdr = new FileInputStream(file);
            prop.load(rdr);
            return prop;
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }finally {
            try {
                rdr.close();
            } catch (Exception e) {
            }
        }
    }

    public static Properties getProperty(InputStream data) {
        try {
            Properties prop = new Properties();
            prop.load(data);
            return prop;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

	public static String subPath(File path, File superPath) {
		try {
			return path.getCanonicalPath().substring(superPath.getCanonicalPath().length()+1);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String find(String string, String match){
	    Pattern p = Pattern.compile(match);
	    Matcher m = p.matcher(string);

	    if (m.find()) {
	        return m.group(1);
	    }
	    return null;
	}
	/**
	 * Clone array trimming each element
	 * @param arr
	 * @return
	 */
	public static String[] trimEach(String[] arr) {
		if(arr == null) {
			return null;
		}
		String[] ret = new String[arr.length];
		for(int r=0; r<ret.length; r++) {
			ret[r] = arr[r].trim();
		}
		return ret;
	}

	/**
	 * Delete file/directory recursive
	 * 
	 * @param dir
	 */
    public static boolean recursiveDelete(File dir){
        if(!dir.exists()){
            return true;
        }
        if(dir.isFile()) {
            return dir.delete();
        }else{
            for(File f:dir.listFiles()) {
                if(!recursiveDelete(f)){
                    return false;
                }
            }
            return dir.delete();
        }
    }
	
	public static URI safeResolve(URI parent, String path){
        if(path == null){
            return parent;
        }
        try{
            
            return parent.resolve(path);
        } catch (Exception e) {
            try {
                return parent.resolve(URLEncoder.encode(path, "UTF-8").replaceAll("%2F", "/"));
            } catch (UnsupportedEncodingException ex) {
                throw new RuntimeException(ex);
            }
        }
	}
	
	public static boolean equals(URL url1, URL url2){
        String u1 = url1.toString();
        String u2 = url2.toString();
        while(u1.endsWith("/")){
            u1 = u1.substring(0, u1.length()-1);
        }
        while(u2.endsWith("/")){
            u2 = u2.substring(0, u2.length()-1);
        }
        return u1.equalsIgnoreCase(u2);
	}
	
	public static URL toUrl(String url){
	    try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
	}
    
    public static URL parse(URL parent, String path){
        try {
            return new URL(parent, path);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static URL toUrl(String protocol, String host, String path){
        try {
            return new URL(protocol, host, path);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static URL toUrl(String protocol, String user, String password, String host, String path){
        try {
            if(user==null||password==null){
                throw new RuntimeException("Invalid username/password data");
            }
            return new URL(protocol, user+":"+password+"@"+host, path);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static URL toUrl(String protocol, String user, String password, String host, int port, String path){
        try {
            if(user==null||password==null){
                throw new RuntimeException("Invalid username/password data");
            }
            return new URL(protocol, user+":"+password+"@"+host, port, path);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static URL toUrl(URL url, String protocol){
        try {
            return new URL(protocol, url.getHost(), url.getPort(), url.getFile());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static URL toUrl(URL url, String protocol, String user, String password){
        try {
            int port = url.getPort();
            if(port < 1){
                port = NetUtils.getDefaultPort(protocol);
            }
            return new URL(protocol+ "://" + ((user==null||password==null)?"":user+":"+password+"@")+url.getHost()+":"+port+"/"+url.getFile());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
