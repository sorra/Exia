package com.iostate.exia.ast.visitors;


import com.iostate.exia.util.StringMatcher;
import com.iostate.exia.ast.AstUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;


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
        AstUtils.pureNameOfType(node.getType()))) {
      AST ast = node.getAST();
      node.setType(ast.newSimpleType(ast.newSimpleName(neo)));
    }
    return true;
  }
}
