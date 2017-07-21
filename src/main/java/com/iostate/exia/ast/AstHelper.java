package com.iostate.exia.ast;

import java.util.List;

import com.iostate.exia.util.Assert;
import com.iostate.exia.util.PrimitiveTypeUtil;
import com.iostate.exia.util.StringMatcher;
import com.iostate.exia.ast.visitors.GenericSelector;
import com.iostate.exia.ast.visitors.SimpleNameReplacer;
import com.iostate.exia.ast.visitors.TypeReferenceSelector;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.*;

/**
 * This is a helper only for ast building. NOTE: it's stateful!
 */
@SuppressWarnings("unchecked")
public class AstHelper {

  private AST ast;

  public AstHelper(AST ast) {
    this.ast = ast;
  }

  /**
   * @care primitive types will become boxed types
   */
  public Type copyReturnType(Type ejbReturnType) {
    Type rt;
    if (ejbReturnType instanceof PrimitiveType) {
      String primTypeName = ((PrimitiveType) ejbReturnType).getPrimitiveTypeCode().toString();
      rt = createSimpleType(PrimitiveTypeUtil.box(primTypeName));
    } else {
      rt = (Type) ASTNode.copySubtree(ast, ejbReturnType);
    }
    return rt;
  }

  public void copyImports(CompilationUnit dest, CompilationUnit source) {
    TypeReferenceSelector trSelector = new TypeReferenceSelector();
    dest.accept(trSelector);
    // System.out.println("#Candidate Imports: " + trs.getHits());

    List<ImportDeclaration> ejbImports = source.imports();
    for (ImportDeclaration one : ejbImports) {
      String fullName = one.getName().getFullyQualifiedName();
      String lastName = fullName.substring(fullName.lastIndexOf('.') + 1);
      if (trSelector.getHits().contains(lastName)) {
        ImportDeclaration copy = (ImportDeclaration) ASTNode.copySubtree(ast, one);
        dest.imports().add(copy);
      }
    }
  }

  private ImportDeclaration createImport(String fullName) {
    ImportDeclaration importDecl = ast.newImportDeclaration();
    if (fullName.endsWith(".*")) {
      fullName = fullName.substring(0, fullName.lastIndexOf('.'));
      importDecl.setName(ast.newName(fullName));
      importDecl.setOnDemand(true);
    } else {
      importDecl.setName(ast.newName(fullName));
    }

    return importDecl;
  }

  public boolean insertImport(CompilationUnit cu, String fullName) {
    ImportDeclaration importD = createImport(fullName);

    List<ImportDeclaration> imports = cu.imports();
    for (ImportDeclaration each : imports) {
      String eachFullName = each.getName().getFullyQualifiedName();
      if (eachFullName.equals(fullName)) {
        return false;
      } else if (eachFullName.substring(eachFullName.lastIndexOf('.'))
          .equals(fullName.substring(fullName.lastIndexOf('.')))) {
        throw new ImportConflictException(eachFullName + " ! " + fullName);
      }
    }

    imports.add(importD);
    return true;
  }

  @Deprecated
  public boolean forceImport(CompilationUnit cu, String fullName) {
    ImportDeclaration importD = createImport(fullName);

    List<ImportDeclaration> imports = cu.imports();
    for (ImportDeclaration each : imports) {
      String eachFullName = each.getName().getFullyQualifiedName();
      if (eachFullName.equals(fullName)) {
        return false;
      }
    }

    imports.add(importD);
    return true;
  }

  public PackageDeclaration createPackageDecl(String fullName) {
    PackageDeclaration packageDecl = ast.newPackageDeclaration();
    packageDecl.setName(ast.newName(fullName));
    return packageDecl;
  }

  public FieldDeclaration createInjectableField(String type, String name) {
    VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
    fragment.setName(ast.newSimpleName(name));
    FieldDeclaration field = ast.newFieldDeclaration(fragment);
    field.setType(createSimpleType(type));

    field.modifiers().add(createMarkerAnnotation("In"));

    List<Modifier> modifiers = field.modifiers();
    modifiers.add(ast.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD));

