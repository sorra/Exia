package com.iostate.exia.ast;

import org.eclipse.jdt.core.dom.*;

public class FindUpper {

  public static <T extends ASTNode> T scoper(ASTNode node, Class<T> clazz) {
    ASTNode parent = node.getParent();
    if (parent == null) return null;

    if (clazz.isAssignableFrom(parent.getClass())) {
      return (T) parent;
    }
    return scoper(parent, clazz);
  }

  /**
   * Returns null if not found until it meets BodyDeclaration
   */
  public static Statement statement(ASTNode node) {
    ASTNode parent = node.getParent();

    if (parent == null) {
      return null;
    }

    if (parent instanceof Statement) {
      return (Statement) parent;
    } else if (parent instanceof BodyDeclaration) {
      return null;
    }

    return statement(parent);
  }

  /**
   * Returns null if not found until it meets TypeDeclaration
   */
  public static MethodDeclaration methodScope(ASTNode node) {
    ASTNode parent = node.getParent();

    if (parent == null) {
      return null;
    }

    if (parent instanceof MethodDeclaration) {
      return (MethodDeclaration) parent;
    } else if (parent instanceof AbstractTypeDeclaration) {
      return null;
    }

    return methodScope(parent);
  }

  /**
   * Returns null if not found until it meets MethodDeclaration
   */
  public static TryStatement tryScope(ASTNode node) {
    ASTNode parent = node.getParent();

    if (parent == null) {
      return null;
    }
    if (parent instanceof TryStatement) {
      return (TryStatement) parent;
    } else if (parent instanceof MethodDeclaration) {
      return null;
    }

    return tryScope(parent);
  }

  public static CatchClause catchClause(ASTNode node) {
    ASTNode parent = node.getParent();

    if (parent == null) {
      return null;
    }
    if (parent instanceof CatchClause) {
      return (CatchClause) parent;
    }
    if (parent instanceof TryStatement) {
      return null;
    }
    if (parent instanceof MethodDeclaration) {
      return null;
    }

    return catchClause(parent);
  }

  /**
   * Returns null if not found until it meets CompilationUnit
   */
  public static TypeDeclaration typeScope(ASTNode node) {
    ASTNode parent = node.getParent();

    if (parent == null) {
      return null;
    }

    if (parent instanceof TypeDeclaration) {
      return (TypeDeclaration) parent;
    } else if (parent instanceof AbstractTypeDeclaration) {
      throw new UnsupportedOperationException();
    } else if (parent instanceof CompilationUnit) {
      return null;
    }

    return typeScope(parent);
  }

  /**
   * Returns null if not found until it meets CompilationUnit
   */
  public static AbstractTypeDeclaration abstractTypeScope(ASTNode node) {
    ASTNode parent = node.getParent();

    if (parent == null) {
      return null;
    }

    if (parent instanceof AbstractTypeDeclaration) {
      return (AbstractTypeDeclaration) parent;
    } else if (parent instanceof CompilationUnit) {
      return null;
    }

    return abstractTypeScope(parent);
  }

  /**
   * Returns null if not found
   */
  public static CompilationUnit cu(ASTNode node) {
    ASTNode parent = node.getParent();

    if (parent == null) {
      return null;
    }

    if (parent instanceof CompilationUnit) {
      return (CompilationUnit) parent;
    }

    return cu(parent);
  }

}
