/*
 * ObservableList.java
 *
 * (C) 2015 - 2015 Cedac Software S.r.l.
 */
package blue.lang;

import java.util.ArrayList;
import java.util.Collection;

public abstract class ObservableList<K> extends ArrayList<K> {
    private static final long serialVersionUID = 6781110803018836869L;
    @Override
    public synchronized void add(int index, K element) {
        beforeInsert(index,element);
        super.add(index, element);
        afterInsert(index,element);
    }

    @Override
    public synchronized boolean add(K e) {
        int index = super.size()-1;
        beforeInsert(index,e);
        boolean ret = super.add(e);
        if(ret){
            afterInsert(index,e);
        }
        return ret;
    }
    
    @Override
    public synchronized boolean addAll(Collection<? extends K> c) {
        int startK=super.size()-1;
        int k = startK;
        for(K e:c){
            beforeInsert(k++,e);
        }
        boolean ret = super.addAll(c);
        if(ret){
            k= startK;
            for(K e:c){
                afterInsert(k++,e);
            }
        }
        return ret;
    }
    
    @Override
    public synchronized boolean addAll(int index, Collection<? extends K> c) {
        int k = index;
        for(K e:c){
            beforeInsert(k++,e);
        }
        boolean ret = super.addAll(index, c);
        if(ret){
            k= index;
            for(K e:c){
                afterInsert(k++,e);
            }
        }
        return ret;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public synchronized boolean remove(Object o) {
        int iof = super.indexOf(o);
        beforeRemove(iof,(K) o);
        boolean ret = super.remove(o);
        if(ret){
            afterRemove(iof,(K) o);
        }
        return ret;
    }
    
    @Override
    public synchronized K remove(int index) {
        K el = super.get(index);
        beforeRemove(index,el);
        K ret = super.remove(index);
        if(ret != null){
            afterRemove(index,el);
        }
        return ret;
    }

    // ===============================================
    
    protected synchronized void afterInsert(int index, K element) {    }

    protected synchronized void beforeInsert(int index, K element) {    }
    
    protected synchronized void afterRemove(int index, K element) {    }

    protected synchronized void beforeRemove(int index, K element) {    }
}
