package com.iostate.exia.ast;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.*;

public class VariableTypeResolver {
  private final String symbol;
  private final ASTNode minScope;

  private boolean typeLevel = true;

  /**
   * The found result
   */
  private SimpleName declName;

  private final ASTVisitor visitor = new ASTVisitor() {
    @Override
    public boolean visit(SimpleName sn) {
      if (found()) {
        return false;
      }
      if (sn.getIdentifier().equals(symbol) && sn.getParent() instanceof VariableDeclaration) {
        declName = sn;
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
    return declName;
  }

  public Type resolveType() {
    SimpleName declSN = resolveDeclSimpleName();
    if (declSN == null) throw new RuntimeException("Cannot resolve decl for: " + symbol);
    ASTNode maybeFrag = declSN.getParent();
    ASTNode varDecl;
    if (maybeFrag instanceof VariableDeclarationFragment) {
      varDecl = maybeFrag.getParent();
    } else if (maybeFrag instanceof SingleVariableDeclaration) {
      varDecl = maybeFrag;
    } else {
      throw new RuntimeException(maybeFrag.toString());
    }
    List<StructuralPropertyDescriptor> props = varDecl.structuralPropertiesForType();
    return props.stream().filter(p -> p.isChildProperty() && p.getId().equals("type"))
        .findAny().map(p -> (Type) varDecl.getStructuralProperty(p))
        .orElseThrow(RuntimeException::new);
  }

  public String resolveTypeQname() {
    Type type = resolveType();
    return AstFind.qnameOfTypeRef(type.toString().trim(), FindUpper.cu(type));
  }

  private void resolve() {
    if(found()) {return;}

    applyLocal(minScope);

    if(found()) {return;}

    if (typeLevel) {
      AbstractTypeDeclaration typeScope = FindUpper.abstractTypeScope(minScope);
      applyInFields(typeScope, false);

      if(found()) {return;}

      for (TypeDeclaration superClass : superClasses(typeScope)) {
        if(found()) {return;}
        applyInFields(superClass, true);
      }
    }
  }

  private boolean found() {
    return declName != null;
  }

  private void applyLocal(ASTNode node) {
    if (node instanceof Block) {
      node.accept(visitor);
    }
    while (!found()) {
      ASTNode outer = FindUpper.scoper(node, Block.class);
      if (outer == null) break;
      node = outer;
      node.accept(visitor);
    }
    if (!found()) {
      MethodDeclaration md = FindUpper.methodScope(node);
      if (md != null) {
        List<SingleVariableDeclaration> parameters = md.parameters();
        for (SingleVariableDeclaration parameter : parameters) {
          parameter.accept(visitor);
        }
      }
    }
  }

  private void applyInFields(AbstractTypeDeclaration typeScope, boolean isSuper) {
    for (Object bd : typeScope.bodyDeclarations()) {
      if (bd instanceof FieldDeclaration) {
        if (isSuper && AstFind.hasModifierKeyword(((FieldDeclaration) bd).modifiers(),
            Modifier.ModifierKeyword.PRIVATE_KEYWORD)) {//TODO handle package-private
          continue;
        }
        ((FieldDeclaration) bd).accept(visitor);
        if (found()) {return;}
      }
    }
  }

  private List<TypeDeclaration> superClasses(AbstractTypeDeclaration atd) {
    if (atd instanceof TypeDeclaration) {
      return AstFind.superClasses((TypeDeclaration) atd);
    }
    else {
      return Collections.emptyList();
    }
  }
}