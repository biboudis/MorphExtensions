package utils;

import com.sun.tools.javac.code.Scope;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.tree.JCTree;

public class Debug {

	public static void printTreeInfo(JCTree tree) {
		if (tree != null) {
			System.out.println("# Tree: " + tree);
			System.out.println("\tKind: " + tree.getKind());
			System.out.println("\tTag: " + tree.getTag());
		} else {
			System.out.println("# Tree is null.");
		}
	}

	public static void printSymbolInfo(Symbol sym) {
		if (sym != null) {
			System.out.println("# Symbol: " + sym);
			System.out.println("\tKind: " + sym.getKind());
			System.out.println("\tType: " + sym.type);
			System.out.println("\tBase Symbol: " + sym.baseSymbol());
			System.out.println("\tOutermost class: " + sym.outermostClass());
			System.out.println("\tEnclosing element: "
					+ sym.getEnclosingElement());
			System.out.println("\tLocation: " + sym.location());
			System.out.println("\tMembers: " + sym.members());

			System.out.println("\tOwner: " + sym.owner);
			System.out.println("\t\tKind: " + sym.owner.getKind());
			System.out.println("\t\tMembers: " + sym.owner.members());
		} else {
			System.out.println("# Symbol is null.");
		}
	}

	public static void printEnvInfo(Env<?> env) {
		if (env != null) {
			System.out.println("# Env: " + env);
			System.out.println("#\tEnclosing method: " + env.enclMethod);
		} else {
			System.out.println("# Env is null.");
		}
	}

	public static void printScopeInfo(Scope s) {
		if (s != null) {
			System.out.println("# Scope: " + s);
			System.out.println("#\tElements: " + s.elems);
		} else {
			System.out.println("# Scope is null.");
		}
	}
}
