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
import org.fixb.meta.*;
import org.joda.time.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * An implementation of FixFieldExtractor that is used to extract field values straight from a raw string representation
 * of a FIX message. It is the fastest implementation of FixFieldExtractor as it does not create any intermediate
 * representation of extracted data.
 *
 * @author vladyslav.yatsenko
 */
public class NativeFixFieldExtractor implements FixFieldExtractor<String> {
    private final FixEnumDictionary fixEnumDictionary;

    public NativeFixFieldExtractor(FixEnumDictionary fixEnumDictionary) {
        this.fixEnumDictionary = fixEnumDictionary;
    }

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
    @SuppressWarnings("unchecked")
    <T> T extractFixBlock(FieldCursor cursor, FixBlockMeta<T> componentMeta) {
        final Map<FixFieldMeta, Object> values = new LinkedHashMap<>(componentMeta.getFields().size());

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
        T value = null;
        if (cursor.nextField(tag)) {
            value = toRequestedType(cursor.lastValue, type);
        }

        if (value != null) {
            return value;
        } else if (optional) {
            return null;
        } else {
            throw FixException.fieldNotFound(tag, cursor.fixMessage);
        }

    }

    @SuppressWarnings("unchecked")
    private <T> T toRequestedType(String value, Class<T> type) {
        if (type == String.class) return (T) value;
        if (type == Boolean.class || type == boolean.class) return (T) toBoolean(value);
        if (type == Character.class || type == char.class) return (T) (Character) value.charAt(0);
        if (type == Byte.class || type == byte.class) return (T) Byte.valueOf(value);
        if (type == Short.class || type == short.class) return (T) Short.valueOf(value);
        if (type == Integer.class || type == int.class) return (T) Integer.valueOf(value);
        if (type == Double.class || type == double.class) return (T) Double.valueOf(value);
        if (type == Float.class || type == float.class) return (T) Float.valueOf(value);
        if (type == BigDecimal.class) return (T) new BigDecimal(value);
        if (type == Date.class) return (T) toDate(value);
        if (type == LocalDate.class) return (T) toLocalDate(value);
        if (type == DateTime.class) return (T) toDateTime(value);
        if (type == LocalDateTime.class) return (T) toLocalDateTime(value);
        if (type == LocalTime.class) return (T) toLocalTime(value);
        if (type.isEnum()) return (T) toEnum(value, (Class<Enum>) type);
        return null;
    }

    private LocalDate toLocalDate(String value) {
        int[] f = extractDateFields(value);
        return new LocalDate(f[0], f[1], f[2]);
    }

    private LocalTime toLocalTime(String value) {
        int[] f = extractTimeFields(value, 4, "UTCTimeOnly");
        return new LocalTime(f[0], f[1], f[2], f[3]);
    }

    private LocalDateTime toLocalDateTime(String value) {
        int[] f = extractTimeFields(value, 5, "UTCTimestamp");
        int[] d = extractDateFields(f[0]);
        return new LocalDateTime(d[0], d[1], d[2], f[1], f[2], f[3], f[4]);
    }

    private DateTime toDateTime(String value) {
        int[] f = extractTimeFields(value, 6, "UTCTimestamp or TZTimestamp");
        int[] d = extractDateFields(f[0]);
        return new DateTime(d[0], d[1], d[2], f[1], f[2], f[3], DateTimeZone.forOffsetHoursMinutes(f[4], f[5]));
    }

    private int[] extractDateFields(int value) {
        return new int[]{
                value / 10000,
                value / 100 - value / 10000 * 100,
                value - value / 100 * 100
        };
    }

    private int[] extractTimeFields(String value, int fieldCount, String fixTypeName) {
        if (value.length() < 5 && value.length() > 23) {
            throw new FixException("Expected " + fixTypeName + ", got: " + value);
        }
        IntTokenizer tokenizer = new IntTokenizer(value, ":-+Z");
        try {
            return tokenizer.nextNWithDefault(fieldCount, 0);
        } catch (NumberFormatException e) {
            throw new FixException("Expected " + fixTypeName + ", got: " + value);
        }
    }

    private int[] extractDateFields(String value) {
        if (value.length() != 8) {
            throw new FixException("Expected UTCDateOnly or LocalMktDate, got: " + value);
        }
        try {
            return extractDateFields(Integer.valueOf(value));
        } catch (NumberFormatException e) {
            throw new FixException("Expected UTCDateOnly or LocalMktDate, got: " + value);
        }
    }

    private Boolean toBoolean(String value) {
        return "Y".equals(value) || "1".equals(value);
    }

    private Enum<?> toEnum(String value, Class<Enum> type) {
        int fieldValue = Integer.valueOf(value);

        if (fixEnumDictionary.hasFixEnumMeta(type)) {
            return fixEnumDictionary.getFixEnumMeta(type).enumForFixValue(value);
        }

        for (Enum enumValue : type.getEnumConstants()) {
            int ordValue = enumValue.ordinal() + 1;
            if (ordValue == fieldValue) {
                return enumValue;
            }
        }
        throw new IllegalArgumentException("Invalid ordinal of enum type " + type + ": " + fieldValue);
    }

    private Date toDate(String value) {
        return toDateTime(value).toDate();
    }

    static class IntTokenizer {
        private final StringTokenizer tokenizer;

        IntTokenizer(String s, String delimiters) {
            tokenizer = new StringTokenizer(s, delimiters);
        }

        boolean hasNext() {
            return tokenizer.hasMoreTokens();
        }

        int next() {
            return Integer.parseInt(tokenizer.nextToken());
        }

        int[] nextNWithDefault(int n, int def) {
            int[] result = new int[n];
            for (int i = 0; i < n; i++) {
                result[i] = hasNext() ? next() : def;
            }
            return result;
        }
    }
}
