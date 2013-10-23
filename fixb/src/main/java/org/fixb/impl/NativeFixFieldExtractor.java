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
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.fixb.impl.FormatConstants.*;

/**
 * I am an implementation of FixFieldExtractor. I extract extract field values straight from a raw (represented as a String) FIX message,
 * I use FieldCursor for that. I am the fastest implementation of FixFieldExtractor as I don't create any intermediate objects.
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
        if (type == Date.class) return (T) toDate(value, DATE_TIME_FORMAT);
        if (type == LocalDate.class) return (T) toLocalDate(value);
        if (type == Instant.class) return (T) toInstant(value);
        if (type == DateTime.class) return (T) toDateTime(value);
        if (type == LocalDateTime.class) return (T) toLocalDateTime(value);
        if (type == LocalTime.class) return (T) toLocalTime(value);
        if (type.isEnum()) return (T) toEnum(value, (Class<Enum>) type);
        return null;
    }

    private LocalTime toLocalTime(String value) {
        return LocalTime.parse(value, dtFormatterFor(value).withZoneUTC());
    }

    private DateTimeFormatter dtFormatterFor(String value) {
        final String pattern;
        switch (value.length()) {
            case 8: pattern = value.indexOf(':') > 1 ? TIME_FORMAT : DATE_FORMAT; break;
            case 12: pattern = TIME_FORMAT_WITH_MILLIS; break;
            case 17: pattern = DATE_TIME_FORMAT; break;
            case 21: pattern = DATE_TIME_FORMAT_WITH_MILLIS; break;
            default: throw new FixException("Invalid date/time value: " + value);
        }
        return new DateTimeFormatterBuilder().appendPattern(pattern).toFormatter();
    }

    private LocalDateTime toLocalDateTime(String value) {
        return toDateTime(value).toLocalDateTime();
    }

    private DateTime toDateTime(String value) {
        return DateTime.parse(value, dtFormatterFor(value));
    }

    private Instant toInstant(String value) {
        return Instant.parse(value, dtFormatterFor(value));
    }

    private LocalDate toLocalDate(String value) {
        return LocalDate.parse(value, dtFormatterFor(value));
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

    private Date toDate(String value, String format) {
        try {
            return new SimpleDateFormat(format).parse(value);
        } catch (ParseException e) {
            throw new FixException("Expected date, got: " + value);
        }
    }
}
