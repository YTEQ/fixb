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

package org.fix4j;

import org.fix4j.meta.FixFieldMeta;
import org.joda.time.*;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * I am a builder of FIX message objects, which consist of fields and groups.
 *
 * @author vladyslav.yatsenko
 */
public abstract class FixMessageBuilder<M> {
    /**
     * Since the FixMessageBuilder objects are not re-usable, I am a factory that is used to create new instances.
     *
     * @param <M> the type of FIX message object to build
     * @param <B> specific FixMessageBuilder implementation (in most of cases can be simply FixMessageBuilder<M>)
     */
    public interface Factory<M, B extends FixMessageBuilder<M>> {
        /**
         * @return a new instance of FixMessageBuilder of type B with an empty FIX message.
         */
        B create();

        /**
         * @param fixMessage an initial FIX message
         * @return a new instance of FixMessageBuilder of type B pre-populated with the given initial FIX message.
         */
        B createWithMessage(M fixMessage);
    }

    /**
     * @return a library specific FIX message object prepared by a sequence of invocations of setField and setGroups.
     */
    public abstract M build();

    /**
     * Sets the field identified by the given tag to the given <tt>char</tt> tag.
     *
     * @param header identifies whether the field is a header field
     * @return the current builder instance.
     */
    public abstract FixMessageBuilder<M> setField(int tag, char value, boolean header);

    /**
     * Sets the field identified by the given tag to the given <tt>int</tt> tag.
     *
     * @param header identifies whether the field is a header field
     * @return the current builder instance.
     */
    public abstract FixMessageBuilder<M> setField(int tag, int value, boolean header);

    /**
     * Sets the field identified by the given tag to the given <tt>double</tt> tag.
     *
     * @param header identifies whether the field is a header field
     * @return the current builder instance.
     */
    public abstract FixMessageBuilder<M> setField(int tag, double value, boolean header);

    /**
     * Sets the field identified by the given tag to the given <tt>boolean</tt> tag.
     *
     * @param header identifies whether the field is a header field
     * @return the current builder instance.
     */
    public abstract FixMessageBuilder<M> setField(int tag, boolean value, boolean header);

    /**
     * Sets the field identified by the given tag to the given <tt>BigDecimal</tt> tag.
     *
     * @param header identifies whether the field is a header field
     * @return the current builder instance.
     */
    public abstract FixMessageBuilder<M> setField(int tag, BigDecimal value, boolean header);

    /**
     * Sets the field identified by the given tag to the given <tt>LocalDate</tt> tag (converting it to UTC date).
     *
     * @param header identifies whether the field is a header field
     * @return the current builder instance.
     * @see org.joda.time.LocalDate
     */
    public abstract FixMessageBuilder<M> setField(int tag, LocalDate value, boolean header);

    /**
     * Sets the field identified by the given tag to the given <tt>LocalTime</tt> tag (converting it to UTC time).
     *
     * @param header identifies whether the field is a header field
     * @return the current builder instance.
     * @see org.joda.time.LocalTime
     */
    public abstract FixMessageBuilder<M> setField(int tag, LocalTime value, boolean header);

    /**
     * Sets the field identified by the given tag to the given <tt>LocalDateTime</tt> tag (converting it to UTC timestamp).
     *
     * @param header identifies whether the field is a header field
     * @return the current builder instance.
     * @see org.joda.time.LocalDateTime
     */
    public abstract FixMessageBuilder<M> setField(int tag, LocalDateTime value, boolean header);

    /**
     * Sets the field identified by the given tag to the given <tt>DateTime</tt> tag (converting it to UTC timestamp).
     *
     * @param header identifies whether the field is a header field
     * @return the current builder instance.
     * @see org.joda.time.DateTime
     */
    public abstract FixMessageBuilder<M> setField(int tag, DateTime value, boolean header);

    /**
     * Sets the field identified by the given tag to the given <tt>Instant</tt> tag (converting it to UTC timestamp).
     *
     * @param header identifies whether the field is a header field
     * @return the current builder instance.
     * @see org.joda.time.Instant
     */
    public abstract FixMessageBuilder<M> setField(int tag, Instant value, boolean header);

