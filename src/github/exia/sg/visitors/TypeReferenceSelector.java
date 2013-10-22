package github.exia.sg.visitors;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;

public class TypeReferenceSelector extends ASTVisitor {
	private static final boolean GO = true;
	
	private Set<String> hits = new HashSet<String>();
	
	public Set<String> getHits() {
		return hits;
	}
	
	@Override
	public boolean visit(SimpleName node) {
		ASTNode parent = node.getParent();
		if (parent instanceof Type || parent instanceof Expression) {
			hits.add(node.getIdentifier());
		}
		return GO;
	}
	
	// To be considered!
//	@Override
//	public boolean visit(QualifiedName node) {
//		if (!(node.getParent() instanceof QualifiedName)) {
//			System.out.println("#Meet QualifiedName! " + node.getFullyQualifiedName());	
//		}
//		return GO;
//	}

}
