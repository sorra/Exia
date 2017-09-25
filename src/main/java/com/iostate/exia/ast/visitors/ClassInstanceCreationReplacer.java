package com.iostate.exia.ast.visitors;


import com.iostate.exia.util.StringMatcher;
import com.iostate.exia.ast.AstUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;

/**
 * Example
 */
public class ClassInstanceCreationReplacer extends ASTVisitor {

  private StringMatcher strMatcher;
  private String neo;

  public ClassInstanceCreationReplacer(StringMatcher strMatcher, String neo) {
    this.strMatcher = strMatcher;
    this.neo = neo;
  }

  @Override
  public boolean visit(ClassInstanceCreation node) {
    if (strMatcher.matches(
        node.getType().toString().trim())) {
      AST ast = node.getAST();
      node.setType(ast.newSimpleType(ast.newSimpleName(neo)));
    }
    return true;
  }
}
