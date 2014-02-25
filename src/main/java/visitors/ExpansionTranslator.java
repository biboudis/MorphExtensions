package visitors;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.annotation.processing.ProcessingEnvironment;

import annotations.Morph;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.code.Scope;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.comp.MemberEnter;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

public class ExpansionTranslator extends TreeTranslator {

	protected static final String SYN_PREFIX = "__";

	protected Context context;
	protected ProcessingEnvironment processingEnv;

	protected TreeMaker make;
	protected Names names;
	protected Enter enter;
	protected MemberEnter memberEnter;
	protected TreePath path;
	protected Attr attr;
	protected Log log;

	public ExpansionTranslator(Context context) {
		// context = ((BasicJavacTask) task).getContext();
		make = TreeMaker.instance(context);
		names = Names.instance(context);
		enter = Enter.instance(context);
		memberEnter = MemberEnter.instance(context);
		attr = Attr.instance(context);
		log = Log.instance(context);
	}

	Env<AttrContext> env;

	@Override
	public void visitTopLevel(JCCompilationUnit tree) {
		super.visitTopLevel(tree);
	}

	@Override
	public void visitMethodDef(JCMethodDecl tree) {
		env = memberEnter.getMethodEnv(tree,
				enter.getClassEnv(tree.sym.enclClass()));

		super.visitMethodDef(tree);
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

				printEnvInfo(env);

				makeExpandedVarDeclaration(varDecl);

				enterMember(varDecl, env);

				attribute(varDecl);

				printScopeInfo(varDecl.sym.members());

				printSymbolInfo(varDecl.sym);
			}
		}

		System.out.println("# end: \n" + tree);

		result = tree;

		super.visitBlock(tree);
	}

	@Override
	public void visitVarDef(JCVariableDecl tree) {

		super.visitVarDef(tree);

		if (isMorphedVariableDeclaration(tree)) {
			makeExpandedVarDeclaration(tree);
		}
	}

	// Inspired by EnerJ
	public void enterMember(JCTree member, Env<AttrContext> env) {
		Method meth = null;
		try {
			meth = MemberEnter.class.getDeclaredMethod("memberEnter",
					JCTree.class, Env.class);
		} catch (NoSuchMethodException e) {
			System.out.println("raised only if compiler internal api changes");
		}
		meth.setAccessible(true);
		Object[] args = { member, env };
		try {
			meth.invoke(memberEnter, args);
		} catch (IllegalAccessException e) {
			System.out.println("raised only if compiler internal api changes");
		} catch (InvocationTargetException e) {
			System.out.println("raised only if compiler internal api changes");
		}
	}

	private void attribute(JCTree tree) {
		attr.attribStat(tree, env);
	}

	@SuppressWarnings("unused")
	private void spliceNode(List<JCStatement> statementList,
			JCStatement oldNode, JCStatement newNode) {
		List<JCTree.JCStatement> newList = List
				.<JCTree.JCStatement> of(newNode);
		newList.tail = statementList.tail;
		statementList.tail = newList;
	}
	
	private void printTreeInfo(JCTree tree){
		if (tree != null) {
			System.out.println("# Tree: " + tree);
			System.out.println("\tKind: " + tree.getKind());
			System.out.println("\tTag: " + tree.getTag());
		} else {
			System.out.println("# Tree is null.");
		}
	}

	private void printSymbolInfo(Symbol sym) {
		if (sym != null) {
			System.out.println("# Symbol: " + sym);
			System.out.println("\tKind: " + sym.getKind());
			System.out.println("\tType: " + sym.type);
			System.out.println("\tBase Symbol: " + sym.baseSymbol());
			System.out.println("\tOutermost class: " + sym.outermostClass());
			System.out.println("\tEnclosing element: " + sym.getEnclosingElement());
			System.out.println("\tLocation: " + sym.location());
			System.out.println("\tMembers: " + sym.members());

			System.out.println("\tOwner: " + sym.owner);
			System.out.println("\t\tKind: " + sym.owner.getKind());
			System.out.println("\t\tMembers: " + sym.owner.members());
		} else {
			System.out.println("# Symbol is null.");
		}
	}

	private void printEnvInfo(Env<?> env) {
		if (env != null) {
			System.out.println("# Env: " + env);
			System.out.println("#\tEnclosing method: " + env.enclMethod);
		} else {
			System.out.println("# Env is null.");
		}
	}

	private void printScopeInfo(Scope s) {
		if (s != null) {
			System.out.println("# Scope: " + s);
			System.out.println("#\tElements: " + s.elems);
		} else {
			System.out.println("# Scope is null.");
		}
	}

	private void makeExpandedVarDeclaration(JCVariableDecl tree) {

		Name expandedClassName = names.fromString("__Logged$Stack");
		
		printTreeInfo(tree);
		printSymbolInfo(tree.sym);
		printScopeInfo(tree.sym.enclClass().members());
		
		Type expandedClassType = tree.sym.enclClass().members()
				.lookup(expandedClassName).sym.type;

		List<JCExpression> oldInitializerList = ((JCNewClass) tree.init).args;
		
		JCNewClass initExpression = make.NewClass(null, null, make.Select(
				make.Ident(tree.sym.enclClass().name), expandedClassName),
				oldInitializerList, null);

		tree.vartype = make.Select(make.Ident(tree.sym.enclClass().name), expandedClassName);
		tree.setType(expandedClassType);
		tree.init = initExpression;
		tree.sym.type = expandedClassType;
	} 

	private boolean isMorphedVariableDeclaration(JCTree tree) {
		if (tree.getKind() == Kind.VARIABLE) {
			JCVariableDecl varDecl = ((JCVariableDecl) tree);
			
			attribute(varDecl);
						
			return ((JCVariableDecl) tree).getType().type.tsym
					.getAnnotation(Morph.class) != null;
		}
		
		return false;
	}

	private JCExpression stringToExpression(String chain) {
		String[] symbols = chain.split("\\.");
		JCExpression node = make.Ident(names.fromString(symbols[0]));
		for (int i = 1; i < symbols.length; i++) {
			com.sun.tools.javac.util.Name nextName = names
					.fromString(symbols[i]);
			node = make.Select(node, nextName);
		}
		return node;
	}
}
