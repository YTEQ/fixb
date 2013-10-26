package org.fixb.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that is only applicable to enums and is used to identify that the enum's values have FIX bindings.
 * The enum values FIX bindings are defined by applying @FixValue annotation to the enum values: all values must be
 * annotated.
 *
 * @author vladyslav.yatsenko
 * @see FixValue
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface FixEnum {
}
