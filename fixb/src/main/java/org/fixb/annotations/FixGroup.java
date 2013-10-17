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

package org.fixb.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to map a field of a collection type to a FIX group.
 *
 * @author vladyslav.yatsenko
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD})
public @interface FixGroup {

    /**
     * A number representing the repeating FIX group tag, its value will be a number of elements in the collection.
     */
    int tag();

    /**
     * The delimiter tag or the tag used for the collection elements in case of a collection of
     * simple values (single field values). It's ignored when elements are of a complex type.
     */
    int componentTag() default 0;

    /**
     * The type of the collection elements. If not specified it will be derived from the generic type's parameter.
     */
    Class<?> component() default Void.class;

    /**
     * True if the field is a part of the FIX message header, false otherwise.
     */
    boolean header() default false;

    /**
     * True if the field is optional, false otherwise
     */
    boolean optional() default false;
}
