/*
 * Trial.java
 *
 * (C) 2016 - 2016 Cedac Software S.r.l.
 */
package blue.lang;

public abstract class Trial<T> {
    
    private int maxTry;
    private long waitTime;
    
    public Trial(int maxTry, long waitTime) {
        this.maxTry = maxTry;
        this.waitTime = waitTime;
    }
    
    public T get(){
        return get(maxTry);
    }
    private T get(int currTry){
        try{
            return doTry();
        }catch(Throwable t) {
            if(currTry ==0){
                return null;
            }
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                return null;
            }
            System.err.println("Trial retry: " + currTry);
            return get(currTry-1);
        }
    }
    
    protected abstract T doTry() throws Throwable;
}
