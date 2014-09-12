package github.exia.provided;

import java.io.File;

import github.exia.filewalker.FileFilter;

public class JavaSourceFileFilter implements FileFilter {
  @Override
  public boolean pass(File file) {
    return file.getPath().endsWith(".java") && Character.isUpperCase(file.getName().charAt(0));
  }
}
