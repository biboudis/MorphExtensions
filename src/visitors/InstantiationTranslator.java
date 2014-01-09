package visitors;

import javax.annotation.processing.ProcessingEnvironment;

import annotations.Morph;
import checkers.types.AnnotatedTypeFactory;

import com.sun.source.util.TreePath;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.MemberEnter;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;

public class InstantiationTranslator extends TreeTranslator {

	protected static final String SYN_PREFIX = "__";
	
	protected Context context;
	protected TreeMaker maker;
	protected Names names;
	protected Enter enter;
	protected MemberEnter memberEnter;
	protected TreePath path;
	protected Attr attr;
	protected AnnotatedTypeFactory atypeFactory;
	
	public InstantiationTranslator(
			ProcessingEnvironment processingEnv,
			TreePath path) {
		
        context = ((JavacProcessingEnvironment) processingEnv).getContext();
        maker = TreeMaker.instance(context);
        names = Names.instance(context);
        enter = Enter.instance(context);
        memberEnter = MemberEnter.instance(context);
        attr = Attr.instance(context);	
        // atypeFactory = createFactory(path.getCompilationUnit());

	}
	
	@Override
	public void visitVarDef(JCVariableDecl tree) {

		/*	
		JCExpression init = null;
	    
        if (tree.init != null)
            init = maker.Ident(tree.name);
        else
            init = maker.Literal(TypeTag.BOT, null);
        */
		
		if (tree.getType().type.tsym.getAnnotation(Morph.class) != null) {
			JCExpression newType = makeDotExpression("__Logged$Stack");
			JCVariableDecl newVarDef = maker.VarDef(tree.mods, tree.name, newType, tree.init);
			JCExpression newInit = maker.NewClass(null, null, newType, List.<JCExpression> nil(), null);
			newVarDef.init = newInit;
			System.out.println("# Old: \n" + tree);
			System.out.println("# New: \n" + newVarDef);
			result = newVarDef;
		}
		super.visitVarDef(tree);
	}
	
    public JCExpression makeDotExpression(String chain) {
        String[] symbols = chain.split("\\.");
        JCExpression node = maker.Ident(names.fromString(symbols[0]));
        for (int i = 1; i < symbols.length; i++) {
            com.sun.tools.javac.util.Name nextName = names.fromString(symbols[i]);
            node = maker.Select(node, nextName);
        }
        return node;
    }
}
