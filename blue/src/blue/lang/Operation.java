package blue.lang;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import blue.lang.Task;

public abstract class Operation extends Task {
	
	// ==========================================
	
	private double perc = -1;
	private int result = -1;
	
	// ==========================================
	
	protected void setPerc(double perc){
		if(perc > 1){
			perc = 1;
		}
		if(perc < 0) {
			perc = 0;
		}
		this.perc = perc;
	}
	
	// ==========================================
	
	protected List<Operation> getSubOperations(){
		List<Operation> ret = new ArrayList<Operation>();
		Iterator<Task> subs = this.subTasks();
		while(subs.hasNext()) {
			Task nx = subs.next();
			if(nx instanceof Operation){
				ret.add((Operation) nx);
			}
		}
		return ret;
	}

	
	protected List<Operation> getSubOperationsError(){
		List<Operation> ret = getSubOperations();
		Iterator<Operation> itr = ret.iterator();
		while(itr.hasNext()){
			if(itr.next().isOk()) {
				itr.remove();
			}
		}
		return ret;
	}
	
	protected boolean hasSubErrors(){
		return getSubOperationsError().size() > 0;
	}
	
	/**
	 * Progress from 0 to 1
	 * 
	 * @return
	 */
	public double getPerc() {
		return perc;
	}
	public double calcSubPerc() {
		double ret = 1d;
		List<Operation> sp = getSubOperations();
		if(sp.size() > 0){
			double par = 1d/((double)(sp.size()*1d));
			for(Operation op:sp){
				ret += par*op.getPerc();
			}
		}
		return ret;
	}
	
	public double calcSubPerc(double min, double max) {
		double tot = calcSubPerc();
		return (max-min)*tot;
	}
	
	public void setSubPerc(){
		setPerc(calcSubPerc());
	}
	
	public void setSubPerc(double min, double max){
		setPerc(calcSubPerc(min,max));
	}
	
	public boolean isOk(){
		return result == 0;
	}
	
	public int getResult(){
		return result;
	}
	
	public void setResult(int result) {
		this.result = result;
	}
	
	@Override
	protected synchronized void threadEnd() {
		super.threadEnd();
		perc = 1;
	}
	
	@Override
	protected synchronized void threadStart() {
		super.threadStart();
		perc = 0;
		result = -1;
	}
}
