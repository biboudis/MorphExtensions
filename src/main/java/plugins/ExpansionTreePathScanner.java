package plugins;

import com.sun.source.tree.VariableTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import javax.lang.model.util.Types;

public class ExpansionTreePathScanner extends TreePathScanner<Void, Void> {
	
	private final SourcePositions sourcePositions;
	private final Trees trees;
	
	private final Types types;

	public ExpansionTreePathScanner(JavacTask task) {
		types = task.getTypes();
		trees = Trees.instance(task);
		sourcePositions = trees.getSourcePositions();
	}

	@Override
	public Void visitVariable(VariableTree node, Void p) {
		
		return super.visitVariable(node, p);
	}
}
