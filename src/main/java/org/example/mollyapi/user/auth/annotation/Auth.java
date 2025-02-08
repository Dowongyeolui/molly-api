package org.example.mollyapi.user.auth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // 메소드 대상으로만 적용할게요~
@Retention(RetentionPolicy.RUNTIME) //런타임에만 유지할게요~
public @interface Auth {
}
