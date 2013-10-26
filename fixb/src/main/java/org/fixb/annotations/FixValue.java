package org.fixb.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that defines a FIX constant mapped to the associated enum constant.
 *
 * @author vladyslav.yatsenko
 * @see FixEnum
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface FixValue {
    String value();
}
