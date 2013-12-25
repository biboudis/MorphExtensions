package annotations;

import java.lang.annotation.*;

import checkers.quals.*;

/**
 * Type annotation to indicate static-for method declarations.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@TypeQualifier
public @interface For {
	
}
