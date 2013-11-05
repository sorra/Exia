package github.exia.ast.util;

import github.exia.sg.visitors.GenericSelector;
import github.exia.util.MyLogger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WildcardType;
import org.junit.Assert;

@SuppressWarnings("unchecked")
public class AstUtils {
	private static MyLogger logger = MyLogger.getLogger(AstUtils.class);

	/**
	 * An excellent method! Feel free to use it!
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

  /**
	 * This algorithm had better get reviewed.
	 * Returns null if not found until it meets Block
	 */
	public static Statement findUpperStatement(ASTNode node) {
		ASTNode parent = node.getParent();

		if (parent == null) {
			return null;
		}

		if (parent instanceof Statement) {
			return (Statement) parent;
		}
		else if (parent instanceof Block) {
			return null;
		}

		return findUpperStatement(parent);
	}

	/**
	 * Returns null if not found until it meets TypeDeclaration
	 */
	public static MethodDeclaration findUpperMethodScope(ASTNode node) {
		ASTNode parent = node.getParent();

		if (parent == null) {
			return null;
		}

		if (parent instanceof MethodDeclaration) {
			return (MethodDeclaration) parent;
		}
		else if (parent instanceof AbstractTypeDeclaration) {
			return null;
		} 

		return findUpperMethodScope(parent);
	}
	
	/**
	 * Returns null if not found until it meets MethodDeclaration
	 */
	public static TryStatement findUpperTryScope(ASTNode node) {
		ASTNode parent = node.getParent();

		if (parent == null) {
			return null;
		}
		if (parent instanceof TryStatement) {
			return (TryStatement) parent;
		}
		else if (parent instanceof MethodDeclaration) {
			return null;
		}
		
		return findUpperTryScope(parent);
	}
	
	public static CatchClause findUpperCatch(ASTNode node) {
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

      return findUpperCatch(parent);
	}

	/**
	 * Returns null if not found until it meets CompilationUnit
	 */
	public static TypeDeclaration findUpperTypeScope(ASTNode node) {
		ASTNode parent = node.getParent();

		if (parent == null) {
			return null;
		}

		if (parent instanceof TypeDeclaration) {
			return (TypeDeclaration) parent;
		}
		else if (parent instanceof AbstractTypeDeclaration) {
		    throw new UnsupportedOperationException();
		}
		else if (parent instanceof CompilationUnit) {
			return null;
		}

		return findUpperTypeScope(parent);
	}
	
	/**
	 * Returns null if not found
	 */
	public static CompilationUnit findUpperCu(ASTNode node) {
		ASTNode parent = node.getParent();
		
		if (parent == null) {
			return null;
		}
		
		if (parent instanceof CompilationUnit) {
			return (CompilationUnit) parent;
		}

		return findUpperCu(parent);
	}
	
    public static FieldDeclaration findFieldByName(String name, TypeDeclaration type) {
      for (FieldDeclaration field : type.getFields()) {
        for (Object frag : field.fragments()) {
          if (((VariableDeclarationFragment) frag)
              .getName().getIdentifier().equals(name)) {
            return field;
          }
        }
      }
      return null;
    }
    
    /**
     * returns the first one met
     */
    public static MethodDeclaration findMethodByName(String name, AbstractTypeDeclaration type) {
      for (Object member : type.bodyDeclarations()) {
        if (member instanceof MethodDeclaration) {
          MethodDeclaration method = (MethodDeclaration) member;
          if (method.getName().getIdentifier().equals(name)) {
            return method;
          }
        }
      }
      return null;
    }
    
    public static ImportDeclaration findImportByLastName(String name, List<ImportDeclaration> imports) {
      for (ImportDeclaration imp : imports) {
        String impFullName = imp.getName().getFullyQualifiedName();
        String impLastName = impFullName.substring(impFullName.lastIndexOf('.')+1);
        if (impLastName.equals(name)) {
          return imp;
        }
      }
      return null;
    }

	public static boolean typeHasName(SimpleType type, String name) {
		if (type.getName().getFullyQualifiedName().equals(name)) {
			return true;
		}
		else return false;
	}
	
	public static String pureNameOfType(Type type) {
		if (type instanceof PrimitiveType) {
			return ((PrimitiveType) type).getPrimitiveTypeCode().toString();
		}
		if (type instanceof ArrayType) {
			// Compatible with Octopus(ASM) representation
			return "["+pureNameOfType( ((ArrayType) type).getComponentType() );
		}
		if (type instanceof SimpleType) {
			return ((SimpleType) type).getName().getFullyQualifiedName();
		}
		if (type instanceof ParameterizedType) {
			return pureNameOfType( ((ParameterizedType) type).getType() );
		}
		if (type instanceof QualifiedType) {
			logger.log("Meets QualifiedType: " + type.toString());
		}
		if (type instanceof WildcardType) {
			logger.log("Meets WildCardType: " + type.toString());
		}
		
		return type.toString();
	}
	
	public static String qname(TypeDeclaration type) {
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
	  else return null;
	}
	
	public static boolean isTypeSerializable(TypeDeclaration type) {
      List<Type> interfaces = type.superInterfaceTypes();
      for (Type intf : interfaces) {
        if (intf.toString().contains("Serializable")) {
          return true;
        }
      }
      return false;
	}
	
