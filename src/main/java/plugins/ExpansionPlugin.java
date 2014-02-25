package plugins;

import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;

public class ExpansionPlugin implements Plugin {

	@Override
	public String getName() {
		return "ExpansionPlugin";
	}

	@Override
	public void init(JavacTask task, String... arg1) {
		task.addTaskListener(new ExpansionTaskListener(task));
	}
}
