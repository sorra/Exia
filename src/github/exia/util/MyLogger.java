package github.exia.util;

public class MyLogger {
	private String name;
		
	public static MyLogger getLogger(Class<?> cl) {
		return new MyLogger(cl);
	}
	
	public static MyLogger getLogger(String name) {
	  return new MyLogger(name);
	}
	
	private MyLogger(Class<?> cl) {
		String qname = cl.getName();
		name = qname.substring(qname.lastIndexOf('.')+1, qname.length());
	}
	
	private MyLogger(String name) {
	  this.name = name;
	}
	
	public void log(Object msg) {
		System.out.println("[" + name + "] " + msg);
	}
	
	public void record(Object msg) {
		System.out.println("[" + name + "] " + msg);
	}
}
