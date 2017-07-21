package com.iostate.exia.samples;

import java.io.File;

import com.iostate.exia.api.AstFunction;
import com.iostate.exia.api.JavaSourceFileFilter;
import com.iostate.exia.ast.AstUtils;
import com.iostate.exia.ast.visitors.GenericSelector;
import com.iostate.exia.core.FileWalker;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeLiteral;

/**
 * Class A should call getLogger(A.class).
 * If A calls getLogger(B.class), this might be wrong.
 */
public class WrongLoggerNameChecker implements AstFunction {
  public static void main(String[] args) {
    FileWalker.launch(args, new WrongLoggerNameChecker());
  }

  @Override
  public boolean doAndModify(final CompilationUnit cu, final File file) {
    cu.accept(new GenericSelector<MethodInvocation>() {
      @Override
      public boolean visit(MethodInvocation mi) {
        if (mi.getName().getIdentifier().equals("getLogger") && mi.arguments().size() == 1
            && AstUtils.tryGetConcreteType(cu) != null) {
          Object firstArg = mi.arguments().get(0);
          if (firstArg instanceof TypeLiteral) {
            String typeName = ((TypeLiteral) firstArg).getType().toString();
            if (!typeName.equals(AstUtils.tryGetConcreteType(cu).getName().getIdentifier())) {
              addHit(mi);
              // Print out problem locations [line, path]
              System.out.printf("Line %d in %s\n",
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
}
