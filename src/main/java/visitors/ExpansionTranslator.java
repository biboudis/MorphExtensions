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
import com.sun.tools.javac.code.Symbol;
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
import com.sun.tools.javac.tree.JCTree.JCStatement;
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
            	System.out.println("# Found a morphed variable declaration: " + stat);

            	JCVariableDecl varDecl = (JCVariableDecl) stat;
            	
            	printSymbolInfo(varDecl.sym);
            	
            	Env<AttrContext> env = enter.getEnv(varDecl.type.tsym);
            	
            	System.out.println(env);
            	
            	// Env<AttrContext> localEnv = memberEnter.getMethodEnv(varDecl.sym.owner, env);
           	
            	System.out.println("# old var decl: " + stat);
            	
            	JCVariableDecl syntheticStat = replaceWithSynthetic((JCVariableDecl) stat);

            	spliceNode(stats, stat, syntheticStat);
            	
            	System.out.println(stats.toString());
            	
            	int oldErrors = log.nerrors;
                log.nerrors = 100; 
            	
                attr.attribExpr(syntheticStat, env);                
                
                log.nerrors = oldErrors;
                
            	printSymbolInfo(syntheticStat.sym);
				
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
	
	private void spliceNode (List<JCStatement> statementList, JCStatement oldNode, JCStatement newNode){
        statementList.intersect(List.<JCStatement>of(newNode));
    }
	
	private void printSymbolInfo(Symbol sym){
    	System.out.println("# Symbol: " + sym);
    	
    	if (sym != null) {
    		System.out.println("\tKind: " + sym.getKind());
			System.out.println("\tType: " + sym.type);
			System.out.println("\tMembers: " + sym.members());
			System.out.println("\tOwner: " + sym.owner);
			System.out.println("\tOwner Kind: " + sym.owner.getKind());
			System.out.println("\tLocation: " + sym.location());
			System.out.println("\tMembers " + sym.members());
			System.out.println("\tMembers of Owner: " + sym.owner.members());
		}
	}

	private JCVariableDecl replaceWithSynthetic(JCVariableDecl tree) {

		List<JCExpression> oldInitializerList = ((JCNewClass) tree.init).args;

		Name dummyName = names.fromString("__Logged$Stack");

		Type clazz = tree.sym.enclClass().members().lookup(dummyName).sym.type;

		JCNewClass newClassExpression = make.NewClass(null, null,
				make.QualIdent(clazz.tsym), oldInitializerList, null);

		JCVariableDecl newVarDef = make.VarDef(tree.mods, tree.name,
				make.QualIdent(clazz.tsym), newClassExpression);

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
