package com.iostate.exia.core;


import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.iostate.exia.ast.visitors.ExtraArrayDimensionRewriter;
import com.iostate.exia.io.FileUtil;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;


/**
 * Gets & Caches compilation units
 */
@SuppressWarnings("unchecked")
public class CuBase {
  /**
   * key: file path
   * value: compilation unit
   */
  private static final Map<String, CompilationUnit> cuCache = new ConcurrentHashMap<>();

  /**
   * key: file path
   * value: source content
   */
  private static final Map<String, String> docCache = new ConcurrentHashMap<>();

  private static final Map<String, String> compilerOptions = JavaCore.getOptions();

  static {
    compilerOptions.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
    compilerOptions.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
    compilerOptions.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
  }

  private static final Map<String, String> formatterOptions = DefaultCodeFormatterConstants.getEclipseDefaultSettings();

  static {
    formatterOptions.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
    formatterOptions.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
    formatterOptions.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
    formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
    formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "2");
    formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, "100");
    formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_JOIN_LINES_IN_COMMENTS, DefaultCodeFormatterConstants.FALSE);
    // change the option to wrap each enum constant on a new line
    formatterOptions.put(
        DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ENUM_CONSTANTS,
        DefaultCodeFormatterConstants.createAlignmentValue(
            true,
            DefaultCodeFormatterConstants.WRAP_ONE_PER_LINE,
            DefaultCodeFormatterConstants.INDENT_ON_COLUMN));
  }

  private static final CodeFormatter codeFormatter = ToolFactory.createCodeFormatter(formatterOptions);

  public static void unloadAnyFile(String file) {
    cuCache.remove(file);
    docCache.remove(file);
  }

  public static CompilationUnit getCuByPath(String srcFile) {
    String source = getSourceDoc(srcFile);
    return getCuBySource(srcFile, source, true);
  }

  public static CompilationUnit getCuByQname(String qname) {
    return getCuByPath(SourcePaths.get(qname));
  }

  public static String rewriteSource(CompilationUnit cu, File file) {
    Document document = new Document(getSourceDoc(file.getPath()));
    TextEdit edits = cu.rewrite(document, formatterOptions);
    try {
      edits.apply(document);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return document.get();
  }

  public static Map getFormatterOptions() {
    return Collections.unmodifiableMap(formatterOptions);
  }

  private static String getSourceDoc(String srcFile) {
    String doc = docCache.get(srcFile);
    if (doc == null) {
      doc = FileUtil.read(new File(srcFile));
      docCache.put(srcFile, doc);
    }
    return doc;
  }

  private static CompilationUnit getCuBySource(String srcFile, String source, boolean recordable) {
    CompilationUnit cu = cuCache.get(srcFile);
    if (cu != null) {
      return cu;
    }
    cu = parse(source.toCharArray());

    if (recordable)
      cu.recordModifications();
    else
      cu.accept(new ExtraArrayDimensionRewriter());

    cuCache.put(srcFile, cu);
    return cu;
  }

  public static CompilationUnit getCuByPathNoCache(String srcFile, boolean recordable) {
    String source = FileUtil.read(new File(srcFile));

    return getCuBySourceNoCache(source, recordable);
  }

  private static CompilationUnit getCuBySourceNoCache(String source, boolean recordable) {
    CompilationUnit cu = parse(source.toCharArray());

    if (recordable) {
      cu.recordModifications();
    } else {
      cu.accept(new ExtraArrayDimensionRewriter());
    }

    return cu;
  }

  public static CompilationUnit parse(char[] src) {
    ASTParser parser = ASTParser.newParser(AST.JLS8);
    parser.setKind(ASTParser.K_COMPILATION_UNIT);

    parser.setCompilerOptions(compilerOptions);

    parser.setSource(src);

    return (CompilationUnit) parser.createAST(null);
  }

  public static String format(String source) {
    return format(source, CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS);
  }

  public static String format(String source, int kind) {
    final TextEdit edit = codeFormatter.format(
        kind,
        source, // source to format
        0, // starting position
        source.length(), // length
        0, // initial indentation
        System.getProperty("line.separator") // line separator
    );
    if (edit == null) {
      System.out.println("#Cannot format it#");
      System.out.println(source);
      throw new RuntimeException("Cannot format it!");
    }

    IDocument document = new Document(source);
    try {
      edit.apply(document);
    } catch (MalformedTreeException | BadLocationException e) {
      throw new RuntimeException(e);
    }

    return document.get();
  }
}
