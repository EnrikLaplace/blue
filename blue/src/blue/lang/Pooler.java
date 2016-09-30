package blue.lang;

public abstract class Pooler extends Pool<Pooler.InternalRun> {

	protected class InternalRun extends Task {
		private int _index;
		private InternalRun(int index){
			_index = index;
		}
		@Override
		protected void work() throws Throwable {
			doWork(_index);
		}
	}
	
	public Pooler(int max) {
		super(max);
	}
	
	public void fill(int count){
		for(int i=0; i<count; i++){
			new InternalRun(i).runAsSubTask(this);;
		}
	}
	
	protected abstract void doWork(int index) throws Throwable;
}
