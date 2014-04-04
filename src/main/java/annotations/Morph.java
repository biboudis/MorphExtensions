package annotations;

import java.lang.annotation.*;


/**
 * Type annotation to indicate a meta-class that includes static for patterns.
 * This class will get expanded for each different use-site and also will be
 * modularly type checked.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface Morph {

}
