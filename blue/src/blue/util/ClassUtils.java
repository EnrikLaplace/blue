package blue.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import blue.lang.Zip;
import blue.lang.Zip.ZipElement;

public final class ClassUtils {
	private ClassUtils(){}
	
	// ======================================

    @SuppressWarnings("unchecked")
    public static <T> T getStaticField(Class<?> cc, String fieldName) {
        Field[] declaredFields = cc.getDeclaredFields();
        for (Field field : declaredFields) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                if(field.getName().equals(fieldName)){
                    try {
                        field.setAccessible(true);
                        return (T) field.get(cc);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                        return null;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 
     * @param hds
     * @param string
     */
	@SuppressWarnings("unchecked")
	public static <T> T getField(Object obj, String fieldName) {
        Class<?> c = obj.getClass();
        do{
			Field[] fs = c.getDeclaredFields();
			for(Field f:fs) {
				if(f.getName().equalsIgnoreCase(fieldName)) {
					try {
						f.setAccessible(true);
						return (T)f.get(obj);
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
						return null;
					} catch (IllegalAccessException e) {
						e.printStackTrace();
						return null;
					}
				}
			}
        }
        while((c=c.getSuperclass()) != null);
        
		// not found
		return null;
	}

	
	@SuppressWarnings("unchecked")
    public static <T> T doMethod(Object obj, String methodName, Object ... args){
        Class<?> c = obj.getClass();
        do{
            for(Method m:c.getDeclaredMethods()){
                if(m.getName().equalsIgnoreCase(methodName)) {
                    m.setAccessible(true);
                    try {
                        return (T) m.invoke(obj, args);
                    } catch (IllegalArgumentException e) {
                        continue;
                    } catch (IllegalAccessException e) {
                        return null;
                    } catch (InvocationTargetException e) {
                        return null;
                    }
                }
            }
        }
        while((c=c.getSuperclass()) != null);
        
        // invalid
        return null;        
    }
	
	public static Set<ClassLoader> getClassLoaders(){
		Set<ClassLoader> loaders = new HashSet<ClassLoader>();
		ClassLoader csLoader = Thread.currentThread().getContextClassLoader();
		// load all class loaders
		while(csLoader != null){
			loaders.add(csLoader);
			csLoader = csLoader.getParent();
		}
		return loaders;
	}
	
	public static Set<URL> getClassLocations() {
		Set<URL> ret = new HashSet<URL>();
		for(ClassLoader cs:getClassLoaders()){
			if(cs instanceof URLClassLoader){
				URLClassLoader cl = (URLClassLoader)cs;
				URL[] listLoc = cl.getURLs();
				ret.addAll(Arrays.asList(listLoc));
			}
		}
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Set<Class<T>> getClasses(String classPath) {
		
		Set<Class<T>> ret = new HashSet<Class<T>>();
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Set<URL> locs = getClassLocations();
		
		for(URL loc:locs){
			
			// caso zip/jar
			if(loc.toString().endsWith(".jar") || loc.toString().endsWith(".zip")) {
				try {
					Zip zz = new Zip(loc.openStream());
					ZipElement el = zz.get(classPath.replaceAll("\\.", "/"));
					if(el == null){
						continue;
					}
					for(ZipElement ec:el.getContent(false)){
						if(!ec.isDirectory() && ec.getName().endsWith(".class")) {
							try {
								ret.add((Class<T>) loader.loadClass(classPath + "." + ec.getName().replace(".class", "")));
							} catch (ClassNotFoundException e) {
								e.printStackTrace();
							} catch (ClassCastException e){
								// incompatible class, ignore
							}
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// caso dir
			if(loc.toString().endsWith("/")) {
				File cpath = null;
				try {
					cpath = new File(new File(loc.toURI()), classPath.replaceAll("\\.", "/"));
				} catch (URISyntaxException e1) {
				}
				if(cpath.exists()){
					for(File ff:cpath.listFiles()){
						// generate file path
						if(ff.isFile() && ff.getName().endsWith(".class")){
							try {
								ret.add((Class<T>) loader.loadClass(classPath + "." + ff.getName().replace(".class", "")));
							} catch (ClassNotFoundException e) {
								e.printStackTrace();
							} catch (ClassCastException e){
								// incompatible class, ignore
							}
						}
					}
				}
			}
		}
		return ret;
	}

}
