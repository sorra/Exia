package com.iostate.exia.ast;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.*;

public class DataFlowAnalyzer {
  private SimpleName ref;
  private LookupVisitor visitor;

  public DataFlowAnalyzer(SimpleName ref) {
    this.ref = ref;
    visitor = new LookupVisitor(ref.getIdentifier());
  }

  public VariableDeclaration getDecl() {
    return visitor.decl;
  }

  public List<Assignment> assignments() {
    return visitor.assigns;
  }

  public void analyze() {
    if (isDeclFound()) {return;}

    // Lookup in code blocks, excluding method body
    Block upperBlock = null;
    while (true) {
      upperBlock = FindUpper.scoper(upperBlock == null ? ref : upperBlock, Block.class);
      if (upperBlock == null || upperBlock.getParent() instanceof MethodDeclaration) {
        break;
      }

      visitor.assigns.clear();
      upperBlock.accept(visitor);
      if (isDeclFound()) {return;}
    }

    // Lookup in method decl and body
    visitor.assigns.clear();
    MethodDeclaration upperMethod = FindUpper.methodScope(ref);
    if (upperMethod != null) {
      upperMethod.accept(visitor);
      if (isDeclFound()) {return;}
    }

    // Lookup in current type's fields
    TypeDeclaration upperType = FindUpper.typeScope(ref);
    applyInFields(upperType, false);
    if (isDeclFound()) return;

    // Lookup in super types' fields
    for (TypeDeclaration superType : AstFind.superClasses(upperType)) {
      applyInFields(superType, true);
      if (isDeclFound()) {return;}
    }
  }

  private void applyInFields(AbstractTypeDeclaration typeScope, boolean isSuper) {
    for (Object bd : typeScope.bodyDeclarations()) {
      if (bd instanceof FieldDeclaration) {
        FieldDeclaration fd = (FieldDeclaration) bd;
        if (isSuper && AstFind.hasModifierKeyword(fd.modifiers(),
            Modifier.ModifierKeyword.PRIVATE_KEYWORD)) {//TODO handle package-private
          continue;
        }
        fd.accept(visitor);
        if (isDeclFound()) {break;}
      }
    }
  }

  private boolean isDeclFound() {
    return visitor.decl != null;
  }

  private static class LookupVisitor extends ASTVisitor {
    private String refName;

    LookupVisitor(String refName) {
      this.refName = refName;
    }

    private VariableDeclaration decl;
    private List<Assignment> assigns = new ArrayList<>();

    @Override
    public boolean visit(VariableDeclarationFragment frag) {
      if (frag.getName().getIdentifier().equals(refName)) {
        if (decl != null) {
          throw new RuntimeException("Already found: " + decl + "\nNow found: " + frag);
        }
        decl = frag;
      }
      return true;
    }

    @Override
    public boolean visit(SingleVariableDeclaration svd) {
      if (svd.getName().getIdentifier().equals(refName)) {
        if (decl != null) {
          throw new RuntimeException("Already found: " + decl + "\nNow found: " + svd);
        }
        decl = svd;
      }
      return true;
    }

    @Override
    public boolean visit(Assignment assignment) {
      Expression left = assignment.getLeftHandSide();
      if (left instanceof SimpleName && ((SimpleName) left).getIdentifier().equals(refName)) {
        assigns.add(assignment);
      }
      return true;
    }
  }
}
