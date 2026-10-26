package org.fusesource.hawtjni.runtime;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JniMethod {
   String cast() default "";

   String accessor() default "";

   MethodFlag[] flags() default {};

   String copy() default "";

   String conditional() default "";

   JniArg[] callbackArgs() default {};
}
