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

package org.fixb.impl;

import org.fixb.FixException;

import java.lang.reflect.Modifier;
import java.util.*;

/**
 * I am a static factory for Collection types.
 *
 * @author vladyslav.yatsenko
 */
public class CollectionFactory {
    /**
     * Creates an empty instance of the given collection type.
     * If the given an class in an interface, an ArrayList will be created for java.util.List
     * or java.util.Collection interfaces, and HashSet for java.util.Set interface.
     */
    public static <T extends Collection<?>> T createCollection(Class<T> collClass) {
        if (!collClass.isInterface() && !Modifier.isAbstract(collClass.getModifiers())) {
            try {
                return collClass.newInstance();
            } catch (InstantiationException e) {
                throw new FixException(e.getMessage(), e);
            } catch (IllegalAccessException e) {
                throw new FixException(e.getMessage(), e);
            }
        } else if (List.class.isAssignableFrom(collClass)) {
            return (T) new ArrayList<Object>();
        } else if (Set.class.isAssignableFrom(collClass)) {
            return (T) new HashSet<Object>();
        } else if (Collection.class.isAssignableFrom(collClass)) {
            return (T) new ArrayList<Object>();
        } else {
            throw new IllegalArgumentException("Unknown collection type: " + collClass);
        }
    }
}
