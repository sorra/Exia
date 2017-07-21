package com.iostate.exia.api;

import java.io.File;
import java.io.FileFilter;

public class JavaSourceFileFilter implements FileFilter {
  @Override
  public boolean accept(File file) {
    return file.getPath().endsWith(".java") && !file.getName().equals("package-info.java");
  }
}
