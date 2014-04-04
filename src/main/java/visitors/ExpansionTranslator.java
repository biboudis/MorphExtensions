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
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Check;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.comp.MemberEnter;
import com.sun.tools.javac.jvm.ClassReader;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
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
import com.sun.tools.javac.tree.TreeCopier;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeScanner;
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
    protected Types types;
    protected ClassReader reader;

    public ExpansionTranslator(Context context) {
        make = TreeMaker.instance(context);
        names = Names.instance(context);
        enter = Enter.instance(context);
        memberEnter = MemberEnter.instance(context);
        attr = Attr.instance(context);
        log = Log.instance(context);
        syms = Symtab.instance(context);
        chk = Check.instance(context);
        types = Types.instance(context);
        reader = ClassReader.instance(context);
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
        Object[] args = {member, env};
        try {
            meth.invoke(memberEnter, args);
        } catch (IllegalAccessException e) {
            System.out.println("raised only if compiler internal api changes");
        } catch (InvocationTargetException e) {
            System.out.println("raised only if compiler internal api changes");
        }
    }

    private JCVariableDecl makeExpandedVarDeclaration(JCVariableDecl tree) {

        TypeSymbol symbolOfMorphClass = tree.getType().type.tsym;

        // check if has already been rewritten
        if (replaced.contains(tree.name)) {
            return null;
        }

        // Lookup synthetic class: e.g. Logged$Stack
        Name expandedClassName = names.fromString("Logged$Stack");
        // Fully qualified path: e.g Hello.Logged$Stack
        JCExpression newType = make.Select(
                make.Ident(tree.sym.enclClass().name), expandedClassName);

        // Testing synthetic creation
        JCClassDecl morphedClass = makeMorphedClass(symbolOfMorphClass.enclClass(), symbolOfMorphClass, tree.type, enter.getClassEnv(symbolOfMorphClass));

        JCExpression morphedClassIdent = make.Select(make.Ident(morphedClass.name), morphedClass.sym.enclClass().name);

        Debug.printTreeInfo(morphedClass);
        Debug.printTreeInfo(morphedClassIdent);

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

    /**
     * @Morph public static class Logged<T> { T instance; public Logged(T t) {
     * this.instance = t; }
     * @for("m", "public R ()") public R<R>m() { System.out.println("Log
     * first"); return instance.m(); } //specializes to-> public static class
     * Logged$Stack { Stack instance; public Logged$Stack(Stack t) {
     * this.instance = t; } (...reflective methods...) }
     */
    private JCClassDecl makeMorphedClass(ClassSymbol owner, TypeSymbol morphedClass, final Type instantiatedType, final Env<AttrContext> env) {

        JCClassDecl morphClassDefTree = enter.getClassEnv(morphedClass).enclClass;
        final JCClassDecl specializedClassDefTree = new TreeCopier<JCClassDecl>(make).copy(morphClassDefTree);

        // Needs refactoring to delegate the class symbol creation to the enter phase (see visitClassDef) 
        ClassSymbol c = reader.defineClass(names.empty, morphedClass.owner);

        c.flatname = names.fromString("Logged$Stack");
        c.name = c.flatname;
        c.sourcefile = owner.sourcefile;
        c.completer = null;
        c.members_field = new Scope(c);
        c.flags_field = Flags.SYNTHETIC | Flags.STATIC;

        ClassType ctype = (ClassType) c.type;
        ctype.supertype_field = syms.objectType;
        ctype.interfaces_field = List.nil();

        enterSynthetic(c, owner.members());
        chk.compiled.put(c.flatname, c);

        specializedClassDefTree.sym = c;
        specializedClassDefTree.type = c.type;
        ///////////////////////////////

        attr.attribExpr(specializedClassDefTree, env);

        final Map<String, Type> map = buildSubstitutionMap(instantiatedType.tsym.enclClass(), instantiatedType);

        JCTree.Visitor specializer = new TreeTranslator() {

            @Override
            public void visitClassDef(JCClassDecl tree) {

                super.visitClassDef(tree);

                StringBuilder sb = new StringBuilder();
                sb.append(tree.name);
                List<Type> typeParameters = instantiatedType.getTypeArguments();

                for (Type tp : typeParameters) {
                    sb.append("$");
                    sb.append(tp.tsym.name);
                }

                System.out.println("# Synthetic string for class name " + sb.toString());

                tree.name = names.fromString(sb.toString());
                tree.typarams = List.nil();
            }

            @Override
            public void visitVarDef(JCVariableDecl tree) {
                super.visitVarDef(tree);
                if (map.containsKey(tree.vartype.toString())) {
                    tree.vartype = make.at(tree.pos).Ident(map.get(tree.vartype.toString()).tsym);
                }
            }

            @Override
            public void visitIdent(JCIdent tree) {
                super.visitIdent(tree);
            }

        };

        specializedClassDefTree.accept(specializer);

        return specializedClassDefTree;
    }

    /// type of formal type variable -> type of actual type argument
    private Map<String, Type> buildSubstitutionMap(ClassSymbol c, Type instantiatedType) {

        Map<String, Type> map = new HashMap<String, Type>();

        List<Type> formals = c.type.allparams();
        List<Type> actuals = instantiatedType.getTypeArguments();
        while (!actuals.isEmpty() && !formals.isEmpty()) {
            Type actual = actuals.head;
            Type formal = formals.head;

            map.put(formal.toString(), actual);

            System.out.println("# Adding to substitution map: " + formal + "->" + actual);

            actuals = actuals.tail;
            formals = formals.tail;
        }

        return map;
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
                .<JCTree.JCStatement>of(newNode);
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
