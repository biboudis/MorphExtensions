
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.junit.Test;

public class TestSyntheticClassUse {

//	@Test
//	public void TestHello() throws IOException {
//		
//		String[] compileOptions = new String[]{"-processor", "checkers.MorphChecker"} ;	
//		Iterable<String> compilationOptionss = 
//				Arrays.asList(compileOptions);
//		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
//		DiagnosticCollector<JavaFileObject> diagnostics = 
//				new DiagnosticCollector<JavaFileObject>();
//		StandardJavaFileManager fileManager = 
//				compiler.getStandardFileManager(diagnostics, Locale.getDefault(), null);
//		Iterable<? extends JavaFileObject> compilationUnits = 
//				fileManager.getJavaFileObjects(new File("resources/Hello.java"));
//		compiler.getTask(null, fileManager, diagnostics, compilationOptionss, null,
//				compilationUnits).call();
//
//		for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics())
//			System.out.format("Error on line %d in %d%n",
//					diagnostic.getLineNumber(), diagnostic.getSource());
//
//		fileManager.close();
//	}
}
