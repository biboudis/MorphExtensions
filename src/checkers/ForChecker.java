package checkers;

import javax.lang.model.element.TypeElement;

import com.sun.source.util.TreePath;
import com.sun.tools.javac.tree.JCTree;

import annotations.For;
import checkers.basetype.BaseTypeChecker;
import checkers.quals.TypeQualifiers;
import checkers.source.SourceChecker;
import checkers.source.SourceVisitor;
import expanders.StaticForMethodExpander;

@TypeQualifiers({For.class})
public class ForChecker extends SourceChecker {

	@Override
	public void typeProcess(TypeElement elem, TreePath path) {
		JCTree tree = (JCTree) path.getCompilationUnit();		
		tree.accept(new StaticForMethodExpander(processingEnv, path));
	}

	@Override
	protected SourceVisitor<?, ?> createSourceVisitor() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
