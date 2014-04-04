package annotations;

import java.lang.annotation.*;


/**
 * Type annotation to indicate static-for method declarations.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface For {
	
}
