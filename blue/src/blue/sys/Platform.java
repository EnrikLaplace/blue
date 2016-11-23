package blue.sys;

public class Platform {
	
	private Platform(){}
	
	// --------------------------------------
	
	public static final String getArch(){
		// Will say "x86" even on a 64-bit machine
	    // using a 32-bit Java runtime
//	    SystemEnvironment env =
//	        SystemEnvironment.getSystemEnvironment();
//	    final String envArch = env.getOsArchitecture();

	    // The os.arch property will also say "x86" on a
	    // 64-bit machine using a 32-bit runtime
	    final String propArch = System.getProperty("os.arch");
	    
	    return propArch;
	}
	
	public static final boolean isArch64(){
		return getArch().equalsIgnoreCase("x86_64") || getArch().equalsIgnoreCase("amd64");
	}
	
	public static final boolean isArch32(){
		return !isArch64();
	}
}
