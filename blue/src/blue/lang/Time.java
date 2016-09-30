package blue.lang;

public class Time {
	
	private long time;

	/**
	 * Specified nano-time
	 * 
	 * @param nanotime
	 */
	public Time(long millis){
		this.time = millis;
	}
	
	/**
	 * Current time
	 */
	public Time(){
		this.time = now();
	}
	
	/**
	 * Time in milliseconds
	 * 
	 * @return
	 */
	public long millis() {
		return time;
	}

	/**
	 * time difference in milliseconds
	 * 
	 * @param millistime
	 * @return
	 */
	public long diff(long millistime) {
		return millistime - millis();
	}
	
	/**
	 * Calculate elapsed milliseconds
	 * 
	 * @return
	 */
	public long elapsed(){
		return (System.currentTimeMillis() - time);
	}
	
	/**
	 * TRUE when elapsed time is equal or greater of delta (milliseconds)
	 * 
	 * @param delta in milliseconds seconds
	 * @return
	 */
	public boolean isElapsed(long delta){
		return elapsed() >= delta;
	}
	
	/**
	 * reset time to current time
	 */
	public void reset(){
		this.time = now();
	}

	/**
	 * clear time to 0
	 */
	public void clear() {
		this.time = 0;
	}
	
	/**
	 * Return current time (ms)
	 */
	public static long now(){
		return System.currentTimeMillis();
	}

	public boolean after(Time t) {
		return t.time < time;
	}

	public boolean before(Time t) {
		return t.time > time;
	}
	
	@Override
	public boolean equals(Object obj) {
		 if (obj instanceof Time) {
			 return time == ((Time) obj).time;
			
		}
		return false;
	}
	
	@Override
	public String toString() {
		return time+"ms";
	}
	
}
