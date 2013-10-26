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
import java.util.Collection;

/**
 * A FIX metadata that describes mapping between a FIX repeating group a class field of a collection type.
 *
 * @author vladyslav.yatsenko
 */
public class FixGroupMeta extends FixDynamicFieldMeta {
    private final FixBlockMeta<?> componentMeta;
    private final int componentTag;
    private final Class<?> componentType;

    /**
     * Creates a FIX mapping metadata for a complex repeating FIX group (group of more that one FIX field).
     *
     * @param tag the repeating group FIX tag
     * @param header identifies whether the mapped FIX field is a part of the FIX message header
     * @param optional identifies whether the field is nullable
     * @param componentMeta the metadata of the group component
     * @param path the POJO field path used to resolve this field's value
     */
    public FixGroupMeta(int tag, boolean header, boolean optional, FixBlockMeta<?> componentMeta, Field... path) {
        super(tag, header, optional, path);
        this.componentMeta = componentMeta;
        this.componentTag = -1;
        this.componentType = componentMeta.getType();
    }

    /**
     * Creates a FIX mapping metadata for a simple repeating FIX group (group of scalar values).
     *
     * @param tag the repeating group FIX tag
     * @param header identifies whether the mapped FIX field is a part of the FIX message header
     * @param optional identifies whether the field is nullable
     * @param componentTag the FIX tag of the group component
     * @param componentType the Java type of the group component
     * @param path the POJO field path used to resolve this field's value
     */
    public FixGroupMeta(int tag,
                        boolean header,
                        boolean optional,
                        int componentTag,
                        Class<?> componentType,
                        Field... path) {
        super(tag, header, optional, path);
        this.componentMeta = null;
        this.componentTag = componentTag;
        this.componentType = componentType;
    }

    /**
     * @return the group component metadata if this meta represents a complex group, otherwise <code>null</code>.
     */
    public FixBlockMeta<?> getComponentMeta() {
        return componentMeta;
    }

    /**
     * Use this method before calling one of {@link #getComponentMeta()} of {@link #getComponentTag()}.
     *
     * @return true if this meta represents a simple repeating group, otherwise false.
     */
    public boolean isSimple() {
        return componentMeta == null;
    }

    /**
     * @return the group component FIX tag if this meta represents a simple group, otherwise -1.
     */
    public int getComponentTag() {
        return componentTag;
    }

    /**
     * @return the Java type of the group component.
     */
    public Class<?> getComponentType() {
        return componentType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Collection<?>> getType() {
        return (Class<? extends Collection<?>>) super.getType();
    }

    @Override
    public Collection<?> getValue(Object o) {
        return (Collection<?>) super.getValue(o);
    }

    /**
     * @return true.
     */
    @Override
    public boolean isGroup() {
        return true;
    }
}
