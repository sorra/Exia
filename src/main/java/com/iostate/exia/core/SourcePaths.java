package com.iostate.exia.core;

import java.io.File;
import java.util.*;

import com.iostate.exia.util.MyLogger;

public class SourcePaths {
  private static boolean inited = false;

  private static final Map<String, String> qnamesVsPaths = new HashMap<>();
  private static final List<File> roots = new ArrayList<>();

  private static final MyLogger logger = MyLogger.getLogger(SourcePaths.class);

  static {
    ensureInit();
  }

  public static String get(String qname) {
    String path = qnamesVsPaths.get(qname);
    if (path == null) {
      throw new RuntimeException("No file for " + qname);
    }
    return path;
  }

  public static boolean containsQname(String qname) {
    return qnamesVsPaths.containsKey(qname);
  }

  public static Map<String, String> sourcePaths() {
    return Collections.unmodifiableMap(qnamesVsPaths);
  }

  public static synchronized void ensureInit() {
    if (!inited) {
      for (String project : FileWalker.projects) {
        addRoot(new File(project));
      }

      for (File root : roots) {
        goThrough(root);
      }

      if (qnamesVsPaths.isEmpty()) {
        throw new RuntimeException("Found no file. This is weird!");
      }
      inited = true;
    }
  }

  private static void goThrough(File file) {
    if (file.getName().startsWith(".")) {
      return;
    }

    if (file.isDirectory()) {
      for (File child : file.listFiles()) {
        goThrough(child);
      }
    } else {
      goFile(file);
    }
  }

  private static void goFile(File file) {
    final String path = file.getPath().replace('\\', '/');
    final String _java_ = "/java/";
    if (path.endsWith(".java") && path.contains(_java_) && !path.endsWith("package-info.java")) {
      String qname = path.substring(path.indexOf(_java_) + _java_.length())
          .replace(".java", "").replace('/', '.');
      if (!qname.contains(".")) {
        throw new RuntimeException("Weird qualified name having no dot: " + qname);
      }
      String prev = qnamesVsPaths.put(qname, path);
      if (prev != null && prev.contains("/main/java/")) {
        logger.log(String.format("WTF dup [%s]: %s,\n      %s", qname, prev, path));
      }
    }
  }

  public static synchronized void addRoot(File root) {
    roots.add(root);
  }
}