    return field;
  }

  public MarkerAnnotation createMarkerAnnotation(String typename) {
    MarkerAnnotation marker = ast.newMarkerAnnotation();
    marker.setTypeName(ast.newName(typename));
    return marker;
  }

  public SingleMemberAnnotation createCatchMark(String catchMark) {
    SingleMemberAnnotation supp = ast.newSingleMemberAnnotation();
    supp.setTypeName(ast.newSimpleName("SuppressWarnings"));
    StringLiteral sl = ast.newStringLiteral();
    sl.setLiteralValue(catchMark);
    supp.setValue(sl);
    return supp;
  }

  public FieldDeclaration createLogger(String loggedClassName) {
    return createLogger("logger", loggedClassName);
  }

  public FieldDeclaration createLogger(String loggerName, String loggedClassName) {
    VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
    fragment.setName(ast.newSimpleName(loggerName));
    MethodInvocation loggerCreation = ast.newMethodInvocation();
    loggerCreation.setExpression(ast.newSimpleName("Logger"));
    loggerCreation.setName(ast.newSimpleName("getLogger"));
    TypeLiteral typeLiteral = ast.newTypeLiteral();
    typeLiteral.setType(createSimpleType(loggedClassName));
    loggerCreation.arguments().add(typeLiteral);
    fragment.setInitializer(loggerCreation);

    FieldDeclaration loggerDecl = ast.newFieldDeclaration(fragment);
    loggerDecl.setType(createSimpleType("Logger"));
    loggerDecl.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD));
    loggerDecl.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD));

    return loggerDecl;
  }

  public MethodDeclaration createGetter(String fieldName, Type fieldType) {
    String shiftedName = StringUtils.capitalize(fieldName);
    Type typeCopy = (Type) ASTNode.copySubtree(ast, fieldType);

    MethodDeclaration getter = ast.newMethodDeclaration();
    getter.setName(ast.newSimpleName("get" + shiftedName));
    getter.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));

    getter.setReturnType2(typeCopy);

    // body
    Block body = ast.newBlock();
    getter.setBody(body);

    ReturnStatement rs = ast.newReturnStatement();
    rs.setExpression(ast.newSimpleName(fieldName));
    body.statements().add(rs);

    return getter;
  }

  public MethodDeclaration createSetter(String fieldName, Type fieldType) {
    String shiftedName = StringUtils.capitalize(fieldName);
    Type typeCopy = (Type) ASTNode.copySubtree(ast, fieldType);

    MethodDeclaration setter = ast.newMethodDeclaration();
    setter.setName(ast.newSimpleName("set" + shiftedName));
    setter.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));

    SingleVariableDeclaration parDecl = ast.newSingleVariableDeclaration();
    parDecl.setType(typeCopy);
    parDecl.setName(ast.newSimpleName(fieldName));
    setter.parameters().add(parDecl);

    // body
    Block body = ast.newBlock();
    setter.setBody(body);

    FieldAccess fieldAccess = ast.newFieldAccess();
    fieldAccess.setExpression(ast.newThisExpression());
    fieldAccess.setName(ast.newSimpleName(fieldName));

    Assignment assignment = ast.newAssignment();
    assignment.setLeftHandSide(fieldAccess);
    assignment.setRightHandSide(ast.newSimpleName(fieldName));
    body.statements().add(ast.newExpressionStatement(assignment));

    return setter;
  }

  public SimpleType createSimpleType(String typename) {
    return ast.newSimpleType(ast.newName(typename));
  }

  public TryStatement createWrapperTryCatch(String catchEx, String newEx) {
    TryStatement tryStmt = ast.newTryStatement();
    Block tryBody = ast.newBlock();
    tryStmt.setBody(tryBody);

    {
      CatchClause catchClause = ast.newCatchClause();
      tryStmt.catchClauses().add(catchClause);
      SingleVariableDeclaration svd = ast.newSingleVariableDeclaration();
      catchClause.setException(svd);
      svd.setType(createSimpleType(catchEx));
      svd.setName(ast.newSimpleName("e"));

      Block catchBody = ast.newBlock();
      catchClause.setBody(catchBody);
      ThrowStatement ts = ast.newThrowStatement();
      catchBody.statements().add(ts);
      ClassInstanceCreation exCreation = ast.newClassInstanceCreation();
      ts.setExpression(exCreation);
      exCreation.setType(createSimpleType(newEx));
      StringLiteral message = ast.newStringLiteral();
      message.setLiteralValue("Wrapped Exception: ");
      exCreation.arguments().add(message);
      exCreation.arguments().add(ast.newSimpleName("e"));
    }
    return tryStmt;
  }

  /**
   * Be careful to use it!
   */
  public boolean normalEqualsByBody(CatchClause a, CatchClause b) {
    Assert.assertNotNull(a);
    Assert.assertNotNull(b);
    return normalizeCatchClause(a).getBody().toString()
        .equals(normalizeCatchClause(b).getBody().toString());
  }

  public CatchClause normalizeCatchClause(CatchClause clauseToNormalize) {
    Assert.assertNotNull(clauseToNormalize);
    CatchClause cc = (CatchClause) ASTNode.copySubtree(ast, clauseToNormalize);
    cc.getException().modifiers().clear();
    final String oldExName = cc.getException().getName().getIdentifier();
    final String newExName = "e";
    cc.accept(new SimpleNameReplacer(StringMatcher.equals(oldExName), newExName));

    GenericSelector<MethodInvocation> logSel = new GenericSelector<MethodInvocation>() {
      @Override
      public boolean visit(MethodInvocation node) {
        if (node.getExpression() != null
            && node.getExpression().toString().contains("log")) {
          addHit(node);
        }
        return true;
      }
    };
    cc.accept(logSel);
    for (MethodInvocation each : logSel.getHits()) {
      each.accept(new ASTVisitor() {
        @Override
        public boolean visit(StringLiteral node) {
          node.setLiteralValue("");
          return true;
        }
      });
    }
    return cc;
  }
}
