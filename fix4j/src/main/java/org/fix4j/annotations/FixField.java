/*
 * Copyright 2013 YTEQ Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fix4j.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When used in front of a Java class field it defines mapping of that field to a FIX field and can be used to map
 * simple (single field) types only. It must be repeated in front of a constructor parameter that is used to initialize
 * the annotated field.
 *
 * @author vladyslav.yatsenko
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD})
public @interface FixField {
    /**
     * A FIX tag to map the annotated field to.
     */
    int tag();

    /**
     * True if the field is to be a part of the FIX message header, false otherwise.
     */
    boolean header() default false;

    /**
     * True if the field is optional, false otherwise.
     */
    boolean optional() default false;
}
