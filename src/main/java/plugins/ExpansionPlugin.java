package plugins;

import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;

public class ExpansionPlugin implements Plugin {

	@Override
	public String getName() {
		return "ExpansionPlugin";
	}

	@Override
	public void init(JavacTask arg0, String... arg1) {
		System.out.println("Running!");
		arg0.addTaskListener(new ExpansionTaskListener(arg0));
	}
}
