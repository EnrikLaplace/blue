/*
 * SearchList.java
 *
 * (C) 2016 - 2016 Cedac Software S.r.l.
 */
package blue.lang;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import blue.util.Utils;

public abstract class SearchList<ST extends SearchList<ST,T>,T> implements Iterable<T> {
    
    /**
     * Basic search condition
     * 
     * @author Matteo
     *
     */
    protected abstract class SearchCond<K> {
        
        ST perform(K inp){
            ST ret = createEmpty();
            ret.list = new ArrayList<T>();
            if(inp == null && !allowNull){
                return ret;
            }
            for(T s:list()){
                if(check(s, inp)){
                    ret.list.add(s);
                }
            }
            return ret;
        }
        
        protected abstract boolean check(T el, K ck);
    }

    
    // ===============================
    
    // internal maps
    protected ArrayList<T> list;
    private boolean allowNull = false;
    
    // ==============================
    
    protected SearchList(){
    }
    
    private Collection<T> list(){
        if(list != null){
            return this.list;
        }else{
            return searchList();
        }
    }
    
    protected abstract Collection<T> searchList();

    // ===============================

    
    protected <K> ST search(SearchCond<K> cond, K param) {
        return cond.perform(param);
    }
    
    @SuppressWarnings("unchecked")
    protected ST createEmpty() {
        return (ST) Utils.newInstance(this.getClass());
    }

    public int size(){
        return this.list().size();
    }
    
    public T getFirst(){
        Collection<T> ll = list();
        return ll.size() == 0?null:ll.iterator().next();
    }

    public Iterator<T> iterator() {
        return iter(list().iterator());
    }
    
    private Iterator<T> iter(final Iterator<T> listIter) {
        return new Iterator<T>() {
            
            public void remove() {
                throw new UnsupportedOperationException();
            }
            
            public T next() {
                return listIter.next();
            }
            
            public boolean hasNext() {
                return listIter.hasNext();
            }
        };
    }
    
    public void setAllowNull(boolean allowNull) {
        this.allowNull = allowNull;
    }
    
    public boolean isSubList() {
        return this.list != null;
    }
}
