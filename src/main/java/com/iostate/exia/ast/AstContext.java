package com.iostate.exia.ast;

import java.io.File;

import org.eclipse.jdt.core.dom.CompilationUnit;

public class AstContext {
  public final File file;
  public final String source;
  public final CompilationUnit cu;
  public boolean modified = false;

  public AstContext(File file, String source, CompilationUnit cu) {
    this.file = file;
    this.source = source;
    this.cu = cu;
  }
}
