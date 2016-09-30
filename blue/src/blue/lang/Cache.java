package blue.lang;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public abstract class Cache<K,C> implements Iterable<C> {
	
	private HashMap<K, C> cache;
    private Time lastTime;
    private int duration;
    
    public Cache(){
        this(-1);
    }
	
	public Cache(int duration){
		this.cache = new HashMap<K, C>();
		this.lastTime = new Time(0);
		this.duration = duration;
	}
	
	public synchronized C get(K key){
	    if(duration >= 0 && lastTime.isElapsed(duration)){
	        lastTime.reset();
	        cache.clear();
	    }
		if(!cache.containsKey(key)){
			C obj = init(key);
			if(obj == null){
				return null;
			}
			cache.put(key, obj);
		}
		return cache.get(key);
	}

	public boolean has(K key) {
		return cache.containsKey(key);
	}
	
	public void clear(){
		cache.clear();
	}
	
	protected abstract C init(K key);

    public int size() {
        return cache.size();
    }
    
    public Set<K> getKeys(){
        return cache.keySet();
    }
    
    public Iterator<C> iterator() {
        return cache.values().iterator();
    }
}
