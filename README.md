Morph Extensions for Java
=========================

This library introduces class-morphing to Java via (JSR 308) type annotations.

Environment setup
-----------------
``` bash
CHECKERS=/home/bibou/Projects/types/checker-framework-1.7.4/
export PATH=${CHECKERS}/binary:${PATH}
```

Manual Testing
--------------
```cd ~/Projects/MorphExtensions;mvn package;./src/test/resources/javamc ./src/test/resources/Hello.java```


Goal
----
The first step is to translate this var declaration:

``` Java
Hello.Logged<Hello.Stack> l_stack = new Hello.Logged<Hello.Stack>(new Hello.Stack());
```

into this:

``` Java
Hello.__Logged$Stack l_stack = new Hello.__Logged$Stack(new Hello.Stack())
```

This rewriting phasem in the future will come after entering a synthetic class (like ```__Logged$Stack```) into the symbol table but for the time being this class is included manually.

``` Java
@Morph
public static class Logged<T> {
  T instance;
  public Logged(T t) { this.instance = t; }
}
```
	

``` Java
public static class __Logged$Stack {
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
```
