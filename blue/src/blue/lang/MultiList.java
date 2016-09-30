/*
 * MultiList.java
 *
 * (C) 2016 - 2016 Cedac Software S.r.l.
 */
package blue.lang;

import java.util.HashMap;
import java.util.Map;

public class MultiList<K> extends ObservableList<K> {
    private static final long serialVersionUID = -8903719318591099440L;
    
    private Map<String, ListRegistration<?>> regs = new HashMap<String, ListRegistration<?>>();
    
    public  abstract class ListRegistration<T> {
        private HashMap<T,K> contMap = new HashMap<T, K>();
        protected abstract T calcKey(K element);
        public K get(T key){
            return contMap.get(key);
        }
        public boolean has(T key) {
            return contMap.containsKey(key);
        }
        protected boolean register(K element) {
            T key = calcKey(element);
            if(key != null){
                contMap.put(key, element);
                return true;
            }
            return false;
        }
    }
    
    protected <T> void register(String name, ListRegistration<T> register){
        regs.put(name, register);
    }
    
    public <N> K search(String srcName, N key){
        @SuppressWarnings("unchecked")
        ListRegistration<N> src = (ListRegistration<N>) regs.get(srcName);
        return src.get(key);
    }

    @Override
    protected synchronized void afterInsert(int index, K element) {
        for(ListRegistration<?> ff:regs.values()){
            ff.register(element);
        }
    }
    
    @Override
    protected synchronized void beforeRemove(int index, K element) {
        for(ListRegistration<?> ff:regs.values()){
            ff.contMap.remove(element);
        }
    }
}
