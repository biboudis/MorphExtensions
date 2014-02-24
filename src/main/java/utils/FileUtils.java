package utils;

import java.io.IOException;
import java.io.Writer;
import java.util.Formatter;
import java.util.Locale;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.PackageElement;
import javax.tools.JavaFileObject;

public class FileUtils {
	
	public static void createDummySourceFile(
			ProcessingEnvironment processingEnv, 
			PackageElement pkgElement, 
			String className) {
		Formatter formatter = null;
		Writer writer = null;
		try {
		    System.out.println("# creating the dummy source file"); 
		    JavaFileObject jfo = processingEnv.getFiler().createSourceFile(className, pkgElement);
		    writer = jfo.openWriter();

		    StringBuilder sb = new StringBuilder();
		    formatter = new Formatter(sb, Locale.US);
		    formatter.format("class %s { }", className);
		    
		    writer.write(sb.toString());
		    writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			formatter.close();
		}
	}
}
