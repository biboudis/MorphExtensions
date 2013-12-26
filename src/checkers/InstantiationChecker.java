package checkers;

import com.sun.source.util.TreePath;
import com.sun.tools.javac.tree.JCTree;

import annotations.For;
import annotations.Morph;
import checkers.basetype.BaseTypeChecker;
import checkers.quals.TypeQualifiers;
import checkers.source.SourceChecker;
import checkers.source.SourceVisitor;
import expanders.StaticForMethodExpander;

@TypeQualifiers({Morph.class})
public class InstantiationChecker extends SourceChecker {

	@Override
	protected SourceVisitor<?, ?> createSourceVisitor() {
		// TODO Auto-generated method stub
		return null;
	}

}
