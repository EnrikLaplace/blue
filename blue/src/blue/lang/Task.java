package blue.lang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public abstract class Task  {
	
	// =================================
	
    private final Runnable thread;
	private Thread current_thread;
	
	// params
	private boolean isDaemon;
	
	// sub - runners
	private Task parent;
	private final List<Task> subThreads;
	
	// safe-variables to thread
	private volatile Thread threadStopCallback;
	private volatile boolean threadRunning;
	private volatile boolean threadStarting;

	// =================================
	
	public Task(){
		this(false);
	}

	public Task(boolean isDaemon){
		subThreads = Collections.synchronizedList(new ArrayList<Task>());
		threadRunning = false;
		threadStarting = false;
		
		// preparing thread
		thread = new Runnable() {
			public void run() {
				try {
                    threadStart();
					threadRunning = true;
					threadStarting = false;
					try{
						work();
						threadRunning = false;
					}catch(Throwable t){
						threadRunning = false;
						throw t;
					}
					threadEnd();
				} catch (InterruptedException e){
					e.printStackTrace();
					threadStopCallback.interrupt();
				} catch (ThreadDeath e){
					// is it possible to handle this one??
					// anyway.. thread is dead.. R.I.P.
				} catch (Throwable e) {
					manageException(e);
				}
				// thread end
				_internal_on_end();
				init();
			}
		};

		// config
		this.isDaemon = isDaemon;
		init();
	}
	
	private final synchronized void init(){
        this.current_thread = new Thread(thread);
        this.current_thread.setDaemon(isDaemon);
	}
	
	// =================================
	//			params
	
	public boolean isDaemon(){
		return isDaemon;
	}
	
	public void setDaemon(boolean isDaemon){
	    this.isDaemon = isDaemon;
	    this.current_thread.setDaemon(isDaemon);
	}
	
	public boolean isWorking(){
		return current_thread.isAlive();
	}
	
	public boolean isRunning(){
		return threadRunning || threadStarting;
	}
	
	public boolean isSub(){
		return parent != null;
	}
	
	protected Thread currentThread(){
		return current_thread;
	}
	
	/**
	 * Pause task execution
	 * 
	 * @throws InterruptedException
	 */
	public void pause() throws InterruptedException{
		synchronized (currentThread()) {
			currentThread().wait();
		}
	}
	
	/**
	 * Resume task execution
	 */
	public void resume() {
		synchronized (currentThread()) {
			currentThread().notify();
		}
	}
	
	// =================================
	//			internal
	
	/**
	 * Manage exception internally
	 * @param ex
	 */
	protected void manageException(Throwable ex){
		ex.printStackTrace();
	}
	
	// =================================
	//			internal events
	
	void _internal_on_start(){
		// start all childs
		for(Task c:subThreads){
			c._internal_allowed_start();
		}
	}
	
	protected void _internal_on_end(){
		// brutally kill al childs!
		// buahahaahahahahah!!
		for(Task c:subThreads){
			c.stop(0);
		}
		
		// notify parent
		if(parent != null){
			parent._internal_on_child_end(this);
		}
	}
	
	void _internal_on_child_added(Task child){
		if(isRunning()){
			child._internal_allowed_start();
		}
	}
	
	void _internal_on_child_end(Task child){
		// do nothing
	}
	
	// =================================
	//			functions
	
	public boolean start(){
		if(isSub()){
			return false; // sub task cannot be started
		}
		return _internal_allowed_start();
	}
	
	synchronized boolean _internal_allowed_start(){
		if(isRunning()){
			return false;
		}
		current_thread.start();
		// event
		_internal_on_start();
		threadStarting = current_thread.isAlive();
		return current_thread.isAlive();
	}
	
	public synchronized boolean stop(){
		return stop(-1);
	}
	
	@SuppressWarnings("deprecation")
	public synchronized boolean stop(long timeout){
	    try{
    		if(!current_thread.isAlive()){
    			return false;
    		}
    		if(timeout == 0){
    		    current_thread.stop();
    			return true;
    		}
    		// send interrupt
    		threadStopCallback = Thread.currentThread();
    		current_thread.interrupt();
    		if(timeout >= 0){
    			try {
    				// current thread is sleeping
    				Thread.sleep(timeout);
    				
    				// no interrupt raised, timeout reached!
    				current_thread.stop();
    			} catch (InterruptedException e) {
    				// all ok! no thread stop required
    				// ... this time ..
    				// you are a lucky guy..
    			}
    		}
	    }catch(Exception e){
	        return false;
	    }finally{
	        
	    }
		return true;
	}
	
	/**
	 * Avoid to use run() with running sub-threads
	 */
	protected boolean restart(){
		if(!stop()){
			// invalid state
			return false;
		}
		return _internal_allowed_start();
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void finalize() throws Throwable {
		if(current_thread.isAlive()){
			// hard stop
		    current_thread.stop();
		}
	}
	
	public void runAsSubTask(Task owner){
		assert owner != null;
		assert owner != this;
		if(isRunning() || isSub()){
			throw new IllegalStateException();
		}
		
		// registering on parent
		parent = owner;
		parent.subThreads.add(this);
		
		// daemon childs must be daemon!
		current_thread.setDaemon(current_thread.isDaemon() || parent.current_thread.isDaemon());
		
		// call parent
		parent._internal_on_child_added(this);
	}
	
	public void runSubTask(Task sub){
	    if(sub == null){
	        return;
	    }
		sub.runAsSubTask(this);
	}
    
    // =================================

	/**
     * Internal event after thread end
	 */
    protected synchronized void threadEnd() {
    }

    /**
	 * Internal event before thread start
     */
    protected synchronized void threadStart() {
    }
	
	// =================================

    protected boolean hasRunningSubTasks() {
        for(Task t:subThreads){
            if(t.isRunning()){
                return true;
            }
        }
        return false;
    }
    
    protected Iterator<Task> subTasks(){
        return subThreads.iterator();
    }
    
    /**
     * wait this thread
     * @return 
     */
    public boolean waitMe(){
        if(!this.isRunning()){
            return false;
        }
        wait(this);
        return true;
    }
    
	
	/**
	 * 
	 * @throws Throwable
	 */
	protected abstract void work() throws Throwable;
	
	// ================================================
	
	/**
	 * Stop current thread waiting target thread
	 * @param task
	 */
	public static final void wait(Task task){
	    while(task.isRunning()){
	        try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
	    }
	}
}
