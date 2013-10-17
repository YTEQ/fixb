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
import org.fixb.FixFieldExtractor;
import org.fixb.meta.FixBlockMeta;
import org.fixb.meta.FixConstantFieldMeta;
import org.fixb.meta.FixFieldMeta;
import org.fixb.meta.FixGroupMeta;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.fixb.impl.FormatConstants.DATE_FORMAT;

/**
 * I am an implementation of FixFieldExtractor. I extract extract field values straight from a raw (represented as a String) FIX message,
 * I use FieldCursor for that. I am the fastest implementation of FixFieldExtractor as I don't create any intermediate objects.
 *
 * @author vladyslav.yatsenko
 */
public class NativeFixFieldExtractor implements FixFieldExtractor<String> {

    @Override
    public <T> T getFieldValue(String fixMessage, Class<T> type, int tag, boolean optional) {
        return extractFieldValue(FieldCursor.create(fixMessage), tag, type, optional);
    }

    @Override
    public <T, C extends Collection<T>> C getGroups(String fixMessage, Class<C> type, int groupTag, Class<T> elementType, int elementTag, boolean optional) {
        return getGroups(FieldCursor.create(fixMessage), type, groupTag, elementType, elementTag, optional);
    }

    @Override
    public <T, C extends Collection<T>> C getGroups(String fixMessage, Class<C> type, int groupTag, FixBlockMeta<T> componentMeta, boolean optional) {
        return getGroups(FieldCursor.create(fixMessage), type, groupTag, componentMeta, optional);
    }

    /**
     * Extracts values using the given FieldCursor that are defined by the given FixBlockMeta and creates an object as defined by the block meta.
     *
     * @param cursor        a FieldCursor operating on the FIX message of interest
     * @param componentMeta a metadata with FIX mappings used to create the resulting POJO
     * @param <T>           a type of object to create
     * @return an instance of type T populated with values from the given FieldCursor.
     */
    <T> T extractFixBlock(FieldCursor cursor, FixBlockMeta<T> componentMeta) {
        final Map<FixFieldMeta, Object> values = new LinkedHashMap<FixFieldMeta, Object>(componentMeta.getFields().size());

        for (FixFieldMeta f : componentMeta.getFields()) {
            if (f instanceof FixConstantFieldMeta) continue;

            final Object fieldValue;
            if (f.isGroup()) {
                final FixGroupMeta groupMeta = (FixGroupMeta) f;
                fieldValue = groupMeta.isSimple() ?
                        getGroups(cursor,
                                (Class<Collection<Object>>) groupMeta.getType(),
                                groupMeta.getTag(),
                                (Class<Object>) groupMeta.getComponentType(),
                                groupMeta.getComponentTag(),
                                groupMeta.isOptional()) :
                        getGroups(cursor,
                                (Class<Collection<Object>>) groupMeta.getType(),
                                groupMeta.getTag(),
                                (FixBlockMeta<Object>) groupMeta.getComponentMeta(),
                                groupMeta.isOptional());
            } else {
                fieldValue = extractFieldValue(cursor, f.getTag(), f.getType(), f.isOptional());
            }
            values.put(f, fieldValue);
        }
        return componentMeta.createModel(values);
    }

    private <T, C extends Collection<T>> C getGroups(FieldCursor cursor, Class<C> type, int groupTag, FixBlockMeta<T> componentMeta, boolean optional) {
        if (!cursor.nextField(groupTag)) {
            if (optional) {
                return CollectionFactory.createCollection(type);
            } else {
                throw FixException.fieldNotFound(groupTag, cursor.fixMessage);
            }
        }

        final int count = Integer.parseInt(cursor.lastValue);
        final C result = CollectionFactory.createCollection(type);

        for (int i = 0; i < count; i++) {
            result.add(extractFixBlock(cursor, componentMeta));
        }
        return result;
    }

    private <T, C extends Collection<T>> C getGroups(FieldCursor cursor, Class<C> type, int groupTag, Class<T> elementType, int elementTag, boolean optional) {
        if (!cursor.nextField(groupTag)) {
            if (optional) {
                return CollectionFactory.createCollection(type);
            } else {
                throw FixException.fieldNotFound(groupTag, cursor.fixMessage);
            }
        }

        final int count = Integer.parseInt(cursor.lastValue);
        final C result = CollectionFactory.createCollection(type);

        for (int i = 0; i < count && cursor.nextField(elementTag); i++) {
            result.add(toRequestedType(cursor.lastValue, elementType));
        }

        return result;
    }

    private <T> T extractFieldValue(FieldCursor cursor, int tag, Class<T> type, boolean optional) {
        if (cursor.nextField(tag)) {
            return toRequestedType(cursor.lastValue, type);
        } else if (optional) {
            return null;
        } else {
            throw FixException.fieldNotFound(tag, cursor.fixMessage);
        }

    }

    private <T> T toRequestedType(String value, Class<T> type) {
        if (type == String.class) return (T) value;
        if (type == Boolean.class || type == boolean.class)
            return (T) Boolean.valueOf("Y".equals(value) || "1".equals(value));
        if (type == Character.class || type == char.class) return (T) (Character) value.charAt(0);
        if (type == Byte.class || type == byte.class) return (T) Byte.valueOf(value);
        if (type == Short.class || type == short.class) return (T) Short.valueOf(value);
        if (type == Integer.class || type == int.class) return (T) Integer.valueOf(value);
        if (type == Double.class || type == double.class) return (T) Double.valueOf(value);
        if (type == Float.class || type == float.class) return (T) Float.valueOf(value);
        if (type == BigDecimal.class) return (T) new BigDecimal(value);
        if (type == Date.class) try {
            return (T) new SimpleDateFormat(DATE_FORMAT).parse(value);
        } catch (ParseException e) {
            new FixException("Expected date, got: " + value);
        }
        return null;
    }

}
