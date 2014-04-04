
interface T_bound <R> {
    R m();
    R m1(Logged_Translated.Customer p1);
}

public class Logged_Translated<T extends T_bound<R>, R, A> {

    T instance;

    static class Customer {
	void order() {}
    }

    //Customer instance2;
    CustomerReflectiveMethods<R, A> instance2;
    abstract class CustomerReflectiveMethods<R, A> extends Customer {
	abstract R m1();
	abstract R m2(A a);
    }

    public Logged_Translated(T t) { this.instance = t; }
    
    /*@For("m", "public R m ()" : T.methods)*/
    public R refl1_m()
    {
	System.out.println("Log first");
	return instance.m();
    }

    /*@For("m1", "public R m1 (Customer) : T.methods")*/
    public R refl2_delegate_m1(Customer x)
    {
	System.out.println("Log first");
	return instance.m1(x);
    }

    /*@For("m1", "public R m1 ()" : Customer.methods)*/
    public R refl3_m(Customer a)
    {
    	System.out.println("Log first");
    	return instance2.m1();
    }

    /*@For("m2, A", "public R m2 (A)" : Customer.methods)*/
    public R refl3_m(A a)
    {
    	System.out.println("Log first");
    	return instance2.m2(a);
    }

    /*@For("m1, A", "public R m1 (A)" : Customer.methods)*/
    public R refl4_m(A a)
    {
    	System.out.println("Order first");
	instance2.order();
    	return instance2.m2(a);
    }
}

