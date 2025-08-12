package coffeeshout.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiSchema {
    Class<?>[] enums() default {};
    Class<?> responseType() default Object.class;
    Class<?> requestType() default Object.class;
    String description() default "";
}
