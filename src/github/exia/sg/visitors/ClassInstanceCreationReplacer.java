package github.exia.sg.visitors;


import github.exia.ast.util.AstUtils;
import github.exia.ast.util.StringMatcher;

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
				AstUtils.pureNameOfType(node.getType()) )) {
			AST ast = node.getAST();
			node.setType(ast.newSimpleType(ast.newSimpleName(neo)));
		}
		return true;
	}
}
