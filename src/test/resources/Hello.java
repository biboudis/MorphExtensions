import java.util.LinkedList;
import annotations.*;

public class Hello {
	
	public static class Stack {
	    private LinkedList<Object> list = new LinkedList<Object>();
	    public void push(Object item) {list.addFirst(item);}
	    public Object pop() {return list.removeFirst();}
	    public Object peek() {return list.getFirst();}
	    public int size() {return list.size();}
	    public boolean isEmpty() {return list.isEmpty();}
	}
	
	// This will be declared as a @Morph class.
	@Morph
	public static class Logged<T> {
		
            T instance;

            public Logged(T t) { this.instance = t; }
		 
//            @For("m", "public R ()") 
//            @For()
//            public void m()
//            {
//                    System.out.println("Log first");
//                    instance.m();
//            }
	}
	
	// Will be this effectively at compile time.
	// 1. Fresh name will be generated for top level class definition.
	// 2. Generic type is substituted with concrete type.
	// 3. Expanded static for method declarations.
	public static class Logged$Stack {
		
		Stack instance;
		
		public Logged$Stack(Stack t) { this.instance = t; }
		
	    public Object pop() {
	    	System.out.println("# Log: pop");
	    	return instance.pop();
	    }
	    
	    public Object peek() {
	    	System.out.println("# Log: peek");
	    	return instance.peek();
	    }
	    
	    public void push(Object item) {
	    	System.out.println("# Log: push");
	    	instance.push(item);
	    }
	}
	
	public static void main(String[] args) {
		
		System.out.println("# Normal Stack Test");
		Hello.Stack stack = new Hello.Stack();
		stack.push("Added a string to normal.");
		stack.peek();
		
		System.out.println("# Logged Stack Test");
		Hello.Logged<Hello.Stack> l_stack = new Hello.Logged<Hello.Stack>(new Hello.Stack());
		l_stack.push("Added a string to morphed.");
		l_stack.peek();
	}
}