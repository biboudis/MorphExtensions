package visitors;

import javax.annotation.processing.ProcessingEnvironment;

import annotations.Morph;
import checkers.types.AnnotatedTypeFactory;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Scope;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.comp.MemberEnter;
import com.sun.tools.javac.comp.Resolve;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import static com.sun.tools.javac.code.Kinds.*;

public class ExpansionTranslator extends TreeTranslator {

	protected static final String SYN_PREFIX = "__";
	
	protected Context context;
	private Symtab syms;
	protected TreeMaker make;
	protected Names names;
	protected Enter enter;
    private Resolve rs;
	protected MemberEnter memberEnter;
	protected TreePath path;
	protected Attr attr;
	protected Log log;
	
	protected AnnotatedTypeFactory atypeFactory;
	
	public ExpansionTranslator(
			ProcessingEnvironment processingEnv,
			TreePath path) {
		
        context = ((JavacProcessingEnvironment) processingEnv).getContext();
        syms = Symtab.instance(context);
        make = TreeMaker.instance(context);
        names = Names.instance(context);
        enter = Enter.instance(context);
        rs = Resolve.instance(context);
        memberEnter = MemberEnter.instance(context);
        attr = Attr.instance(context);	
        log = Log.instance(context);
	}
	
	@Override
	public void visitBlock(JCBlock tree) {
		System.out.println("# visitBlock: \n" + tree);
		List<JCStatement> stats;
        
		for (stats = tree.stats; stats.tail != null; stats = stats.tail) {
            JCStatement stat = stats.head;
            
            if (isMorphedVariableDeclaration(stat)) {
            	Env<AttrContext> env = enter.getEnv(stat.type.tsym);

            	System.out.println("# Found a morphed variable declaration: " + stat);
            	
            	System.out.println("# The environment of the block: " + env.info);
            	
            	JCVariableDecl syntheticStat = replaceWithSynthetic((JCVariableDecl) stat);
            	
            	System.out.println("# Transformed it in: " + syntheticStat);
            	
            	int oldErrors = log.nerrors;
                log.nerrors = 100; 
            	
                Type type = attr.attribStat(syntheticStat, env); 
            	
                log.nerrors = oldErrors;
            	
            	System.out.println("# Type is: " + type);
            	
				stats = stats.tail;
			}
        }
        
        System.out.println("# end");
		
        super.visitBlock(tree);
	}

	@Override
	public void visitVarDef(JCVariableDecl tree) {
		
		super.visitVarDef(tree);
		
		if (isMorphedVariableDeclaration(tree)) {
			
			replaceWithSynthetic(tree);
		}
		
	}

	private JCVariableDecl replaceWithSynthetic(JCVariableDecl tree) {
/*		if (tree.init.getTag() == Tag.NEWCLASS)
		{*/
			List<JCExpression> oldInitializerList = ((JCNewClass) tree.init).args;
			
			Name dummyName = names.fromString("__Logged$Stack");
			
			Type clazz = tree.sym.enclClass().members().lookup(dummyName).sym.type;
			
			JCNewClass newClassExpression = make.NewClass(null, null,  make.QualIdent(clazz.tsym), oldInitializerList, null);
			
			JCVariableDecl newVarDef = make.VarDef(tree.mods, tree.name, make.QualIdent(clazz.tsym), newClassExpression);
			
			System.out.println("# old var decl: " + tree);
			System.out.println("# new var decl: " + newVarDef);
			
/*				VarSymbol varSymbol = tree.sym; //l_stack
			
			// Env<AttrContext> localEnv = enter.getEnv(tree.type.tsym);
			Env<AttrContext> env = enter.getEnv(tree.type.tsym); // Must be the environment of Hello.
			
			//memberEnter.visitVarDef(newVarDef);
			
			// I need the method environment. How to find it?
			if(tree.sym.owner.kind == MTH) {
				System.out.println("The owner is method: " + env.enclMethod);
				// env = memberEnter.getMethodEnv(env.enclMethod, env);
			}
			
			System.out.println("Kind of the owner symbol: " + tree.sym.owner.getKind());
			System.out.println("tree.type.tsym: " + tree.type.tsym); // Hello.Logged
			System.out.println("tree.sym.type: " + tree.sym); //l_stack
			System.out.println("tree.sym: " + tree.sym); //l_Stack
			
			System.out.println(env.info);
			System.out.println(env.enclMethod);
			
			// Env<AttrContext> newEnv = localEnv.info.dup(tree);
		
			// attr.attribStat(newVarDef, newEnv);
*/				
			return newVarDef;
	}
	
	private boolean isMorphedVariableDeclaration(JCTree tree){
		return tree.getKind() == Kind.VARIABLE && ((JCVariableDecl) tree).getType().type.tsym.getAnnotation(Morph.class) != null;
	}
		
	private JCExpression stringToExpression(String chain) {
        String[] symbols = chain.split("\\.");
        JCExpression node = make.Ident(names.fromString(symbols[0]));
        for (int i = 1; i < symbols.length; i++) {
            com.sun.tools.javac.util.Name nextName = names.fromString(symbols[i]);
            node = make.Select(node, nextName);
        }
        return node;
    }
}
