package blue.util;

public class Logger extends java.util.logging.Logger {
	
	private Class<?> owner;

	protected Logger(Class<?> owner) {
		super(owner.getName(),null);
		this.owner = owner;
	}

	public static Logger build(Class<?> owner){
		return new Logger(owner);
	}

	public void error(String msg) {
		this.severe(msg);
	}

	public void error(Throwable e) {
		e.printStackTrace();
		error(e, null);
	}
	
	@Override
	public void info(String msg) {
		super.info(msg);
		System.out.println(msg);
	}

	public void error(Throwable e, String method) {
		throwing(owner.getName(), method, e);
	}
}
