package blue.lang;

import blue.util.Utils;

public abstract class Mod<T> {
	
	// ===========================================
	
	private String classPath;
	private Cache<String, Class<T>> classes = new Cache<String, Class<T>>() {

		@Override
		protected Class<T> init(String key) {
			return Mod.this.getClass(key);
		}
	};
	
	// ===========================================
	
	public Mod(String basePath){
		this.classPath = basePath;
	}
	
	// ===========================================
	
	protected String constructClassName(String key){
		return classPath + key;
	}

	protected Class<T> getClass(String key){
		String className = constructClassName(key);
		Class<T> cs = Utils.getClass(className);
		return cs;
	}
	
	protected T getImplementation(Class<T> cs, Object ... args){
		return Utils.newInstance(cs,args);
	}
	
	protected T getDefault(String key, Object ... args){
		return null;
	}

	// ------------------------------------------
	
	@SuppressWarnings("unchecked")
	protected <K extends T> K get(String key, Object ... args){
		Class<T> cs = classes.get(key);
		if(cs == null){
			return (K) getDefault(key, args);
		}
		return (K) getImplementation(cs,args);
	}
	
	// ------------------------------------------
	
	public String getBasePath() {
		return classPath;
	}
}
