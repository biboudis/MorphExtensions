package checkers;

import javax.lang.model.element.TypeElement;

import com.sun.source.util.TreePath;
import com.sun.tools.javac.tree.JCTree;

import annotations.For;
import checkers.basetype.BaseTypeChecker;
import checkers.quals.TypeQualifiers;
import expanders.StaticForMethodExpander;

@TypeQualifiers({For.class})
public class ExpansionChecker extends BaseTypeChecker {

	@Override
	public void typeProcess(TypeElement elem, TreePath path) {
		JCTree tree = (JCTree) path.getCompilationUnit();
		
		tree.accept(new StaticForMethodExpander(processingEnv, path));
	}
	
}
