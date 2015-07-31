package github.exia.filewalker;

import github.exia.util.CuBase;
import github.exia.util.FileMaker;
import github.exia.util.MyLogger;

import java.io.File;
import java.io.FileFilter;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.core.dom.CompilationUnit;


public class FileWalker {
  
  private final MyLogger logger = MyLogger.getLogger(getClass());
  
  public static String[] projects;
  
  private static final ThreadLocal<File> currentFile = new ThreadLocal<File>();
  public static File getCurrentFile() {return currentFile.get();}
  
  /**
   * root folders to start scan from
   */
  private final String[] roots;
  
  private final FileFilter filter;
  
  private final AstFunction function;
  
  private final ConcurrentLinkedQueue<File> files = new ConcurrentLinkedQueue<File>(); 
  
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
        throw new RuntimeException("Wrong with "+root);
      }
      goThrough(rootDir);
    }
    processAllFiles();
    
    long end = System.currentTimeMillis();
    System.out.println("Time cost: " + (end-start)/1000 + "s");
  }
  
  private void goThrough(File file) {
//    if (!file.exists()) {
//      logger.log("File not exist: " + file.getPath());
//      return;
//    }
    if (file.isDirectory()) {
      for (File child : file.listFiles()) {
        goThrough(child);
      }
    }
    else if (filter.accept(file)) {
      files.offer(file);
    }
  }
  
  private void processAllFiles() {
    int usableCores = Runtime.getRuntime().availableProcessors();
    usableCores = usableCores > 2 ? usableCores-1 : usableCores;
    ExecutorService es = Executors.newFixedThreadPool(usableCores);
    for (int i=0; i<usableCores; i++) {
      es.execute(new Runnable() {
        @Override
        public void run() {
          while (true) {
            File file = files.poll();
            if (file == null)
              break;
            processFile(file);
          }
          currentFile.remove();
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
    currentFile.set(file);
    CompilationUnit cu = CuBase.getClientCuByFilePath(file.getPath());
    boolean modified = false;
    try {
      modified = function.doAndModify(cu, file);
    } catch (RuntimeException e) {
      System.out.println("Error at " + file.getPath());
      throw e;
    }
    if (modified) {
      System.out.println("Write " + file.getPath());
      String content = CuBase.rewriteClient(file.getPath());
      FileMaker.writePlainFile(file.getPath(), content, "UTF-8");
    }
    CuBase.unloadAnyFile(file.getPath());
  }
}
