package com.iostate.exia.ast;

import java.util.*;

import com.iostate.exia.util.Assert;
import com.iostate.exia.core.SourcePaths;
import com.iostate.exia.ast.visitors.GenericSelector;
import com.iostate.exia.core.CuBase;
import org.eclipse.jdt.core.dom.*;
import com.iostate.exia.util.*;

public class AstFind {
  private static MyLogger logger = MyLogger.getLogger(AstFind.class);

  public static String qnameOfTypeRef(Type type) {
    String typeName = typeName(type);
    if (typeName.isEmpty()) {
      return "";
    }
    CompilationUnit cu = FindUpper.cu(type);
    return qnameOfTypeRef(typeName, cu);
  }

  public static String qnameOfTypeRef(String typeName, CompilationUnit cu) {
    if (typeName.contains(".") && StringUtil.isNotCapital(typeName)) {
      // Has qualifier and is not inner-class
      return typeName;
    }
    if (PrimitiveTypeUtil.isPrimitive(typeName) || langTypes.contains(typeName)) {
      return typeName;
    }
    if (cu == null) {
      throw new IllegalArgumentException("The name is not in a CompilationUnit!");
    }

    String asLocal = cu.getPackage().getName().getFullyQualifiedName() + "." + typeName;
    if (SourcePaths.containsQname(asLocal)) {
      return asLocal;
    }

    List<ImportDeclaration> imports = cu.imports();
    for (ImportDeclaration imp : imports) {
      if (imp.isOnDemand()) {
        String hit = imp.getName().getFullyQualifiedName() + "." + typeName;
        if (SourcePaths.containsQname(hit)) {
          return hit;
        }
      } else {
        String impName = imp.getName().getFullyQualifiedName();
        if (StringUtil.simpleName(impName).equals(typeName)) {
          return impName;
        }
      }
    }
    return typeName;
  }

  public static String simpleNameOfTypeRef(Type type) {
    String s = typeName(type);
    return StringUtil.simpleName(s);
  }

  public static String qnameOfTopTypeDecl(SimpleName name) {
    String refName = name.getIdentifier();
    CompilationUnit cu = FindUpper.cu(name);
    if (cu == null) {
      throw new IllegalArgumentException("The name is not in a CompilationUnit!");
    }
    return cu.getPackage().getName().toString().trim() + "." + refName;
  }

  private static String typeName(Type type) {
    if (type instanceof SimpleType) {
      return ((SimpleType) type).getName().toString().trim();
    }
    if (type instanceof ArrayType) {
      return typeName(((ArrayType) type).getElementType());
    }
    if (type instanceof ParameterizedType) {
      return typeName(((ParameterizedType) type).getType());
    }
    if (type instanceof AnnotatableType) {
      AnnotatableType noAnnoType = (AnnotatableType) ASTNode.copySubtree(type.getAST(), type);
      noAnnoType.annotations().clear();
      return noAnnoType.toString().trim();
    }
    return type.toString().trim();
  }

  public static List<TypeDeclaration> superClasses(TypeDeclaration td) {
    List<TypeDeclaration> supers = new ArrayList<>();
    String name = null;
    while (true) {
      if (td.getSuperclassType() != null) {
        name = td.getSuperclassType().toString().trim();
      }
      if (name == null) {
        break;
      }
      if (!name.contains(".")) {
        ImportDeclaration imp = findImportByLastName(name, ((CompilationUnit) td.getParent()).imports());
        assert imp != null;
        name = imp.getName().getFullyQualifiedName();
      }
      supers.add((TypeDeclaration) CuBase.getCuByQname(name).types().get(0));
    }
    return supers;
  }

  public static ImportDeclaration findImportByLastName(String name, List<ImportDeclaration> imports) {
    for (ImportDeclaration imp : imports) {
      String impFullName = imp.getName().getFullyQualifiedName();
      String impLastName = impFullName.substring(impFullName.lastIndexOf('.') + 1);
      if (impLastName.equals(name)) {
        return imp;
      }
    }
    return null;
  }

  public static ImportDeclaration findImport(String name, List<ImportDeclaration> imports) {
    for (ImportDeclaration imp : imports) {
      if (imp.getName().getFullyQualifiedName().equals(name)) {
        return imp;
      }
    }
    return null;
  }

  public static boolean hasModifierKeyword(List modifiers, Modifier.ModifierKeyword keyword) {
    for (Object mod : modifiers) {
      if (mod instanceof Modifier) {
        Modifier.ModifierKeyword keyword1 = ((Modifier) mod).getKeyword();
        if (keyword.equals(keyword1)) {
          return true;
        }
      }
    }
    return false;
  }

  static AbstractTypeDeclaration typeDecl(String qname) {
    return (AbstractTypeDeclaration) CuBase.getCuByQname(qname).types().get(0);
  }

  /**
   * @param sn the symbol to resolve
   * @return pure type name, null if not found
   */
  public static String findDeclType(SimpleName sn) {
    {
      DeclTypeSelector declTypeSelector = new DeclTypeSelector(sn.getIdentifier());
      MethodDeclaration upperMethod = FindUpper.methodScope(sn);
      if (upperMethod != null) {
        FindUpper.methodScope(sn).accept(declTypeSelector);
        if (declTypeSelector.getHits().size() > 0) {
          return AstUtils.pureNameOfType(declTypeSelector.getHits().get(0));
        }
      }
    }

    List<Type> hits = findDeclTypeInClass(sn, FindUpper.typeScope(sn));
    Assert.assertTrue(hits.size() <= 1);
    if (hits.size() == 1) {
      return AstUtils.pureNameOfType(hits.get(0));
    }

    return null;
  }

