package com.iostate.exia.samples;

import java.io.File;

import com.iostate.exia.ast.FindUpper;
import com.iostate.exia.ast.AstUtils;
import com.iostate.exia.api.AstFunction;
import com.iostate.exia.ast.visitors.GenericSelector;
import com.iostate.exia.core.FileWalker;
import com.iostate.exia.api.JavaSourceFileFilter;
import org.eclipse.jdt.core.dom.*;

/**
 * Detects debug-level logging operations having no isDebugEnabled() check.
 * Such operations harm performance so we want to detect them.
 * How to fix them automatically? Ask me :)
 */
public class LogIsDebugChecker implements AstFunction {

  public static void main(String[] args) {
    FileWalker.launch(args, new LogIsDebugChecker());
  }

  @Override
  public boolean doAndModify(final CompilationUnit cu, final File file) {
    final TypeDeclaration type = AstUtils.tryGetConcreteType(cu);
    if (type == null) return false;

    cu.accept(getSelector(cu, file));

    return READONLY;
  }

  public static GenericSelector getSelector(final CompilationUnit cu, final File file) {
    return new GenericSelector() {
      @Override
      public boolean visit(MethodInvocation mi) {
        if (mi.getName().getIdentifier().equals("debug")
            && mi.getExpression() instanceof SimpleName
            && mi.getExpression().toString().contains("log")) {
          Object firstArg = mi.arguments().get(0);
          if (firstArg instanceof StringLiteral || firstArg instanceof SimpleName) {
            return true; // Simple call without computation is unnecessary to report
          }

          Statement st = FindUpper.statement(mi);
          IfStatement ifs = null;
          if (st.getParent() instanceof IfStatement) {
            ifs = (IfStatement) st.getParent();
          } else if (st.getParent().getParent() instanceof IfStatement) {
            ifs = (IfStatement) st.getParent().getParent();
          }

          if (ifs == null || !ifs.getExpression().toString().contains(".isDebugEnabled(")) {
            addHit(mi);
            // Print out problem locations [line, path]
            System.out.printf("Line %d in %s\n",
                cu.getLineNumber(mi.getStartPosition()),
                file.getPath());
          }
        }
        return true;
      }
    };
  }
}
