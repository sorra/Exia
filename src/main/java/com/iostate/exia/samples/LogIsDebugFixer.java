package com.iostate.exia.samples;

import java.io.File;

import com.iostate.exia.api.AstFunction;
import com.iostate.exia.api.JavaSourceFileFilter;
import com.iostate.exia.ast.AstUtils;
import com.iostate.exia.ast.FindUpper;
import com.iostate.exia.ast.visitors.GenericSelector;
import com.iostate.exia.core.FileWalker;
import com.iostate.exia.util.MyLogger;
import org.eclipse.jdt.core.dom.*;

/**
 * Automatically fix the problems found by LogIsDebugChecker
 */
public class LogIsDebugFixer implements AstFunction {
  private static MyLogger logger = MyLogger.getLogger(LogIsDebugFixer.class);

  public static void main(String[] args) {
    FileWalker.launch(args, new LogIsDebugFixer());
  }

  @Override
  public boolean doAndModify(CompilationUnit cu, File file) {
    GenericSelector<MethodInvocation> selector = LogIsDebugChecker.getSelector(cu, file);
    cu.accept(selector);

    AST ast = cu.getAST();
    for (MethodInvocation debugMI : selector.getHits()) {
      if (!(debugMI.getParent() instanceof ExpressionStatement)) {
        continue;
      }

      IfStatement ifs = ast.newIfStatement();
      SimpleName sn;
      try {
        sn = (SimpleName) debugMI.getExpression();
      } catch (RuntimeException e) {
        logger.log("Error occurs at MI: " + debugMI + " , file: " + file.getPath());
        throw e;
      }

      MethodInvocation isDebugEnabled = ast.newMethodInvocation();
      isDebugEnabled.setExpression(ast.newSimpleName(sn.getIdentifier()));
      isDebugEnabled.setName(ast.newSimpleName("isDebugEnabled"));
      ifs.setExpression(isDebugEnabled);

      Block block = ast.newBlock();
      ifs.setThenStatement(block);

      Statement debugST = FindUpper.statement(debugMI);
      AstUtils.replaceNode(debugST, ifs);
      block.statements().add(debugST);
    }

    if (selector.getHits().size() > 0) {
      return MODIFIED;
    } else {
      return READONLY;
    }
  }
}
