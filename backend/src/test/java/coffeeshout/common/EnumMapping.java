package coffeeshout.common;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumMapping {
    String field();
    Class<? extends Enum<?>> enumClass();
}
