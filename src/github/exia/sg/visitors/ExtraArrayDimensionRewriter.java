package github.exia.sg.visitors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;

/**
 * Rewrite "T a[]" to "T[] a"
 * NOTE: only have effects on method signatures
 */
public class ExtraArrayDimensionRewriter extends ASTVisitor {
	
	@Override
	public boolean visit(SingleVariableDeclaration node) {
		AST ast = node.getAST();
		if (node.getExtraDimensions() > 0) {
			Type type = (Type) ASTNode.copySubtree(ast, node.getType());
			for (int i = node.getExtraDimensions(); i > 0; i--) {
				type = ast.newArrayType(type);
			}
			node.setType(type);
			node.setExtraDimensions(0);
		}
		
		return true;
	}
}
