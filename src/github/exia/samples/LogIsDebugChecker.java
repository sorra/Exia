package github.exia.samples;

import github.exia.ast.util.AstUtils;
import github.exia.ast.util.FindUpper;
import github.exia.filewalker.AstFunction;
import github.exia.filewalker.FileWalker;
import github.exia.provided.JavaSourceFileFilter;

import java.io.File;
import java.io.FileFilter;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * Detects debug-level logging operations which are without an isDebugEnabled() check.
 * Such operations harm performance so we want to detect them.
 */
public class LogIsDebugChecker {
  public static void main(String[] args) {
	String[] roots = args;
	  
    FileFilter filter = new JavaSourceFileFilter();
    
    AstFunction function = new AstFunction() {
      @Override
      public boolean doAndModify(final CompilationUnit cu, final File file) {
        final TypeDeclaration type = AstUtils.tryGetConcreteType(cu);
        if (type == null) return false;
        
        cu.accept(new ASTVisitor() {
          @Override
          public boolean visit(MethodInvocation mi) {
            if (mi.getName().getIdentifier().equals("debug")
                && mi.getExpression() instanceof SimpleName
                && mi.getExpression().toString().contains("log")) {
              if (isOnCaught(mi, type)) {
                Statement st = FindUpper.statement(mi);
                IfStatement ifs = null;
                if (st.getParent() instanceof IfStatement) {
                  ifs = (IfStatement) st.getParent();
                }
                else if (st.getParent().getParent() instanceof IfStatement) {
                  ifs = (IfStatement) st.getParent().getParent();
                }
                
                if (ifs==null || ifs.getExpression().toString().contains(".isDebugEnabled(")==false) {
                  // Print out problem locations [line, path]
                  System.out.printf("L%d in %s\n",
                      cu.getLineNumber(mi.getStartPosition()),
                      file.getPath());
                }
              }
            }
            return true;
          }
        });
        
        return READONLY;
      }
    };
    
    FileWalker.launch(roots, filter, function);
  }
  
  private static boolean isOnCaught(MethodInvocation mi, TypeDeclaration type) {
    SimpleName loggerName = (SimpleName) mi.getExpression();
    Expression arg = (Expression) mi.arguments().get(0);
    if (arg instanceof SimpleName) {
      String ename = ((SimpleName) arg).getIdentifier();
      CatchClause cc = FindUpper.catchClause(mi);
      if (cc != null && cc.getException().getName().getIdentifier().equals(ename)) {
        FieldDeclaration loggerField = AstUtils.findFieldByName(loggerName.getIdentifier(), type);
        if (loggerField != null && loggerField.getType().toString().equals("Logger")) {
          return true;
        }
      }
    }
    
    return false;
  }
}
