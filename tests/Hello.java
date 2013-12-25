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
	
	@Morph
	public class Logged<T> {
		
		T instance;
		
		public Logged(T t) { this.instance = t; }
		
/*		@for("m", "public R ()") 
		public R <R>m()
		{
			return instance.m();
		}*/
	}
	
	public static void main(String[] args) {
		
		System.out.println("# Stack Test");	
		Stack stack = new Stack();
		
		stack.push(3);
		System.out.println("Size: " + stack.size());
		stack.pop();
		
/*		System.out.println("# Logged Stack Test");
		Logged<Stack> l_stack = new Logged<Stack>();

		l_stack.push(3);
		System.out.println("Size: " + l_stack.size());
		l_stack.pop();*/
	}
}