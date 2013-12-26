package checkers;

import javax.lang.model.element.TypeElement;

import visitors.InstantiationTranslator;

import com.sun.source.util.TreePath;
import com.sun.tools.javac.tree.JCTree;

import annotations.For;
import annotations.Morph;
import checkers.basetype.BaseTypeChecker;
import checkers.quals.TypeQualifiers;
import checkers.source.SourceChecker;
import checkers.source.SourceVisitor;

@TypeQualifiers({Morph.class})
public class MorphChecker extends SourceChecker {

	@Override
	public void typeProcess(TypeElement elem, TreePath path) {
		
		super.typeProcess(elem, path);

		JCTree tree = (JCTree) path.getCompilationUnit();		
		
		tree.accept(new InstantiationTranslator(processingEnv, path));
	}

	@Override
	protected SourceVisitor<?, ?> createSourceVisitor() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
