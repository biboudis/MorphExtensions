package visitors;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.annotation.processing.ProcessingEnvironment;

import utils.Debug;

import annotations.Morph;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
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
		// Tracking the current method environment to use it in attribution.
		env = memberEnter.getMethodEnv(tree,
				enter.getClassEnv(tree.sym.enclClass()));

		super.visitMethodDef(tree);
	}

	@Override
	public void visitBlock(JCBlock tree) {
		List<JCStatement> stats;

		for (stats = tree.stats; stats.tail != null; stats = stats.tail) {
			JCStatement stat = stats.head;
			
			// This step is needed to be able to use symbols and type symbols below.
			attribute(stat);
			
			if (isMorphedVariableDeclaration(stat)) {
				JCVariableDecl varDecl = (JCVariableDecl) stat;

				System.out.println("# Found a morphed variable declaration: "
						+ varDecl);
				System.out.println("# in: \n" + tree);

				Debug.printEnvInfo(env);
				Debug.printTreeInfo(varDecl);
				Debug.printSymbolInfo(varDecl.sym);
				Debug.printScopeInfo(varDecl.sym.enclClass().members());

				makeExpandedVarDeclaration(varDecl);

				// attribute(varDecl);
				// enterMember(varDecl, env);
				
				System.out.println("# translated to: \n" + tree);
				Debug.printEnvInfo(env);
				Debug.printTreeInfo(varDecl);
				Debug.printSymbolInfo(varDecl.sym);
				Debug.printScopeInfo(varDecl.sym.enclClass().members());
			}
		}

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

	private void makeExpandedVarDeclaration(JCVariableDecl tree) {

		// Lookup synthetic class and wire it as a type to the previous
		// JCVariableDecl.
		Name expandedClassName = names.fromString("__Logged$Stack");
		Type expandedClassType = tree.sym.enclClass().members()
				.lookup(expandedClassName).sym.type;

		List<JCExpression> oldInitializerList = ((JCNewClass) tree.init).args;

		JCNewClass initExpression = make.NewClass(null, null, make.Select(
				make.Ident(tree.sym.enclClass().name), expandedClassName),
				oldInitializerList, null);

		tree.vartype = make.Select(make.Ident(tree.sym.enclClass().name),
				expandedClassName);
		tree.setType(expandedClassType);
		tree.init = initExpression;
		tree.sym.type = expandedClassType;
	}

	private boolean isMorphedVariableDeclaration(JCTree tree) {
		if (tree.getKind() == Kind.VARIABLE) {
			JCVariableDecl varDecl = ((JCVariableDecl) tree);
			
			return varDecl.getType().type.tsym
					.getAnnotation(Morph.class) != null;
		}

		return false;
	}

	private void spliceNode(List<JCStatement> statementList,
			JCStatement oldNode, JCStatement newNode) {
		List<JCTree.JCStatement> newList = List
				.<JCTree.JCStatement> of(newNode);
		newList.tail = statementList.tail;
		statementList.tail = newList;
	}

	private JCExpression stringToExpression(String chain) {
		String[] symbols = chain.split("\\.");
		JCExpression node = make.Ident(names.fromString(symbols[0]));
		for (int i = 1; i < symbols.length; i++) {
			Name nextName = names.fromString(symbols[i]);
			node = make.Select(node, nextName);
		}
		return node;
	}
}
