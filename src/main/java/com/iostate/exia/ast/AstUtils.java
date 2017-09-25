package com.iostate.exia.ast;

import java.util.ArrayList;
import java.util.List;

import com.iostate.exia.util.MyLogger;
import org.eclipse.jdt.core.dom.*;

import static com.iostate.exia.util.StringMatcher.endsWith;

@SuppressWarnings("unchecked")
public class AstUtils {
  private static MyLogger logger = MyLogger.getLogger(AstUtils.class);

  /**
   * An excellent method! Feel free to use it!
   *
   * @param old the old node
   * @param neo the new node
   */
  public static void replaceNode(ASTNode old, ASTNode neo) {
    StructuralPropertyDescriptor p = old.getLocationInParent();
    if (p == null) {
      // node is unparented
      return;
    }
    if (p.isChildProperty()) {
      old.getParent().setStructuralProperty(p, neo);
      return;
    }
    if (p.isChildListProperty()) {
      List l = (List) old.getParent().getStructuralProperty(p);
      l.set(l.indexOf(old), neo);
    }
  }

  public static String qname(AbstractTypeDeclaration type) {
    String packageName = ((CompilationUnit) type.getParent()).getPackage().getName().getFullyQualifiedName();
    String typeName = type.getName().getIdentifier();
    return packageName + "." + typeName;
  }

  public static TypeDeclaration getType(CompilationUnit cu) {
    return (TypeDeclaration) cu.types().get(0);
  }

  public static AbstractTypeDeclaration getAbstractType(CompilationUnit cu) {
    return (AbstractTypeDeclaration) cu.types().get(0);
  }

  public static TypeDeclaration tryGetConcreteType(CompilationUnit cu) {
    Object type = cu.types().get(0);
    if (type instanceof TypeDeclaration) return (TypeDeclaration) type;
    else {
      logger.log("Fail to get concrete type for: " + ((AbstractTypeDeclaration) type).getName());
      return null;
    }
  }

  public static boolean isTypeSerializable(TypeDeclaration type) {
    List<Type> interfaces = type.superInterfaceTypes();
    for (Type intf : interfaces) {
      String name = intf.toString().trim();
      if (name.equals("Serializable") || name.equals("java.io.Serializable")) {
        return true;
      }
    }
    return false;
  }

  public static MethodSig toMethodSig(MethodDeclaration method) {
    TypeDeclaration type = (TypeDeclaration) method.getParent();
    CompilationUnit cu = (CompilationUnit) type.getParent();

    String methodName = method.getName().getIdentifier();
    String packageName = cu.getPackage().getName().getFullyQualifiedName();
    String className = type.getName().getIdentifier();

    List<SingleVariableDeclaration> pars = method.parameters();
    List<String> emParameters = new ArrayList<>();
    for (SingleVariableDeclaration par : pars) {
      String typename = par.getType().toString().trim();
      emParameters.add(typename);
    }
    return new MethodSig(
        packageName, className, methodName, emParameters);
  }

  public static List<MethodSig> toMethodSigs(List<MethodDeclaration> methods) {
    List<MethodSig> methodSigs = new ArrayList<>();
    for (MethodDeclaration method : methods) {
      methodSigs.add(AstUtils.toMethodSig(method));
    }
    return methodSigs;
  }

  public static boolean isSelfCallGrammar(MethodInvocation mi) {
    return mi.getExpression() == null || mi.getExpression() instanceof ThisExpression;
  }

  public static boolean isClientCallGrammar(MethodInvocation mi) {
    return !isSelfCallGrammar(mi);
  }

  public static boolean isFieldInjectable(FieldDeclaration fd, String annoClassSimpleName) {
    for (Object modifier : fd.modifiers()) {
      if (modifier instanceof Annotation) {
        Annotation annotation = (Annotation) modifier;
        String annoTypeName = annotation.getTypeName().getFullyQualifiedName();
        if (annoTypeName.equals(annoClassSimpleName)) {
          return true;
        } else if (annoTypeName.contains(annoClassSimpleName)) {
          logger.log("Strange annotation type: " + annoTypeName);
        }
      }
    }
    return false;
  }

  /**
   * Matches if methodName and parameters are equal
   *
   * @param keySig  search key
   * @param scopeCu search scope
   * @return method sig, null if not found
   */
  public static MethodSig findRelevantMethodSig(MethodSig keySig, CompilationUnit scopeCu) {
    TypeDeclaration type = getType(scopeCu);
    for (MethodDeclaration method : type.getMethods()) {
      if (method.getName().getIdentifier().equals(keySig.getMethodName())) {
        MethodSig methodSig = toMethodSig(method);
        if (methodSig.parameters().equals(keySig.parameters())) {
          return methodSig;
        }
      }
    }

    return null;
  }


}
