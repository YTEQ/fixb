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
 * When used on a type/class this annotation marks that class as a component that represents a re-usable set of fields.
 * Field references of the annotated class type within another class annotated with @FixBlock or @FixMessage must be
 * accompanied by this annotation too to identify the intention of mixing the fields in.
 *
 * For the sake of some performance benefit a constructor based instantiation can be used, in which case corresponding
 * constructor parameters must also be annotated with @FixBlock annotation.
 *
 * @author vladyslav.yatsenko
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.TYPE})
public @interface FixBlock {
}
