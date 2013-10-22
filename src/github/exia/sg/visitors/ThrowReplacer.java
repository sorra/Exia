package github.exia.sg.visitors;


import github.exia.ast.util.StringMatcher;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.Type;



@Deprecated
public class ThrowReplacer extends ASTVisitor {
	private StringMatcher prevExNameMatcher;
	private String newExName;
	
	public ThrowReplacer(StringMatcher prevExNameMatcher, String newExName){
		this.prevExNameMatcher = prevExNameMatcher;
		this.newExName = newExName;
	}
	
	@Override
	public boolean visit(ThrowStatement node) {
		Expression expression = node.getExpression();
		if (expression instanceof ClassInstanceCreation) {
			Type type = ((ClassInstanceCreation)expression).getType();
			if (type instanceof SimpleType) {
				replaceIfNameMatch((SimpleType)type);
			} else {
				throw new RuntimeException("not supported condition! Case: " + type);
			}
		} else {
			throw new RuntimeException("not supported condition! Case: " + node);
		}
		
		return true;
	}

	private void replaceIfNameMatch(SimpleType simpleType) {
		if ( prevExNameMatcher.matches(simpleType.getName().getFullyQualifiedName()) ) {
			simpleType.setName(simpleType.getAST().newName(newExName));
		}
	}
	
}
