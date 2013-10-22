package github.exia.sg.visitors;


import github.exia.ast.util.AstUtils;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclaration;



public class FieldReferenceSelector extends ASTVisitor {
	private static final boolean GO = true;
	
	private Set<String> hits = new HashSet<String>();

	public Set<String> getHits() {
		return hits;
	}

	@Override
	public boolean visit(SimpleName node) {
		ASTNode parent = node.getParent();
		if (parent instanceof Type 
				&& AstUtils.pureNameOfType((Type) parent).equals(node.getIdentifier())) {
			return GO;
		}
		if (parent instanceof MethodInvocation && ((MethodInvocation)parent).getName()==node) {
			return GO;
		}
		if (parent instanceof VariableDeclaration && ((VariableDeclaration)parent).getName()==node) {
			return GO;
		}
//		if (parent instanceof BodyDeclaration && ((BodyDeclaration)parent).getName()==node) {
//			return GO;
//		}
		
		hits.add(node.getIdentifier());

		return GO;
	}
}
