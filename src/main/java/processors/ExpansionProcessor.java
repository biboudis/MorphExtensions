package processors;

import java.io.Writer;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;

import javax.lang.model.SourceVersion;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import utils.Debug;
import visitors.ExpansionTranslator;

import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacFiler;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ExpansionProcessor extends AbstractProcessor {
	private JavacProcessingEnvironment processingEnv;
	private Context context;
	private Trees trees;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);

		this.processingEnv = (JavacProcessingEnvironment) processingEnv;
		this.context = this.processingEnv.getContext();
		this.trees = Trees.instance(processingEnv);
		Debug.setMessager(processingEnv.getMessager());
	}

	@Override
	public boolean process(Set<? extends TypeElement> arg0,
			RoundEnvironment roundEnvironment) {

		System.out.println("# ExpansionProcessor");

		if (roundEnvironment.processingOver())
			return false;

		Set<? extends Element> elementSet = roundEnvironment.getRootElements();

		if (elementSet.size() > 0)
			System.out
					.println("# ExpansionProcessor: root element set to process "
							+ elementSet.toString());

		for (Element e : elementSet) {
			JCCompilationUnit tree = toCompilationUnit(e);

			tree.accept(new ExpansionTranslator(context));

			processingEnv.getMessager().printMessage(Kind.NOTE,
					e + " synthetic classes rewritten.");
		}

		// Generating a dummy file; doesn't seem to do anything.
//		if (dummyCount == 0)
//			ExpansionProcessor.createDummySourceFile(
//					(JavacFiler) processingEnv.getFiler(), processingEnv);

		return false;
	}

	/**
	 * Inspired by Project Lombok to avoid the warning: Supported source version
	 * 'RELEASE_7' from annotation processor 'processors.ExpansionProcessor'
	 * less than -source '1.8'
	 */
	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.values()[SourceVersion.values().length - 1];
	}

	private static int dummyCount = 0;

	/**
	 * Inspired by Project Lombok to enforce new round after transforming AST
	 * trees to something invalid prior to expansion.
	 * */
	public static void createDummySourceFile(JavacFiler filer,
			JavacProcessingEnvironment processingEnv) {

		if (!filer.newFiles()) {
			System.out.println("# Generating a dummy file.");
			try {
				JavaFileObject dummy = filer
						.createSourceFile("dummy.ForceNewRound"
								+ (dummyCount++));
				Writer w = dummy.openWriter();
				w.close();
			} catch (Exception e) {
				e.printStackTrace();
				processingEnv
						.getMessager()
						.printMessage(Kind.WARNING,
								"Can't force a new processing round. MorphExsentions cannot work.");
			}
		}
	}

	private JCCompilationUnit toCompilationUnit(Element element) {
		TreePath path = trees == null ? null : trees.getPath(element);
		if (path == null)
			return null;

		return (JCCompilationUnit) path.getCompilationUnit();
	}

}
