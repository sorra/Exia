package github.exia.util;

public class MyLogger {
	private String name;
		
	public static MyLogger getLogger(Class<?> cl) {
		return new MyLogger(cl);
	}
	
	private MyLogger(Class<?> cl) {
		String qname = cl.getName();
		name = qname.substring(qname.lastIndexOf('.')+1, qname.length());
	}
	
	public void log(Object msg) {
		System.out.println("[" + name + "] " + msg);
	}
	
	public void record(Object msg) {
		System.out.println("[" + name + "] " + msg);
	}
}
