package expanders;

import javax.annotation.processing.ProcessingEnvironment;

import com.sun.source.util.TreePath;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;

public class StaticForMethodExpander extends TreeTranslator {

	private Context context;
	private TreePath path;

	public StaticForMethodExpander(ProcessingEnvironment env, TreePath p) {
		super();
		context = ((JavacProcessingEnvironment) env).getContext();
		path = p;
	}

	@Override
	public void visitMethodDef(JCMethodDecl tree) {
		super.visitMethodDef(tree);
	}
	
}
