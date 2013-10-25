package github.exia.provided;

import java.io.File;

import github.exia.filewalker.FileFilter;

public class JavaSourceFileFilter implements FileFilter {
  @Override
  public boolean pass(File file) {
    final String path = file.getPath();
    if(path.endsWith(".java") && Character.isUpperCase(file.getName().charAt(0))) {
      return true;
    }
    else return false;
  }
}
