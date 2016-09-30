package blue.lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Run pool
 * 
 * @author Vanguard
 *
 * @param <R>
 */
public class Pool<R extends Task> extends Task implements Iterable<R> {
	
	// pool implementation
	private Task[] pool;
	private final HashMap<Task, Integer> poolOrder = new HashMap<Task, Integer>();
	
	// shared variables
	private volatile int currSize;
	private volatile int doneRunning;
	private volatile int currRunning;
	private volatile int maxRunning;
	private volatile int waitRunning;
	private volatile Object poolThread;

	@Override
	protected final void work() throws Throwable {
		poolThread = new Object();
		while(true){
			// loop
			if(waitRunning > 0){
				while(currRunning-doneRunning < maxRunning  && waitRunning > 0){
					synchronized (pool) {
						pool[currRunning]._internal_allowed_start();
						currRunning++;
						waitRunning--;
					}
				}
			}
			
				// pool is full, wait for next eventb
			synchronized (poolThread) {
				poolThread.wait();
			}
		}
	}
	
	// ===========================================
	
	public Pool() {
		this(0);
	}
	
	public Pool(int max) {
		poolThread = new Object();
		if(max > 0){
			maxRunning = max;
			pool = new Task[max];
		}else{
			maxRunning = Integer.MAX_VALUE;
			pool = new Task[10];
		}
	}
	
	// ===========================================
	
	@Override
	void _internal_on_start() {
		// do nothing
	}
	
	@Override
	void _internal_on_child_added(Task child) {
		synchronized (pool) {
			int insertPosition = currRunning + waitRunning + doneRunning;
			if(pool.length <= insertPosition){
				_internal_resize((insertPosition + pool.length/2) - doneRunning);
			}
			pool[insertPosition] = child;
			poolOrder.put(child, insertPosition); // temporary fix..
			waitRunning++;
		}
		if(isRunning()){
			synchronized (poolThread) {
				poolThread.notify();
			}
		}
	}
	
	@Override
	void _internal_on_child_end(Task child) {
		synchronized (pool) {
			// switch child position with first running thread
			int currPos = poolOrder.get(child);
			
			// switch if needed
			if(doneRunning != currPos){
				Task switcher = pool[doneRunning];
				pool[doneRunning] = child;
				poolOrder.put(child, doneRunning);
				pool[currPos] = switcher;
				poolOrder.put(switcher, currPos);
			}
			// update state variable
			doneRunning++;
		}
		
		// check
		synchronized (poolThread) {
			poolThread.notify();
		}
	}
	
	private void _internal_resize(int newSize){
		Task[] oldPool = pool;
		pool = new Task[newSize];
		System.arraycopy(oldPool, doneRunning, pool, 0, currRunning + waitRunning);
		currRunning -= doneRunning;
		doneRunning = 0;
	}
	
	// ===========================================

	public int size(){
		return currSize;
	}

	public int countWaiting(){
		return waitRunning;
	}

	public int countRunning(){
		return currRunning - doneRunning;
	}

	@SuppressWarnings("unchecked")
	public Iterator<R> iterator() {
		ArrayList<R> ret = new ArrayList<R>();
		synchronized (pool) {
			for(Task r:pool){
				ret.add((R) r);
			}
		}
		return ret.iterator();
	}
	
	// ===========================================
}
