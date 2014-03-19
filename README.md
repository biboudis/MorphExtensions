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

* (Done) Translate a simple var declaration, with ```__Logged$Stack``` being entered via source:

``` Java
Hello.Logged<Hello.Stack> l_stack = new Hello.Logged<Hello.Stack>(new Hello.Stack());
```

into this:

``` Java
Hello.__Logged$Stack l_stack = new Hello.__Logged$Stack(new Hello.Stack())
```

* Enter the synthetic class ```__Logged$Stack``` programmatically as a top level
  class in the same package.


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
