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

package org.fixb.quickfix;

import org.fixb.FixException;
import org.fixb.FixFieldExtractor;
import org.fixb.impl.CollectionFactory;
import org.fixb.meta.*;
import org.joda.time.*;
import org.joda.time.format.DateTimeFormatterBuilder;
import quickfix.*;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An implementation of FixFieldExtractor that is used to extract data from quickfix.Message objects.
 *
 * @author vladyslav.yatsenko
 * @see quickfix.Message
 */
public class QuickFixFieldExtractor implements FixFieldExtractor<Message> {
    @Override
    public <T> T getFieldValue(Message message, Class<T> type, int tag, boolean optional) {
        T fieldValue = getFieldValueFromMap(message, type, tag);
        if (fieldValue == null) {
            if (!optional) {
                throw new FixException("Field [" + tag + "] was not found in message");
            }
            fieldValue = getFieldValueFromMap(message.getHeader(), type, tag);
        }

        return fieldValue;
    }

    @Override
    public <T, C extends Collection<T>> C getGroups(Message fixMessage,
                                                    Class<C> type,
                                                    int groupTag,
                                                    Class<T> elementType,
                                                    int elementTag,
                                                    boolean optional) {
        C groups = getGroupsFromMap(fixMessage, type, groupTag, elementType, elementTag);
        if (groups.isEmpty()) {
            groups = getGroupsFromMap(fixMessage.getHeader(), type, groupTag, elementType, elementTag);
        }
        if (!optional && groups.isEmpty()) {
            throw new FixException("Group [" + groupTag + "] was not found in message");
        }
        return groups;
    }

    @Override
    public <T, C extends Collection<T>> C getGroups(Message fixMessage,
                                                    Class<C> type,
                                                    int groupTag,
                                                    FixBlockMeta<T> componentMeta,
                                                    boolean optional) {
        C groups = getGroupsFromMap(fixMessage, type, groupTag, componentMeta);
        if (groups.isEmpty()) {
            groups = getGroupsFromMap(fixMessage.getHeader(), type, groupTag, componentMeta);
        }
        if (!optional && groups.isEmpty()) {
            throw new FixException("Group [" + groupTag + "] was not found in message");
        }
        return groups;
    }

    @SuppressWarnings("unchecked")
    private <T, C extends Collection<T>> C getGroupsFromMap(FieldMap fieldMap,
                                                            Class<C> type,
                                                            int tag,
                                                            FixBlockMeta<T> componentMeta) {
        try {
            final C result = CollectionFactory.createCollection(type);
            for (Group group : fieldMap.getGroups(tag)) {
                final Map<FixFieldMeta, Object> values = new LinkedHashMap<>();
                for (FixFieldMeta f : componentMeta.getFields()) {
                    if (!(f instanceof FixConstantFieldMeta)) {
                        Object fieldValue;
                        if (f.isGroup()) {
                            FixGroupMeta groupMeta = (FixGroupMeta) f;
                            if (groupMeta.isSimple()) {
                                fieldValue = getGroupsFromMap(group,
                                        (Class<Collection<Object>>) groupMeta.getType(),
                                        groupMeta.getTag(),
                                        (Class<Object>) groupMeta.getComponentType(),
                                        groupMeta.getComponentTag());
                            } else {
                                fieldValue = getGroupsFromMap(group,
                                        (Class<Collection<Object>>) groupMeta.getType(),
                                        groupMeta.getTag(),
                                        (FixMessageMeta<Object>) groupMeta.getComponentMeta());
                            }
                        } else {
                            fieldValue = getFieldValueFromMap(group, f.getType(), f.getTag());
                        }
                        values.put(f, fieldValue);
                    }
                }
                result.add(componentMeta.createModel(values));
            }
            return result;
        } catch (Exception e) {
            throw new FixException(e.getMessage(), e);
        }
    }

    private <T, C extends Collection<T>> C getGroupsFromMap(FieldMap fieldMap,
                                                            Class<C> type,
                                                            int tag,
                                                            Class<T> elementType,
                                                            int elementTag) {
        try {
            final C result = CollectionFactory.createCollection(type);
            for (Group group : fieldMap.getGroups(tag)) {
                result.add(getFieldValueFromMap(group, elementType, elementTag));
            }
            return result;
        } catch (Exception e) {
            throw new FixException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getFieldValueFromMap(FieldMap message, Class<T> type, int tag) {
        try {
            if (type == String.class) {
                return (T) message.getString(tag);
            } else if (type == Boolean.class || type == boolean.class) {
                return (T) Boolean.valueOf(message.getBoolean(tag));
            } else if (type == Character.class || type == char.class) {
                return (T) Character.valueOf(message.getChar(tag));
            } else if (type == Integer.class || type == int.class) {
                return (T) Integer.valueOf(message.getInt(tag));
            } else if (type == Double.class || type == double.class) {
                return (T) Double.valueOf(message.getDouble(tag));
            } else if (type == BigDecimal.class) {
                return (T) message.getDecimal(tag);
            } else if (type == Instant.class) {
                return (T) new Instant(message.getUtcTimeStamp(tag));
            } else if (type == LocalDate.class) {
                String dateString = message.getString(tag);
                return (T) LocalDate.parse(dateString, new DateTimeFormatterBuilder().appendPattern("yyyyMMdd").toFormatter());
            } else if (type == LocalTime.class) {
                Date utcTime = message.getUtcTimeOnly(tag);
                return (T) new DateTime(utcTime, DateTimeZone.getDefault()).toLocalTime();
            } else if (type == LocalDateTime.class) {
                Date utcTime = message.getUtcTimeStamp(tag);
                return (T) new DateTime(utcTime, DateTimeZone.getDefault()).toLocalDateTime();
            } else if (type == DateTime.class) {
                Date utcDate = message.getUtcTimeStamp(tag);
                return (T) new DateTime(utcDate, DateTimeZone.UTC);
            } else if (type == Date.class) {
                return (T) message.getUtcTimeStamp(tag);
            } else if (type.isEnum()) {
                int fieldValue = message.getInt(tag);
                for (T enumValue : type.getEnumConstants()) {
                    int ordValue = ((Enum) enumValue).ordinal() + 1;
                    if (ordValue == fieldValue) {
                        return enumValue;
                    }
                }
                throw new IllegalArgumentException("Invalid ordinal of enum type " + type + ": " + fieldValue);
            } else {
                throw new IllegalArgumentException("Can't map type to FIX type: " + type);
            }
        } catch (FieldNotFound e) {
            return null;
        } catch (FieldException e) {
            throw new FixException(e);
        }
    }

}
