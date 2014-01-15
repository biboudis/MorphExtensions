package visitors;

import javax.annotation.processing.ProcessingEnvironment;

import annotations.Morph;
import checkers.types.AnnotatedTypeFactory;

import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Scope;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.MemberEnter;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

public class ExpansionTranslator extends TreeTranslator {

	protected static final String SYN_PREFIX = "__";
	
	protected Context context;
	//private Symtab syms;
	protected TreeMaker maker;
	protected Names names;
	protected Enter enter;
	protected MemberEnter memberEnter;
	protected TreePath path;
	protected Attr attr;
	protected AnnotatedTypeFactory atypeFactory;
	
	public ExpansionTranslator(
			ProcessingEnvironment processingEnv,
			TreePath path) {
		
        context = ((JavacProcessingEnvironment) processingEnv).getContext();
        //syms = Symtab.instance(context);
        maker = TreeMaker.instance(context);
        names = Names.instance(context);
        enter = Enter.instance(context);
        memberEnter = MemberEnter.instance(context);
        attr = Attr.instance(context);	
	}
	
	@Override
	public void visitVarDef(JCVariableDecl tree) {

		super.visitVarDef(tree);
		
		if (tree.getType().type.tsym.getAnnotation(Morph.class) != null) {
			System.out.println("# Tree stats: \n");
			System.out.println(tree);
			System.out.println(tree.getInitializer());
	
			//Name dummySyntheticClass = names.fromString("__Logged$Stack");
			
			System.out.println(new Scope(tree.sym).lookup(tree.name).isStaticallyImported());
//			
//			System.out.println("Is statically imported? " + found.isStaticallyImported());
//			
//			JCExpression ident = maker.Ident(dummySyntheticClass).setType(syms.objectType);
//			
//			//if (tree.init instanceof JCNewClass)
//			
//			JCNewClass newInit = maker.NewClass(null, null, ident, List.<JCExpression> nil(), null);
//			
//			JCVariableDecl newVarDef = maker.VarDef(tree.mods, tree.name, ident, newInit);
//            
//			System.out.println("# New: \n" + newVarDef);
			
		}
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
