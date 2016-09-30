package blue.runtime;

import java.io.InputStream;
import java.net.URL;

import blue.lang.Task;

public class App extends Task {
	
	// ======================================

	private Process process;
	private String[] pars;
	
	// options
	private boolean unique = false;

	// ======================================

//    private App(Process prc) {
//        this.process = prc;
//    }
//    
//    public App(String cmdLine) throws IOException{
//    	this(Runtime.getRuntime().exec(cmdLine));
//    }

	public App(String exe, String... pars) {
		this.pars = new String[pars.length + 1];
		this.pars[0] = exe;
		System.arraycopy(pars, 0, this.pars, 1, pars.length);
	}

	public App(URL exe, String... pars) {
		this(exe.getPath(), pars);
	}

	// ======================================

	private void stopProcess() {
		if (process != null) {
			process.destroy();
			process = null;
		}
	}

	// ======================================

	@Override
	protected void work() throws Throwable {
	    if(process == null){
	        if(unique){
	            String name = pars[0];
	            if(name.lastIndexOf('/') > -1){
	                name = name.substring(name.lastIndexOf('/') + 1);
	            }
	            for(AppProcess prc:AppProcess.list(name)){
	            	prc.kill();
	            }
//	            ExternalProcess.stopAll(name);
	        }
//	        process = new ProcessBuilder(pars).start();
	        process = Runtime.getRuntime().exec(pars);
	        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
				public void run() {
					if(process != null){
						process.destroy();
						process = null;
					}
				}
			}));
	    }
		InputStream in = process.getInputStream();
		int rd;
        while ((rd = in.read()) != -1) {
			// still runnning
		    System.out.print(new String(new byte[]{(byte)rd}));
		}
	}

	@Override
	public synchronized boolean stop(long timeout) {
		stopProcess();
		return super.stop(timeout);
	}

	@Override
	protected void _internal_on_end() {
		stopProcess();
		super._internal_on_end();
	}
	
	// --------------------------------------
	
	public boolean isUnique() {
		return unique;
	}
	
	public void setUnique(boolean unique) {
		this.unique = unique;
	}
	
}
