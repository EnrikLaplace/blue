/*
 * Cached.java
 *
 * (C) 2015 - 2015 Cedac Software S.r.l.
 */
package blue.lang;

public abstract class Cached<T> {
    
    private Task autorefresh_task = new Task()  {

        @Override
        protected void work() throws Throwable {
            while(true){
                try{
                    cachedValue = update();
                    time.reset();
                }catch(Exception e){
                    // ?
                }
                Thread.sleep(expire);
            }
        }
        
    };
    
    // =======================================
    
    private Time time;
    private volatile T cachedValue;
    private long expire;
    private boolean autorefresh = false;
    private boolean firstSet = true;
    
    // =======================================

    public Cached(){
        this(-1);
    }
    public Cached(long expire){
        this.expire = expire;
        this.time = new Time(0);
    }
    public Cached(long expire, boolean autoRefresh){
        this(expire);
        this.autorefresh = autoRefresh;
    }
    
    public synchronized T get(){
        if(expire<=0){
            if(firstSet || isExpired()){
                cachedValue = update();
                firstSet=false;
            }
            return cachedValue;
        }
        if(!isTimeExpired() && !isExpired()){
            return cachedValue;
        }
        if(autorefresh && (!firstSet && !autorefresh_task.isRunning())){
            // async refresh
            autorefresh_task.start();
        }
        if(firstSet || !autorefresh){
            cachedValue = update();
            time.reset();
            firstSet=false;
        }
        return cachedValue;
    }

    public void invalidate() {
        time.clear();
    }
    
    public boolean isExpired(){
        return false;
    }
    private boolean isTimeExpired(){
        return time.isElapsed(expire);
    }
    
    public long lastUpdate(){
    	return time.millis();
    }
    
    public void setAutorefresh(boolean val){
        autorefresh = val;
    }
    
    protected abstract T update();
}
