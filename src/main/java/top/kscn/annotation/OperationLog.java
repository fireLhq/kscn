package top.kscn.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {
    String eventType();

    String action();

    String resourceType() default "";

    String resourceIdParam() default "";

    String resourceNameParam() default "";
}