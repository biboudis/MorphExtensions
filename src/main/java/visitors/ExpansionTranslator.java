package visitors;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;

import utils.Debug;
import annotations.Morph;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Scope;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Check;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.comp.MemberEnter;
import com.sun.tools.javac.jvm.ClassReader;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

public class ExpansionTranslator extends TreeTranslator {

	private static final String SYN_TYPE_PREFIX = "__";
	private static final String SYN_VAR_NAME_SUFFIX = "$SYN";

	protected Set<Name> replaced = new HashSet<Name>();

	protected Context context;
	protected ProcessingEnvironment processingEnv;

	protected TreeMaker make;
	protected Names names;
	protected Enter enter;
	protected MemberEnter memberEnter;
	protected TreePath path;
	protected Attr attr;
	protected Log log;
	protected Symtab syms;
	protected Check chk;

	public ExpansionTranslator(Context context) {
		make = TreeMaker.instance(context);
		names = Names.instance(context);
		enter = Enter.instance(context);
		memberEnter = MemberEnter.instance(context);
		attr = Attr.instance(context);
		log = Log.instance(context);
		syms = Symtab.instance(context);
		chk = Check.instance(context);
	}

	Env<AttrContext> methodEnv;

	@Override
	public void visitTopLevel(JCCompilationUnit tree) {
		super.visitTopLevel(tree);
	}

	@Override
	public void visitMethodDef(JCMethodDecl tree) {
		// Tracking the current method environment to use it in attribution.
		methodEnv = memberEnter.getMethodEnv(tree,
				enter.getClassEnv(tree.sym.enclClass()));

		super.visitMethodDef(tree);
	}

	@Override
	public void visitBlock(JCBlock tree) {
		List<JCStatement> stats;

		for (stats = tree.stats; stats.tail != null; stats = stats.tail) {
			JCStatement stat = stats.head;

			// This step is needed to be able to use symbols and type symbols
			// below.
			attr.attribStat(stat, methodEnv);

			if (isMorphedVariableDeclaration(stat)) {
				JCVariableDecl varDecl = (JCVariableDecl) stat;

				System.out.println("# Found a morphed variable declaration: "
						+ varDecl);

				Debug.printEnvInfo(methodEnv);
				Debug.printTreeInfo(varDecl);
				Debug.printSymbolInfo(varDecl.sym);
				Debug.printScopeInfo(varDecl.sym.enclClass().members());

				JCVariableDecl ret = makeExpandedVarDeclaration(varDecl);

				System.out.println("# translated to: \n" + ret);

				if (ret != null) {
					spliceNode(stats, stat, ret);

					attr.attribStat(tree, methodEnv);
				}

				Debug.printScopeInfo(varDecl.sym.enclClass().members());
			}
		}

		result = tree;

		super.visitBlock(tree);
	}

	@Override
	public void visitApply(JCMethodInvocation tree) {
		super.visitApply(tree);

		System.out.println(tree);
	}

	@Override
	public void visitIdent(JCIdent tree) {
		super.visitIdent(tree);

		// Translate a local variable access: e.g. l_stack to l_stack$SYN
		// if the variable access comes from a variable declaration that has
		// been rewritten.
		if (replaced.contains(tree.name)) {
			System.out.println("Found one!" + tree.name);
			String newName = tree.name.toString() + SYN_VAR_NAME_SUFFIX;

			JCTree.JCExpression expr = make.Ident(names.fromString(newName));
			System.out.println("to: " + expr);
			result = expr;
		}
	}

	@Override
	public void visitVarDef(JCVariableDecl tree) {

		super.visitVarDef(tree);

		if (isMorphedVariableDeclaration(tree)) {
			makeExpandedVarDeclaration(tree);
		}
	}

	// Inspired by EnerJ
	private void enterMember(JCTree member, Env<AttrContext> env) {
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

	private JCVariableDecl makeExpandedVarDeclaration(JCVariableDecl tree) {

		// check if has already been rewritten
		if (replaced.contains(tree.name))
			return null;

		// Lookup synthetic class: e.g. __Logged$Stack
		Name expandedClassName = names.fromString("__Logged$Stack");

		// Fully qualified path: e.g Hello.__Logged$Stack
		JCExpression newType = make.Select(
				make.Ident(tree.sym.enclClass().name), expandedClassName);

		List<JCExpression> oldInitializerList = ((JCNewClass) tree.init).args;

		JCNewClass initExpression = make.NewClass(null, null, newType,
				oldInitializerList, null);

		JCTree.JCVariableDecl decl = make.VarDef(tree.mods,
				names.fromString(tree.name + SYN_VAR_NAME_SUFFIX), newType,
				initExpression);

		// keep track of the produced node (with the original name)
		replaced.add(tree.name);

		return decl;
	}
	
	/*  This method creates an expansion of a morphed class.
	 
		public static class __Logged$Stack {
		
			Stack instance;
		
			public __Logged$Stack(Stack t) { this.instance = t; }
		
	    	(...reflective methods...)
		} 
	*/

	private JCClassDecl makeMorphedClass(ClassSymbol owner, List<Type> typeArguments) {

		ClassSymbol c = syms.defineClass(names.empty, owner);
		c.flatname = names.fromString("__Logged$Stack");
		c.sourcefile = owner.sourcefile;
		c.completer = null;
		c.members_field = new Scope(c);
		c.flags_field = Flags.SYNTHETIC | Flags.STATIC;
		
		ClassType ctype = (ClassType) c.type;
		ctype.supertype_field = syms.objectType;
		ctype.interfaces_field = List.nil();

        enterSynthetic(c, owner.members());
        chk.compiled.put(c.flatname, c);
        
		JCClassDecl cdef = make.ClassDef(make.Modifiers(owner.flags()),
				names.empty, List.<JCTypeParameter> nil(), null,
				List.<JCExpression> nil(), List.<JCTree> nil());

		cdef.sym = c;
		cdef.type = c.type;

		return cdef;
	}

	private void enterSynthetic(ClassSymbol c, Scope members) {
		members.enter(c);
	}

	private boolean isMorphedVariableDeclaration(JCTree tree) {
		if (tree.getKind() == Kind.VARIABLE) {
			JCVariableDecl varDecl = ((JCVariableDecl) tree);

			return varDecl.getType().type.tsym.getAnnotation(Morph.class) != null;
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
