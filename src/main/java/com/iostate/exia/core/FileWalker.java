package com.iostate.exia.core;

import java.io.File;
import java.io.FileFilter;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.iostate.exia.api.JavaSourceFileFilter;
import com.iostate.exia.io.FileUtil;
import com.iostate.exia.util.MyLogger;
import com.iostate.exia.api.AstFunction;
import org.eclipse.jdt.core.dom.CompilationUnit;


/**
 * Multi-threaded. You can set threads to 1 for small codebase.
 */
public class FileWalker {

  private final MyLogger logger = MyLogger.getLogger(getClass());

  public static String[] projects;

  /**
   * root folders to start scan from
   */
  private final String[] roots;

  private final FileFilter filter;

  private final AstFunction function;

  private final ConcurrentLinkedQueue<File> files = new ConcurrentLinkedQueue<>();

  public static void launch(String[] roots, AstFunction function) {
    launch(roots, new JavaSourceFileFilter(), function);
  }

  public static void launch(String[] roots, FileFilter filter, AstFunction function) {
    new FileWalker(roots, filter, function).walk();
  }

  public FileWalker(String[] roots, FileFilter filter, AstFunction function) {
    this.roots = roots;
    this.filter = filter;
    this.function = function;
    projects = roots;
  }

  public void walk() {
    long start = System.currentTimeMillis();
    for (String root : roots) {
      File rootDir = new File(root);
      if (!rootDir.isDirectory()) {
        throw new RuntimeException("Wrong with " + root);
      }
      goThrough(rootDir);
    }
    processAllFiles();

    long end = System.currentTimeMillis();
    System.out.println("Time cost: " + (end - start) / 1000 + "s");
  }

  private void goThrough(File file) {
    if (file.getName().startsWith(".")) {
      return;
    }

    if (file.isDirectory()) {
      for (File child : file.listFiles()) {
        goThrough(child);
      }
    } else if (filter.accept(file)) {
      files.offer(file);
    }
  }

  private void processAllFiles() {
    int usableCores = Runtime.getRuntime().availableProcessors();
    usableCores = usableCores > 2 ? usableCores - 1 : usableCores;
    ExecutorService es = Executors.newFixedThreadPool(usableCores);
    for (int i = 0; i < usableCores; i++) {
      es.execute(new Runnable() {
        @Override
        public void run() {
          while (true) {
            File file = files.poll();
            if (file == null)
              break;
            processFile(file);
          }
        }
      });
    }

    es.shutdown();
    try {
      es.awaitTermination(10, TimeUnit.MINUTES);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private void processFile(File file) {
    CompilationUnit cu = CuBase.getCuByPathNoCache(file.getPath(), true);
    boolean modified;
    try {
      modified = function.doAndModify(cu, file);
    } catch (RuntimeException e) {
      logger.log("Error at file: " + file.getPath());
      throw e;
    }
    if (modified) {
      logger.log("Write " + file.getPath());
      String content = CuBase.rewriteSource(cu, file);
      FileUtil.write(file, content);
    }
  }
}
