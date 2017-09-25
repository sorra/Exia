package com.iostate.exia.samples;

import java.io.File;

import com.iostate.exia.ast.AstFind;
import com.iostate.exia.ast.FindUpper;
import com.iostate.exia.ast.visitors.GenericSelector;
import com.iostate.exia.ast.AstUtils;
import com.iostate.exia.api.AstFunction;
import com.iostate.exia.core.FileWalker;
import com.iostate.exia.api.JavaSourceFileFilter;
import org.eclipse.jdt.core.dom.*;

/**
 * Rewrites pattern "logger.error(e)" to "logger.error(msg, e)".
 * Detects the types of logger & e, no matter how the variables are named.
 */
public class LogCorrecter implements AstFunction {

  public static void main(String[] args) {
    FileWalker.launch(args, new LogCorrecter());
  }

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
    } else {
      return READONLY;
    }
  }

  private void correctMI(MethodInvocation mi) {
    StringLiteral sl = mi.getAST().newStringLiteral();
    sl.setLiteralValue("Error occurred: ");
    mi.arguments().add(0, sl);
  }

  private boolean isOnCaught(MethodInvocation mi, TypeDeclaration type) {
    SimpleName loggerName = (SimpleName) mi.getExpression();
    Expression arg = (Expression) mi.arguments().get(0);
    if (arg instanceof SimpleName) {
      String ename = ((SimpleName) arg).getIdentifier();
      CatchClause cc = FindUpper.catchClause(mi);
      if (cc != null && cc.getException().getName().getIdentifier().equals(ename)) {
        FieldDeclaration loggerField = AstFind.findFieldByName(loggerName.getIdentifier(), type);
        if (loggerField != null && loggerField.getType().toString().equals("Logger")) {
          return true;
        }
      }
    }

    return false;
  }
}
