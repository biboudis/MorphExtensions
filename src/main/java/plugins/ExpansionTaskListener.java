package plugins;

import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;

public class ExpansionTaskListener implements TaskListener {
	private JavacTask task;
	
	public ExpansionTaskListener(JavacTask javactask) {
		task = javactask;
	}

	@Override
	public void finished(TaskEvent taskEvent) {
		System.out.println("# Task Kind: " + taskEvent.getKind());	
//		if (arg0.getKind().equals(TaskEvent.Kind.ANALYZE)) {
//			System.out.println("# Start Processing Task:" + task);
//			JCTree tree = (JCTree) arg0.getCompilationUnit();
//			System.out.println("# End Processing Task:" + task);
//			tree.accept(new ExpansionTranslator(task));
//		}
	}

	@Override
	public void started(TaskEvent arg0) {
	}
}
