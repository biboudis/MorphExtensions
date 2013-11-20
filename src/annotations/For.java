package annotations;

import java.lang.annotation.*;

import checkers.quals.*;

/**
 * Type qualifier for approximate values.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@TypeQualifier
public @interface For {
	
}
