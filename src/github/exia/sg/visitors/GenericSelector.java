package github.exia.sg.visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

public abstract class GenericSelector<T extends ASTNode> extends ASTVisitor {
	private List<T> hits = new ArrayList<T>();
	
	private boolean accepted = false;
	
	@Override
	public final void preVisit(ASTNode node) {
		accepted = true;
		beforeVisit(node);
	}
	
	protected void beforeVisit(ASTNode node) {
	}
	
	public List<T> getHits() {
		if (!accepted) {
			throw new RuntimeException("Selector is not applied on any ASTNode");
		}
		return hits;
	}
	
	protected void addHit(T t) {
		hits.add(t);
	}
}
