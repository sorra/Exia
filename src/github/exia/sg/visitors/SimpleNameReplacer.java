package github.exia.sg.visitors;


import github.exia.ast.util.StringMatcher;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SimpleName;



public class SimpleNameReplacer extends ASTVisitor {
	
	private StringMatcher strMatcher;
	private String neo;
	
	/**
	 * Renames SimpleName nodes
	 */
	public SimpleNameReplacer(StringMatcher strMatcher, String neo) {
		this.strMatcher = strMatcher;
		this.neo = neo;
	}
	
	@Override
	public boolean visit(SimpleName node) {

		if (strMatcher.matches(
				node.getIdentifier())) {
			node.setIdentifier(neo);
		}
		return true;
	}
}