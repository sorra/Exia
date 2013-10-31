package github.exia.main;

import github.exia.samples.LogCorrecter;
import github.exia.samples.LogIsDebugChecker;
import github.exia.samples.UnusedImportDeletor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class Main {
  private static Map<String, Class<?>> map = new HashMap<String, Class<?>>();
  static {
	  map.put("log-correct", LogCorrecter.class);
	  map.put("log-is-debug", LogIsDebugChecker.class);
	  map.put("unused-import-delete", UnusedImportDeletor.class);
  }
  
  public static void main(String[] args) throws Exception {
    String cls = args[0];
    String[] paths = Arrays.copyOfRange(args, 1, args.length);
    Class<?> clazz = map.get(cls);
    if (clazz == null) throw new IllegalArgumentException("Unknown command \""+cls+"\" in arg0");
    clazz.getDeclaredMethods()[0].invoke(null, (Object)paths);
  }
}
