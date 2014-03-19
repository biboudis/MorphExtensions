Morph Extensions for Java
=========================

This library introduces class-morphing to Java via (JSR 308) type annotations.

(the following are personal notes)

Environment setup
-----------------
``` bash
CHECKERS=/home/bibou/Projects/types/checker-framework-1.7.4/
export PATH=${CHECKERS}/binary:${PATH}
```

Manual Testing
--------------
```cd ~/Projects/MorphExtensions;mvn package;./src/test/resources/javamc ./src/test/resources/Hello.java```


Milestones
----------

* (Done) Translate a simple var declaration, with ```Logged$Stack``` being entered via source:

``` Java
Hello.Logged<Hello.Stack> l_stack = new Hello.Logged<Hello.Stack>(new Hello.Stack());
```
into this:

``` Java
Hello.Logged$Stack l_stack = new Hello.Logged$Stack(new Hello.Stack())
```

* Enter the synthetic class ```Logged$Stack``` programmatically as a top level
  class in the same package.

The first step is to specialized class definitions using the morphed class as a
blueprint. This requires the tree of type JCClassDecl to be copied with fresh symbols.
The source-code equivalent of this is the following transformation.

```
@morph
public static class Logged<T> {
	T instance;
	public Logged(T t) { this.instance = t; }
}

public static class Logged$Integer {
	Integer instance;
	public Logged(Integer t) { this.instance = t; }
}

public static class Logged$Customer {
	Customer instance;
	public Logged(Customer t) { this.instance = t; }
}```

The next step is to expand a @for annotation to methods (CTR part) and produce
the method declaration for something that is equivalent in the source-code level
with:

``` Java
public static class Logged$Stack {
  Stack instance;
  
  public Logged$Stack(Stack t) { this.instance = t; }
  
  public Object pop() {
    System.out.println("Log first");
    return instance.pop();
  }
       
  public Object peek() {
    System.out.println("Log first");
    return instance.peek();
  }
}
```
