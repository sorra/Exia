package github.exia.ast.util;

import org.eclipse.jdt.core.dom.*;

import java.util.Collections;
import java.util.List;

public class VariableTypeResolver {
  private final String symbol;
  private final ASTNode minScope;

  private boolean methodLevel = true;
  private boolean typeLevel = true;

  /**
   * The found result
   */
  private SimpleName declSN;

  private final ASTVisitor visitor = new ASTVisitor() {
    @Override
    public boolean visit(SimpleName sn) {
      if (found()) {
        return false;
      }
      if (sn.getIdentifier().equals(symbol) && sn.getParent() instanceof VariableDeclaration) {
        declSN = sn;
        return false;
      }
      return true;
    }
  };

  /**
   * Starts resolving with the requested symbol
   * @param varSymbolNode the variable symbol node to resolve (node must be in the AST)
   */
  public VariableTypeResolver(SimpleName varSymbolNode) {
    this.symbol = varSymbolNode.getIdentifier();
    this.minScope = varSymbolNode;
  }

  public VariableTypeResolver(String varSymbol, ASTNode minScope) {
    this.symbol = varSymbol;
    this.minScope = minScope;
  }

  public VariableTypeResolver disableMethodLevel() {
    methodLevel = false;
    return this;
  }
  public VariableTypeResolver disableTypeLevel() {
    typeLevel = false;
    return this;
  }

  /**
   * Node's parent is instance of {@link VariableDeclarationFragment} or {@link SingleVariableDeclaration}
   * @return the SimpleName node of declaration
   */
  public SimpleName resolveDeclSimpleName() {
    if (!found()) {
      resolve();
    }
    return declSN;
  }

  private void resolve() {
    if(found()) {return;}

    if (methodLevel) {
      apply(FindUpper.methodScope(minScope));
    }

    if(found()) {return;}

    if (typeLevel) {
      AbstractTypeDeclaration typeScope = FindUpper.abstractTypeScope(minScope);
      applyInFields(typeScope);

      if(found()) {return;}

      for (TypeDeclaration superClass : superClasses(typeScope)) {
        if(found()) {return;}
        applyInFields(superClass);
      }
    }
  }

  private boolean found() {
    return declSN != null;
  }

  private void apply(ASTNode scope) {
    if (scope == null) {
      throw new NullPointerException();
    }
    scope.accept(visitor);
  }

  private void applyInFields(AbstractTypeDeclaration typeScope) {
    for (Object bd : typeScope.bodyDeclarations()) {
      if (bd instanceof FieldDeclaration) {
        apply((ASTNode) bd);
      }
    }
  }

  private List<TypeDeclaration> superClasses(AbstractTypeDeclaration atd) {
    if (atd instanceof TypeDeclaration) {
      return AstUtils.superClasses((TypeDeclaration) atd);
    }
    else {
      return Collections.EMPTY_LIST;
    }
  }
}
