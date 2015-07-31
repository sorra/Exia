package github.exia.samples;

import github.exia.ast.util.AstUtils;
import github.exia.ast.util.FindUpper;
import github.exia.filewalker.AstFunction;
import github.exia.filewalker.FileWalker;
import github.exia.provided.JavaSourceFileFilter;
import github.exia.sg.visitors.GenericSelector;

import java.io.File;
import java.io.FileFilter;

import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * Rewrites pattern "logger.error(e)" to "logger.error(msg, e)".
 * Detects the types of logger & e, no matter how the variables are named.
 */
public class LogCorrecter {
  public static void main(String[] args) {
	String[] roots = args;

    FileFilter filter = new JavaSourceFileFilter();
    
    AstFunction function = new AstFunction() {
      @Override
      public boolean doAndModify(final CompilationUnit cu, File file) {
        final TypeDeclaration type = AstUtils.tryGetConcreteType(cu);
        if (type == null) return READONLY;

        GenericSelector<MethodInvocation> logSel = new GenericSelector<MethodInvocation>() {
          @Override
          public boolean visit(MethodInvocation mi) {
            if (mi.getParent() instanceof ExpressionStatement
                && mi.getName().getIdentifier().equals("error")
                && mi.arguments().size() == 1
                && mi.getExpression() instanceof SimpleName
                && mi.getExpression().toString().contains("log")) {
              if (isOnCaught(mi, type)) {
                addHit(mi);
                correctMI(mi);
              }
            }
            return true;
          }
        };
        
        if (type.getMethods().length == 0) {
          return READONLY;
        }
        for (MethodDeclaration method : type.getMethods()) {
          method.accept(logSel);
        }
        
        if (logSel.getHits().size() > 0) {
          return MODIFIED;
        }
        else {return READONLY;}
      }
    };
    
    FileWalker.launch(roots, filter, function);
  }
  
  private static void correctMI(MethodInvocation mi) {
    StringLiteral sl = mi.getAST().newStringLiteral();
    sl.setLiteralValue("Error occurred: ");
    mi.arguments().add(0, sl);
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
