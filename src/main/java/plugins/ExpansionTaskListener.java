package plugins;

import visitors.ExpansionTranslator;

import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.tree.JCTree;

public class ExpansionTaskListener implements TaskListener {
	private JavacTask task;
	
	public ExpansionTaskListener(JavacTask arg0) {
		task = arg0;
	}

	@Override
	public void finished(TaskEvent arg0) {
		System.out.println("# Task Kind: " + arg0.getKind());
		
		if (arg0.getKind().equals(TaskEvent.Kind.ANALYZE)) {
			System.out.println("# Start Processing Task:" + task);
			JCTree tree = (JCTree) arg0.getCompilationUnit();
			System.out.println("# End Processing Task:" + task);
			tree.accept(new ExpansionTranslator(task));
		}
	}

	@Override
	public void started(TaskEvent arg0) {
	}
}
