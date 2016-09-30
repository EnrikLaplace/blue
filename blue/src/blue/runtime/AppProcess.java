package blue.runtime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import blue.util.AppUtil;

public class AppProcess {

	private static final String KILL = "taskkill /F /pid ";
	private static final String TASKLIST = "tasklist";

	public static List<AppProcess> list(String src) {
		List<AppProcess> ret = new ArrayList<AppProcess>();
		
			String line;
			try {
				while ((line = AppUtil.readProcess(TASKLIST).readLine()) != null) {
					String name = line.split("  ")[0];
					String id = line.substring(name.length()).trim().split(" ")[0];
					if(src != null){
						// find by name and extension
						if(src.indexOf('.') > -1) {
							if(!src.toLowerCase().equals(name.toLowerCase())) {
								continue;
							}
						}else{
							String check = (name.indexOf('.') == -1)?name:name.substring(0,name.lastIndexOf('.'));
							// find by name
							if(!src.toLowerCase().equals(check.toLowerCase())){
								continue;
							}
						}
					}
					AppProcess proc = new AppProcess(Long.parseLong(id));
					proc.name = name;
					ret.add(proc);
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return ret;
	}
	
	// ========================================
	
	String name;

	private long id;
	
	// ========================================

	AppProcess(long id){
		this.id= id;
	}
	
	// ========================================

	protected Process requestStop(){
		try {
			return Runtime.getRuntime().exec(KILL + id);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean kill(){

		Process p = requestStop();
		try {
			return p.waitFor() == 0;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
	}
}
