package github.exia.util;



import github.exia.sg.visitors.ExtraArrayDimensionRewriter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
import org.junit.Assert;



/**
 * Caches compilation units
 */
@SuppressWarnings("unchecked")
public class CuBase {
	/**
	 * key: file path
	 * value: compilation unit
	 */
	private static final Map<String, CompilationUnit> cuCache = new HashMap<String, CompilationUnit>();
	
	/**
	 * key: file path
	 * value: source
	 */
	private static final Map<String, String> docCache = new HashMap<String, String>();
	
	private static final Map<String, String> compilerOptions = JavaCore.getOptions();
	static {
		compilerOptions.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_7);
		compilerOptions.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_7);
		compilerOptions.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_7);
	}
	
	private static final Map<String, String> formatterOptions = DefaultCodeFormatterConstants.getEclipseDefaultSettings();
	static {
		formatterOptions.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_7);
		formatterOptions.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_7);
		formatterOptions.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_7);
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
	  if (cuCache.containsKey(file)) {
	    cuCache.remove(file);
	  }
	  if (docCache.containsKey(file)) {
	    docCache.remove(file);
	  }
	}
	
	public static CompilationUnit getClientCuByFilePath(String srcFile) {	
	    if (docCache.get(srcFile) != null) {
	      return getCuBySource(srcFile, docCache.get(srcFile), true);
	    }
		String source;
		try {
			source = FileBufferedReader.readToString(srcFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		docCache.put(srcFile, source);
		return getCuBySource(srcFile, source, true);
	}
	
	public static String rewriteClient(String srcFile) {
	    Assert.assertNotNull(docCache.get(srcFile));
		Document document = new Document(docCache.get(srcFile));
		TextEdit edits = cuCache.get(srcFile).rewrite(document, formatterOptions);
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
	
	private static CompilationUnit getCuBySource(String srcFile, String source, boolean recordable) {		
		CompilationUnit cu = cuCache.get(srcFile);
		if (cu != null) {
			return cu;
		}
		cu = parse(source.toCharArray());
		
		if(recordable)
		  cu.recordModifications();
		else
		  cu.accept(new ExtraArrayDimensionRewriter());
		
		cuCache.put(srcFile, cu);
		return cu;
	}
	
  public static CompilationUnit getCuNoCache(String srcFile, boolean recordable) {
    String source;
    try {
      source = FileBufferedReader.readToString(srcFile);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return getCuBySourceNoCache(source, recordable);
  }

  private static CompilationUnit getCuBySourceNoCache(String source, boolean recordable) {
    CompilationUnit cu = parse(source.toCharArray());

    if (recordable)
      cu.recordModifications();
    else
      cu.accept(new ExtraArrayDimensionRewriter());

    return cu;
  }

	public static CompilationUnit parse(char[] src) {
			ASTParser parser = ASTParser.newParser(AST.JLS4);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			
			parser.setCompilerOptions(compilerOptions);
			
			parser.setSource(src);
	
			CompilationUnit cu = (CompilationUnit) parser.createAST(null);
	
			return cu;
	}
	
	public static String format(String source) {
		return format(source, CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS);
	}
	
	public static String format(String source, int kind){
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
		} catch (MalformedTreeException e) {
			throw new RuntimeException(e);
		} catch (BadLocationException e) {
			throw new RuntimeException(e);
		}

		return document.get();
	}
}