    /**
     * Sets the field identified by the given tag to the given <tt>java.util.Date</tt> tag (converting it to UTC timestamp).
     *
     * @param header identifies whether the field is a header field
     * @return the current builder instance.
     * @see java.util.Date
     */
    public abstract FixMessageBuilder<M> setField(int tag, Date value, boolean header);

    /**
     * Sets the field identified by the given tag to the given <tt>String</tt> tag.
     *
     * @param header identifies whether the field is a header field
     * @return the current builder instance.
     */
    public abstract FixMessageBuilder<M> setField(int tag, String value, boolean header);

    /**
     * Sets the field identified by the given tag to the given <tt>enum</tt> tag.
     *
     * @param header identifies whether the field is a header field
     * @return the current builder instance.
     */
    public abstract FixMessageBuilder<M> setField(int tag, Enum<?> value, boolean header);

    /**
     * Sets the repeating groups identified by the given tag. The given collection can contain only simple types
     * (the ones that have corresponding setField method). Single elements will be included as repeating groups with
     * the <tt>componentTag</tt> tag.
     *
     * @param header identifies whether the field is a header field
     * @return the current builder instance.
     */
    public abstract FixMessageBuilder<M> setGroups(int tag, int componentTag, Collection<?> collection, boolean header);

    /**
     * Sets the repeating groups identified by the given tag. The given collection can contain only complex types
     * that correspond to FIX blocks. FIX mappings for the latter are defined by the given list of
     * <tt>FixFieldMeta</tt>.
     *
     * @param header identifies whether the groups should be added in the message header
     * @return the current builder instance.
     */
    public abstract FixMessageBuilder<M> setGroups(int tag,
                                                   List<FixFieldMeta> fields,
                                                   Collection<?> collection,
                                                   boolean header);

    /**
     * This is a convenience method that sets the given field in the message body.
     */
    public FixMessageBuilder<M> setField(int tag, Object value) {
        return setField(tag, value, false);
    }

    /**
     * This is a generic setField implementation the invokes appropriate setField method for a dynamic type of the
     * given tag.
     *
     * @throws IllegalArgumentException if the type of the given tag can't be mapped to a FIX type.
     */
    public FixMessageBuilder<M> setField(int tag, Object value, boolean header) {
        if (value instanceof String) {
            setField(tag, (String) value, header);
        } else if (value instanceof Character) {
            setField(tag, ((Character) value).charValue(), header);
        } else if (value instanceof Integer) {
            setField(tag, ((Integer) value).intValue(), header);
        } else if (value instanceof Double) {
            setField(tag, ((Double) value).doubleValue(), header);
        } else if (value instanceof BigDecimal) {
            setField(tag, (BigDecimal) value, header);
        } else if (value instanceof Date) {
            setField(tag, (Date) value, header);
        } else if (value instanceof LocalDate) {
            setField(tag, (LocalDate) value, header);
        } else if (value instanceof LocalDateTime) {
            setField(tag, (LocalDateTime) value, header);
        } else if (value instanceof DateTime) {
            setField(tag, (DateTime) value, header);
        } else if (value instanceof LocalTime) {
            setField(tag, (LocalTime) value, header);
        } else if (value instanceof Instant) {
            setField(tag, (Instant) value, header);
        } else if (value instanceof Boolean) {
            setField(tag, ((Boolean) value).booleanValue(), header);
        } else if (value instanceof Enum) {
            setField(tag, (Enum) value, header);
        } else {
            if (value != null) {
                throw new IllegalArgumentException("Can't map type to FIX type: " + value.getClass());
            }
        }

        return this;
    }

    /**
     * This is a convenience method that sets the given groups in the message body.
     *
     * @see #setGroups(int, int, java.util.Collection, boolean)
     */
    public FixMessageBuilder<M> setGroups(int tag, int componentTag, Collection<?> collection) {
        return setGroups(tag, componentTag, collection, false);
    }

    /**
     * This is a convenience method that sets the given groups in the message body.
     *
     * @see #setGroups(int, java.util.List, java.util.Collection, boolean)
     */
    public FixMessageBuilder<M> setGroups(int tag, List<FixFieldMeta> fields, Collection<?> collection) {
        return setGroups(tag, fields, collection, false);
    }
}
