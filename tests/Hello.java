import java.util.LinkedList;
import annotations.*;

public class Hello {
	
	public class Stack {
	    private LinkedList<Object> list = new LinkedList<Object>();
	    public void push(Object item) {list.addFirst(item);}
	    public Object pop() {return list.removeFirst();}
	    public Object peek() {return list.getFirst();}
	    public int size() {return list.size();}
	    public boolean isEmpty() {return list.isEmpty();}
	}
	
	// This will be declared as a @Morph class.
	@Morph
	public class Logged<T> {
		
		T instance;
		
		public Logged(T t) { this.instance = t; }
		
        /*@for("m", "public R ()") 
		public R <R>m()
		{
			System.out.println("Log first");
			return instance.m();
		}*/
	}
	
	// Will be this effectively at compile time.
	// 1. Fresh name will be generated for top level class definition.
	// 2. Generic type is substituted with concrete type.
	// 3. Expanded static for method declarations.
	
	public class __Logged$Stack {
		
		Stack instance;
		
		public __Logged$Stack(Stack t) { this.instance = t; }
		
	    public Object pop() {
	    	System.out.println("Log first");
	    	return instance.pop();
	    }
	    
	    public Object peek() {
	    	System.out.println("Log first");
	    	return instance.peek();
	    }
	}
	
	public static void main(String[] args) {
		Hello h = new Hello();
		
		System.out.println("# Stack Test");	
		Stack stack = h.new Stack();
		
		stack.push(3);
		System.out.println("Size: " + stack.size());
		stack.pop();
		
		System.out.println("# Logged Stack Test");
		Logged<Stack> l_stack = h.new Logged<Stack>(h.new Stack());

		/*		l_stack.push(3);
		System.out.println("Size: " + l_stack.size());
		l_stack.pop();*/
	}
}