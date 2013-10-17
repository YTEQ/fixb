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
 * This annotation is used to map a Java class to a FIX message type.
 *
 * @author vladyslav.yatsenko
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FixMessage {

    /**
     * A FIX message type (FIX tag 35).
     */
    String type();

    /**
     * An optional set of @Field annotations defining literal FIX fields to be included in the FIX message header.
     */
    Field[] header() default {};

    /**
     * An optional set of @Field annotations defining literal FIX fields to be included in the FIX message body.
     */
    Field[] body() default {};

    public @interface Field {
        int tag();

        String value();
    }
}
