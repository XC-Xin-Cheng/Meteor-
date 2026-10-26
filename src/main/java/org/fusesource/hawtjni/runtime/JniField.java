package org.fusesource.hawtjni.runtime;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JniField {
   String cast() default "";

   String accessor() default "";

   String getter() default "";

   String setter() default "";

   String conditional() default "";

   FieldFlag[] flags() default {};
}
