package github.exia.filewalker;

import java.io.File;

/**
 * Stateless
 */
public interface FileFilter {
  /**
   * checks whether the file can pass or not
   * @param file the file to check
   * @return whether file can pass this check
   */
  boolean pass(File file);
}
