package github.exia.sg.visitors;


import github.exia.ast.util.StringMatcher;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ImportDeclaration;



public class ImportDeclarationDeletor extends ASTVisitor {
	
	private StringMatcher matcher;
	
	public ImportDeclarationDeletor(StringMatcher matcher) {
		this.matcher = matcher;
	}
	
	@Override
	public boolean visit(ImportDeclaration node) {
		if (matcher.matches(
				node.getName().getFullyQualifiedName())) {
			node.delete();
		}
		return true;
	}
}
