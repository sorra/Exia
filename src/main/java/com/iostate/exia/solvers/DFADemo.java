package com.iostate.exia.solvers;

import java.util.stream.Collectors;

import com.iostate.exia.api.AstFunction;
import com.iostate.exia.ast.DataFlowAnalyzer;
import com.iostate.exia.ast.visitors.GenericSelector;
import com.iostate.exia.core.CuBase;
import com.iostate.exia.core.FileWalker;
import org.eclipse.jdt.core.dom.*;

public class DFADemo {
  public static void main(String[] args) {
    // Change args to the Exia project folder
    FileWalker.launch(args, (cu, file) -> AstFunction.READONLY);

    final String varName = "visitor";

    CompilationUnit cu = CuBase.getCuByQname(DataFlowAnalyzer.class.getName());
    GenericSelector<SimpleName> refSel = new GenericSelector<SimpleName>() {
      @Override
      public boolean visit(SimpleName node) {
        if (node.getParent() instanceof VariableDeclaration
            // TODO excluding field access?
            || node.getParent() instanceof FieldAccess
            || node.getParent() instanceof SuperFieldAccess
            || node.getParent() instanceof QualifiedName) {
          return true;
        }

        if (node.getIdentifier().equals(varName)) {
          addHit(node);
        }

        return true;
      }
    };
    cu.accept(refSel);
    System.out.println("Refs: ");
    System.out.println(refSel.getHits().stream().map(ASTNode::getParent).collect(Collectors.toList()));

    for (SimpleName hit : refSel.getHits()) {
      DataFlowAnalyzer dataFlowAnalyzer = new DataFlowAnalyzer(hit);
      dataFlowAnalyzer.analyze();

      System.out.println("Decl:");
      System.out.println(dataFlowAnalyzer.getDecl().getParent().toString().trim());
      System.out.println("Assignments:");
      dataFlowAnalyzer.assignments().forEach(a -> System.out.println(a.toString().trim()));
      System.out.println();
    }
  }
}
