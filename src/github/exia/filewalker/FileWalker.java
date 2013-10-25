package github.exia.filewalker;

import github.exia.util.CuBase;
import github.exia.util.FileMaker;
import github.exia.util.MyLogger;

import java.io.File;

import org.eclipse.jdt.core.dom.CompilationUnit;


public class FileWalker {
  
  private final MyLogger logger = MyLogger.getLogger(getClass());
  
  /**
   * root folders to start scan from
   */
  private final String[] roots;
  
  private final FileFilter filter;
  
  private final AstFunction function;
  
  public static void launch(String[] roots, FileFilter filter, AstFunction function) {
    new FileWalker(roots, filter, function).walk();
  }
  
  public FileWalker(String[] roots, FileFilter filter, AstFunction function) {
    this.roots = roots;
    this.filter = filter;
    this.function = function;
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
    else if (filter.pass(file)) {
      processFile(file);
    }
  }

  private void processFile(File file) {
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
