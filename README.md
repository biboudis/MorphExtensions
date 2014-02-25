Morph Extensions for Java
=========================

This library introduces class-morphing to Java via (JSR 308) type annotations.

The first step is to translate this var declaration:

``` Java
Hello.Logged<Hello.Stack> l_stack = new Hello.Logged<Hello.Stack>(new Hello.Stack());
```

into this:

``` Java
Hello.__Logged$Stack l_stack = new Hello.__Logged$Stack(new Hello.Stack())
```

The rewriting process will come after entering a synthetic class into the symbol table but for the time being this class is included manually.

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

