package org.fixb.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The @FixEnum annotation is applicable to enums only and identifies that the enum has FIX bindings.
 * The FIX bindings are defined by applying @FixValue annotation to the enum values: all values must be annotated.
 *
 * @author vladyslav.yatsenko
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface FixEnum {
}