  private static List<Type> findDeclTypeInClass(SimpleName sn, String qname) {
    if (qname == null) {
      return Collections.emptyList();
    }
    String queryName = qname.replace('.', '/') + ".java";
    String pathHit = null;
    for (String path : SourcePaths.sourcePaths().values()) {
      if (path.endsWith(queryName)) {
        pathHit = path;
        break;
      }
    }
    if (pathHit == null) {// Not found this class
      logger.log(qname + " not found");
      return Collections.emptyList();
    }
    try {
      TypeDeclaration typeclass = AstUtils.getType(CuBase.getCuByPath(pathHit));
      return findDeclTypeInClass(sn, typeclass);
    } catch (RuntimeException e) {
      logger.log("pathHit: " + pathHit);
      logger.log(CuBase.getCuByPath(pathHit));
      throw e;
    }
  }

  private static List<Type> findDeclTypeInClass(SimpleName sn, TypeDeclaration typeclass) {
    FieldDeclaration[] fields = typeclass.getFields();
    String superQname = null;
    if (typeclass.getSuperclassType() != null) {
      final String superclassName = typeclass.getSuperclassType().toString().trim();
      if (superclassName.contains(".")) {
        superQname = superclassName;
      }
      final CompilationUnit cu = FindUpper.cu(typeclass);
      assert cu != null;
      ImportDeclaration imp = AstUtils.findImportByLastName(superclassName, cu.imports());
      if (imp != null) superQname = imp.getName().getFullyQualifiedName();
      else superQname = cu.getPackage().getName().getFullyQualifiedName() + '.' + superclassName;
    }
    if (fields.length == 0) {
      return findDeclTypeInClass(sn, superQname);
    } else {
      DeclTypeSelector declTypeSelector = new DeclTypeSelector(sn.getIdentifier());
      for (FieldDeclaration field : fields) {
        field.accept(declTypeSelector);
      }
      for (Iterator<Type> it = declTypeSelector.getHits().iterator(); it.hasNext(); ) {
        Type hit = it.next();
        if (hit.getParent() instanceof FieldDeclaration == false) {
          it.remove();
        }
      }
      if (declTypeSelector.getHits().isEmpty()) {
        return findDeclTypeInClass(sn, superQname);
      } else return declTypeSelector.getHits();
    }
  }

  private static class DeclTypeSelector extends GenericSelector<Type> {
    private String name;

    DeclTypeSelector(String name) {
      this.name = name;
    }

    @Override
    public boolean visit(SimpleName node) {
      ASTNode p = node.getParent();
      if (node.getIdentifier().equals(name)) {
        if (p instanceof VariableDeclarationFragment) {
          ASTNode pp = p.getParent();
          if (pp instanceof VariableDeclarationStatement)
            addHit(((VariableDeclarationStatement) pp).getType());
          else if (pp instanceof FieldDeclaration)
            addHit(((FieldDeclaration) pp).getType());
        } else if (p instanceof SingleVariableDeclaration) {
          addHit(((SingleVariableDeclaration) p).getType());
        }
      }
      return true;
    }
  }


  private static Set<String> langTypes = new HashSet<>(Arrays.asList(
      "AbstractMethodError", "AbstractStringBuilder", "Appendable", "ApplicationShutdownHooks",
      "ArithmeticException", "ArrayIndexOutOfBoundsException", "ArrayStoreException",
      "AssertionError", "AssertionStatusDirectives", "AutoCloseable", "Boolean", "BootstrapMethodError",
      "Byte", "Character", "CharacterData", "CharacterData00", "CharacterData0E", "CharacterData01",
      "CharacterData02", "CharacterDataLatin1", "CharacterDataPrivateUse", "CharacterDataUndefined",
      "CharacterName", "CharSequence", "Class", "ClassCastException", "ClassCircularityError",
      "ClassFormatError", "ClassLoader", "ClassLoaderHelper", "ClassNotFoundException", "ClassValue",
      "Cloneable", "CloneNotSupportedException", "Comparable", "Compiler", "ConditionalSpecialCasing",
      "Deprecated", "Double", "Enum", "EnumConstantNotPresentException", "Error", "Exception",
      "ExceptionInInitializerError", "Float", "FunctionalInterface", "IllegalAccessError",
      "IllegalAccessException", "IllegalArgumentException", "IllegalMonitorStateException",
      "IllegalStateException", "IllegalThreadStateException", "IncompatibleClassChangeError",
      "IndexOutOfBoundsException", "InheritableThreadLocal", "InstantiationError", "InstantiationException",
      "Integer", "InternalError", "InterruptedException", "Iterable", "LinkageError", "Long", "Math",
      "NegativeArraySizeException", "NoClassDefFoundError", "NoSuchFieldError", "NoSuchFieldException",
      "NoSuchMethodError", "NoSuchMethodException", "NullPointerException", "Number", "NumberFormatException",
      "Object", "OutOfMemoryError", "Override", "Package", "Process", "ProcessBuilder", "ProcessEnvironment",
      "ProcessImpl", "Readable", "ReflectiveOperationException", "Runnable", "Runtime", "RuntimeException",
      "RuntimePermission", "SafeVarargs", "SecurityException", "SecurityManager", "Short", "Shutdown",
      "StackOverflowError", "StackTraceElement", "StrictMath", "String", "StringBuffer", "StringBuilder",
      "StringCoding", "StringIndexOutOfBoundsException", "SuppressWarnings", "System", "SystemClassLoaderAction",
      "Terminator", "Thread", "ThreadDeath", "ThreadGroup", "ThreadLocal", "Throwable", "TypeNotPresentException",
      "UNIXProcess", "UnknownError", "UnsatisfiedLinkError", "UnsupportedClassVersionError",
      "UnsupportedOperationException", "VerifyError", "VirtualMachineError", "Void"));
}