	public static MethodSig toMethodSig(MethodDeclaration method) {
		TypeDeclaration type = (TypeDeclaration) method.getParent();
		CompilationUnit cu = (CompilationUnit)type.getParent();
		
		String methodName = method.getName().getIdentifier();
		String packageName = cu.getPackage().getName().getFullyQualifiedName();
		String className = type.getName().getIdentifier();
		
		List<SingleVariableDeclaration> pars = method.parameters();
		List<String> emParameters = new ArrayList<String>();
		for (SingleVariableDeclaration par : pars) {
			String typename = AstUtils.pureNameOfType(par.getType());
			emParameters.add(typename);
		}
		return new MethodSig(
				packageName, className, methodName, emParameters);
	}
	
	public static List<MethodSig> toMethodSigs(List<MethodDeclaration> methods) {
		List<MethodSig> methodSigs = new LinkedList<MethodSig>();
		for (MethodDeclaration method : methods) {
			methodSigs.add(AstUtils.toMethodSig(method));
		}
		return methodSigs;
	}

	public static List<MethodDeclaration> listMethods(CompilationUnit cu,
			ModifierKeyword modifierKeyword) {
		TypeDeclaration ejbClass = (TypeDeclaration) cu.types().get(0);

		List<MethodDeclaration> ejbMethods = new LinkedList<MethodDeclaration>();
		if (modifierKeyword == null) {
			for (MethodDeclaration method : ejbClass.getMethods()) {
				ejbMethods.add(method);
			}
		} else {
			for (MethodDeclaration method : ejbClass.getMethods()) {
				for (Object o : method.modifiers()) {
					if (o instanceof Modifier && ((Modifier) o).getKeyword() == modifierKeyword) {
						ejbMethods.add(method);
					}
				}
			}
		}
		return ejbMethods;
	}
	
	static boolean isExternal(MethodDeclaration method, CompilationUnit remoteCu) {
		if(!hasModifierKeyword(method, ModifierKeyword.PUBLIC_KEYWORD)) {
			return false;
		}
		if (findRelevantMethodSig(toMethodSig(method), remoteCu) != null) {
			return true;
		}
		return false;
	}
	
	public static boolean isSelfCallGrammar(MethodInvocation mi) {
		return mi.getExpression() == null || mi.getExpression() instanceof ThisExpression;
	}
	
	public static boolean isClientCallGrammar(MethodInvocation mi) {
		return !isSelfCallGrammar(mi);
	}
	
	public static boolean hasModifierKeyword(BodyDeclaration decl, ModifierKeyword keyword) {
	    Assert.assertNotNull(decl);
	    Assert.assertNotNull(keyword);
		for (Object each : decl.modifiers()) {
			if (each instanceof Modifier) {
				if ( ((Modifier) each).getKeyword().equals(keyword) ) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean isFieldInjectable(FieldDeclaration fd) {
	  for (Object modifier : fd.modifiers()) {
	    if (modifier instanceof Annotation) {
	      Annotation annotation = (Annotation)modifier;
	      String annoTypeName = annotation.getTypeName().getFullyQualifiedName();
	      if (annoTypeName.equals("In")) {
	        return true;
	      }
	      else if (annoTypeName.contains("In")) {
	        logger.log("Strange annotation type: " + annoTypeName);
	      }
	    }
	  }
	  return false;
	}

	/**
	 * Matches if methodName and parameters are equal
	 * @param keySig search key
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

	/**
   * 
   * @param sn the symbol to resolve
   * @return pure type name, null if not found
   */
  public static String findDeclType(SimpleName sn) {
    DeclTypeSelector declTypeSelector = new DeclTypeSelector(sn.getIdentifier());
    MethodDeclaration upperMethod = AstUtils.findUpperMethodScope(sn);
    if (upperMethod != null) {
      AstUtils.findUpperMethodScope(sn).accept(declTypeSelector);
      if (declTypeSelector.getHits().size() > 0) {
        return AstUtils.pureNameOfType(declTypeSelector.getHits().get(0));
      }
    }
    
    declTypeSelector = new DeclTypeSelector(sn.getIdentifier());
    FieldDeclaration[] fields = AstUtils.findUpperTypeScope(sn).getFields();
    if (fields.length == 0) return null;
    for (FieldDeclaration field : fields) {
      field.accept(declTypeSelector);
    }

    Assert.assertTrue(declTypeSelector.getHits().size() <= 1);
    if (declTypeSelector.getHits().size() == 1) {
      return AstUtils.pureNameOfType(declTypeSelector.getHits().get(0));
    }
    
    return null;
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
        }
        else if (p instanceof SingleVariableDeclaration) {
          addHit(((SingleVariableDeclaration) p).getType());
        }
      }
      return true;
    }
    
    @Deprecated
    private void checkFoundCount(DeclTypeSelector declTypeSelector) {
      String firstHitStr = declTypeSelector.getHits().get(0).toString();
      int diffCount = 0;
      for (Iterator<Type> it = declTypeSelector.getHits().listIterator(1); it.hasNext();) {
        if (!it.next().toString().equals(firstHitStr))
          diffCount++;
      }
      if (diffCount > 0) {
        logger.log("[WARN] decl types found in methodScope: " + declTypeSelector.getHits());
      }
    }
  }
}
