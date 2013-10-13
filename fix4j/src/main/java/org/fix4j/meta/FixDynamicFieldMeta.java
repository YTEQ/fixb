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

package org.fix4j.meta;

import org.fix4j.FixException;

import java.lang.reflect.Field;

/**
 * I define mapping of a FIX field to a POJO field.
 *
 * @author vladyslav.yatsenko
 */
public class FixDynamicFieldMeta extends FixFieldMeta {
    private final Field[] path;
    private final Class<?> type;

    /**
     * @param tag the FIX tag
     * @param header identifies whether the mapped FIX field is a part of the FIX message header
     * @param optional identifies whether the field is nullable
     * @param path the POJO field path used to resolve this field's value
     */
    public FixDynamicFieldMeta(int tag, boolean header, boolean optional, Field... path) {
        super(tag, header, optional);
        if (path.length < 1) {
            throw new IllegalArgumentException("Field path is empty");
        }
        this.path = path;
        this.type = path[path.length - 1].getType();
        for (Field f : path) {
            f.setAccessible(true);
        }
    }

    /**
     * @return the name of the class field
     */
    public String getName() {
        return path[path.length - 1].getName();
    }

    /**
     * @return the type of this field's value
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * @return false.
     */
    public boolean isGroup() {
        return false;
    }

    /**
     * Resolves the field's value using the given object.
     * @param o the object containing the field's value.
     * @return
     */
    public Object getValue(Object o) {
        try {
            Object value = o;
            for (Field f : path) {
                value = f.get(value);
            }
            return value;
        } catch (IllegalAccessException e) {
            throw new FixException("Error while reading tag from path: " + getPathString(), e);
        }
    }

    /**
     * @return this field's path.
     */
    Field[] getPath() {
        return path;
    }

    private String getPathString() {
        StringBuilder s = new StringBuilder();
        for (Field f : path) {
            s.append('.').append(f.getName());
        }
        return s.substring(1);
    }
}
