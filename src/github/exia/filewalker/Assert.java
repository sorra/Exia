package github.exia.filewalker;

public class Assert {
  public static void beTrue(boolean value) {
    if (!value) {
      throw new RuntimeException("Assertion Error! current file: "+FileWalker.getCurrentFile());
    }
  }
  
  public static void notNull(Object o) {
    beTrue(o != null);
  }
}
