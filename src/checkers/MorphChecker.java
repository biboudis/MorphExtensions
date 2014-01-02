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

/**
 * @author bibou
 * 
 */
@TypeQualifiers({ Morph.class })
public class MorphChecker extends BaseTypeChecker {

	@Override
	public void typeProcess(TypeElement elem, TreePath path) {
		// Note taken from EnerJ: Run the SourceChecker base behavior *first* to set
		// currentPath, which must be set before BasicAnnotatedTypeFactory is
		// instantiated.
		super.typeProcess(elem, path);
		
		JCTree tree = (JCTree) path.getCompilationUnit();

		System.out.println("Translating from:");
        System.out.println(tree);
		
		tree.accept(new InstantiationTranslator(processingEnv, path));
		
		System.out.println("Translating to:");
        System.out.println(tree);
	}
}