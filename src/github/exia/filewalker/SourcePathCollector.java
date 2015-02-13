package github.exia.filewalker;

import github.exia.util.MyLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SourcePathCollector {
  private static boolean inited = false;
  
  private static final Map<String, String> sourcePaths = new HashMap<String, String>();
  private static final List<File> roots = new ArrayList<File>();
  
  private static final MyLogger logger = MyLogger.getLogger(SourcePathCollector.class);
  
  static {
    checkInit();
  }
  public static String find(String qname) {
    return sourcePaths.get(qname);
  }
  
  public static String get(String qname) {
    String path = sourcePaths.get(qname);
    if (path == null) {throw new RuntimeException("No file for " + qname);}
    return path;
  }
  
  public static Map<String, String> sourcePaths() {
    return Collections.unmodifiableMap(sourcePaths);
  }
  
  public static synchronized void checkInit() {
    if (!inited) {
      for (String project : FileWalker.projects) {
        addRoot(new File(project));
      }
      
      for (File root : roots) {
        goThrough(root);
      }
      
      if (sourcePaths.isEmpty()) {
        throw new RuntimeException("Find no file. This is strange!");
      }
      inited = true;
    }
  }
  
  private static void goThrough(File file) {
    if (file.isDirectory()) {
      if (file.getName().equals(".git")) {
        return;
      }
      for (File child : file.listFiles()) {
        goThrough(child);
      }
    }
    else {
      goFile(file);
    }
  }

  private static void goFile(File file) {
    final String path = file.getPath().replace('\\', '/');
    final String _java_ = "/java/";
    if (path.endsWith(".java") && path.contains(_java_) && !path.endsWith("package-info.java")) {
      String qname = path.substring(path.indexOf(_java_) + _java_.length())
          .replace(".java", "").replace('/', '.');
      if (!qname.startsWith("com.")) {
        // logger.log(qname+": "+path);
      }
      if (!qname.contains(".")) throw new RuntimeException();
      String prev = sourcePaths.put(qname, path);
      if (prev != null && prev.contains("/main/java/")) {
        logger.log(String.format("WTF dup [%s]: %s,\n      %s", qname, prev, path));
      }
    }
  }
  
  public static synchronized void addRoot(File root) {
    roots.add(root);
  }
}

