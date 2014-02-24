package checkers;

import javax.lang.model.element.TypeElement;

import visitors.ExpansionTranslator;
import annotations.Morph;
import checkers.basetype.BaseTypeChecker;
import checkers.quals.TypeQualifiers;

import com.sun.source.util.TreePath;
import com.sun.tools.javac.tree.JCTree;

@TypeQualifiers({ Morph.class })
public class MorphChecker extends BaseTypeChecker {

	@Override
	public void typeProcess(TypeElement elem, TreePath path) {
		super.typeProcess(elem, path);
	}
}
