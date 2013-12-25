package annotations;

import java.lang.annotation.*;

import checkers.quals.*;

/**
 * Type annotation to indicate a meta-class that includes static for patterns.
 * This class will get expanded for each different use-site and also will be
 * modularly type checked.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@TypeQualifier
public @interface Morph {

}
