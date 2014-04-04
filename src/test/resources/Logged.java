public class Logged<T> {
		
    T instance;
		
    public Logged(T t) { this.instance = t; }
    
    /*@For("m", "public R m () in T")*/
    public R m()
    {
	System.out.println("Log first");
	return instance.m();
    }
}
