package github.exia.samples;

import github.exia.filewalker.AstFunction;
import github.exia.filewalker.FileWalker;
import github.exia.provided.JavaSourceFileFilter;
import github.exia.sg.visitors.GenericSelector;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * Detects and removes unused imports in java files
 */
public class UnusedImportDeletor {
  public static void main(String[] args) {
    String[] roots = args;
    
    FileFilter filter = new JavaSourceFileFilter();
    
    AstFunction function = new AstFunction() {
      @Override
      public boolean doAndModify(final CompilationUnit cu, File file) {
        boolean modified = false;
        List<ImportDeclaration> imports = new ArrayList<ImportDeclaration>(cu.imports());
        for (ImportDeclaration imp : imports) {
          if (imp.isOnDemand()) {
            continue;
          }
          String fullName = imp.getName().getFullyQualifiedName();
          String lastName = fullName.substring(fullName.lastIndexOf('.')+1);
          if (searchHits(lastName, cu).isEmpty()) {
            imp.delete();
            modified = true;
          }
        }
        
        return modified;
      }
      
      private List<SimpleName> searchHits(final String name, CompilationUnit cu) {
        GenericSelector<SimpleName> importUsageFinder = new GenericSelector<SimpleName>() {
          @Override
          public boolean visit(SimpleName sn) {
            if (sn.getIdentifier().equals(name)) {
              addHit(sn);
            }
            return true;
          }
        };
        for (Object type : cu.types()) {
          ((ASTNode) type).accept(importUsageFinder);
        }
        return importUsageFinder.getHits();
      }
    };
    
    FileWalker.launch(roots, filter, function);
  }
}
