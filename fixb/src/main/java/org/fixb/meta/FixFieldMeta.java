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

package org.fixb.meta;

import java.lang.reflect.Field;

/**
 * A base class for FIX field bindings metadata.
 *
 * @author vladyslav.yatsenko
 */
public abstract class FixFieldMeta {
    private final int tag;
    private final boolean header;
    private final boolean optional;

    /**
     * A factory method for constant FixFieldMeta.
     */
    public static FixFieldMeta fixFieldMeta(int tag, Object value, boolean header) {
        return new FixConstantFieldMeta(tag, header, value);
    }

    /**
     * A factory method for dynamic FixFieldMeta.
     */
    public static FixFieldMeta fixFieldMeta(int tag, boolean header, boolean optional, Field... path) {
        return new FixDynamicFieldMeta(tag, header, optional, path);
    }

    /**
     * A factory method for FixGroupMeta (simple type group).
     */
    public static FixGroupMeta fixGroupMeta(int groupTag,
                                            boolean header,
                                            boolean optional,
                                            int componentTag,
                                            Class<?> componentType,
                                            Field... path) {
        return new FixGroupMeta(groupTag, header, optional, componentTag, componentType, path);
    }

    /**
     * A factory method for FixGroupMeta (complex type group).
     */
    public static FixGroupMeta fixGroupMeta(int groupTag,
                                            boolean header,
                                            boolean optional,
                                            FixBlockMeta<?> componentMeta,
                                            Field... path) {
        return new FixGroupMeta(groupTag, header, optional, componentMeta, path);
    }

    /**
     * @param tag the FIX tag
     * @param header identifies whether the mapped FIX field is a part of the FIX message header
     * @param optional identifies whether the field is nullable
     */
    protected FixFieldMeta(int tag, boolean header, boolean optional) {
        this.tag = tag;
        this.header = header;
        this.optional = optional;
    }

    public int getTag() {
        return tag;
    }

    /**
     * @return true if a FIX header field, false otherwise
     */
    public boolean isHeader() {
        return header;
    }

    /**
     * @return true if this field is optional (tag can be null, or empty in case of a collection), false otherwise.
     */
    public boolean isOptional() {
        return optional;
    }

    /**
     * @param object the object to extract the tag from.
     * @return field tag from the given object
     */
    public abstract Object getValue(Object object);

    /**
     * @return the type of this field's values.
     */
    public abstract Class<?> getType();

    /**
     * @return true if this metadata represents a FIX repeating group, false otherwise.
     */
    public abstract boolean isGroup();
}
