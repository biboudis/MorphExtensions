package checkers;

import java.io.IOException;
import java.io.Writer;

import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

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
		// Note taken from EnerJ: Run the SourceChecker base behavior *first* to set
		// currentPath, which must be set before BasicAnnotatedTypeFactory is
		// instantiated.
		super.typeProcess(elem, path);
		
		System.out.println(elem.toString());
		
		JCTree tree = (JCTree) path.getCompilationUnit();

//		System.out.println("Translating from:");
//        System.out.println(tree);
		
		try {
			JavaFileObject jfo = processingEnv.getFiler().createSourceFile(elem.getQualifiedName() + "Gen.java");
			Writer out = jfo.openWriter();
            out.write("class Gen { }");
            out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		
		tree.accept(new ExpansionTranslator(processingEnv, path));
	
//		System.out.println("Translating to:");
//        System.out.println(tree);
	}
}
