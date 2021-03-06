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
import org.fixb.FixMessageBuilder;
import org.fixb.meta.FixEnumDictionary;
import org.fixb.meta.FixEnumMeta;
import org.fixb.meta.FixFieldMeta;
import org.fixb.meta.FixGroupMeta;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatterBuilder;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.fixb.FixConstants.*;
import static org.fixb.impl.FormatConstants.*;

/**
 * An implementation of FixMessageBuilder that is used to build raw (represented as a String) FIX messages ready for
 * transmission. It it the fastest implementation as it does not create any intermediate objects for the extracted data.
 *
 * @author vladyslav.yatsenko
 */
public final class NativeFixMessageBuilder extends FixMessageBuilder<String> {
    private final FixEnumDictionary fixEnumDictionary;
    private final StringBuilder head = new StringBuilder();
    private final StringBuilder body;
    private String beginString;

    public static final class Factory implements FixMessageBuilder.Factory<String, NativeFixMessageBuilder> {

        private final FixEnumDictionary fixEnumDictionary;

        public Factory(FixEnumDictionary fixEnumDictionary) {
            this.fixEnumDictionary = fixEnumDictionary;
        }

        @Override
        public NativeFixMessageBuilder create() {
            return new NativeFixMessageBuilder(fixEnumDictionary, new StringBuilder());
        }

        @Override
        public NativeFixMessageBuilder createWithMessage(String fixMessage) {
            return new NativeFixMessageBuilder(fixEnumDictionary, new StringBuilder(fixMessage));
        }
    }

    @Override
    public String build() {
        if (beginString == null) {
            throw new FixException("BeginString (tag " + BEGIN_STRING_TAG + ") is missing");
        }

        final StringBuilder headAndBody = head.append(body);
        final int bodyLength = headAndBody.length();
        final StringBuilder beginString = appendTag(new StringBuilder(bodyLength + 10), BEGIN_STRING_TAG).append(this.beginString).append(SOH);
        final StringBuilder message = appendTag(beginString, BODY_LENGTH_TAG).append(bodyLength).append(SOH).append(headAndBody);

        return appendTag(message, CHECKSUM_TAG).append(generateCheckSum(message)).toString();
    }

    @Override
    public FixMessageBuilder<String> setField(int tag, char value, boolean header) {
        appendTag(tag, header).append(value).append(SOH);
        return this;
    }

    @Override
    public FixMessageBuilder<String> setField(int tag, int value, boolean header) {
        appendTag(tag, header).append(value).append(SOH);
        return this;
    }

    @Override
    public FixMessageBuilder<String> setField(int tag, double value, boolean header) {
        appendTag(tag, header).append(value).append(SOH);
        return this;
    }

    @Override
    public FixMessageBuilder<String> setField(int tag, boolean value, boolean header) {
        appendTag(tag, header).append(value ? 'Y' : 'N').append(SOH);
        return this;
    }

    @Override
    public FixMessageBuilder<String> setField(int tag, BigDecimal value, boolean header) {
        return setField(tag, value.toString(), header);
    }

    @Override
    public FixMessageBuilder<String> setField(int tag, LocalDate value, boolean header) {
        return setField(tag, value.toString(DATE), header);
    }

    @Override
    public FixMessageBuilder<String> setField(int tag, LocalTime value, boolean header) {
        final String pattern = (value.getMillisOfSecond() == 0) ? TIME : TIME_WITH_MILLIS;
        return setField(tag, value.toString(pattern), header);
    }

    @Override
    public FixMessageBuilder<String> setField(int tag, LocalDateTime value, boolean header) {
        final String pattern = (value.getMillisOfSecond() == 0) ? DATE_TIME : DATE_TIME_WITH_MILLIS;
        return setField(tag, value.toString(pattern), header);
    }

    @Override
    public FixMessageBuilder<String> setField(int tag, DateTime value, boolean header) {
        final String pattern = DATE_TIME_WITH_TZ;
        return setField(tag, new DateTimeFormatterBuilder().appendPattern(pattern).toFormatter().withZone(value.getZone()).print(value), header);
    }

    @Override
    public FixMessageBuilder<String> setField(int tag, Date value, boolean header) {
        final String pattern = (value.getTime() % 1000 == 0) ? DATE_TIME : DATE_TIME_WITH_MILLIS;
        return setField(tag, new SimpleDateFormat(pattern).format(value), header);
    }

    @Override
    public FixMessageBuilder<String> setField(int tag, String value, boolean header) {
        switch (tag) {
            case BEGIN_STRING_TAG:
                beginString = value;
                break;
            default:
                appendTag(tag, header).append(value).append(SOH);
        }
        return this;
    }

    @Override
    public FixMessageBuilder<String> setField(int tag, Enum<?> value, boolean header) {
        FixEnumMeta<? extends Enum<?>> fixEnumMeta = fixEnumDictionary.getFixEnumMeta(value.getDeclaringClass());
        return (fixEnumMeta == null) ?
                setField(tag, value.ordinal(), header) :
                setField(tag, fixEnumMeta.fixValueForEnum(value), header);
    }

    @Override
    public FixMessageBuilder<String> setGroups(int tag, int componentTag, Collection<?> collection, boolean header) {
        if (!collection.isEmpty()) {
            setField(tag, collection.size(), header);
            for (Object o : collection) {
                setField(componentTag, o, header);
            }
        }
        return this;
    }

    @Override
    public FixMessageBuilder<String> setGroups(int tag, List<FixFieldMeta> fields, Collection<?> collection, boolean header) {
        if (!collection.isEmpty()) {
            setField(tag, collection.size(), header);
            for (Object o : collection) {
                for (FixFieldMeta fieldMeta : fields) {
                    if (fieldMeta.isGroup()) {
                        final FixGroupMeta groupMeta = (FixGroupMeta) fieldMeta;
                        if (groupMeta.isSimple()) {
                            setGroups(groupMeta.getTag(), groupMeta.getComponentTag(), (Collection<?>) o, header);
                        } else {
                            setGroups(groupMeta.getTag(), groupMeta.getComponentMeta().getFields(), (Collection<?>) o, header);
                        }
                    } else {
                        setField(fieldMeta.getTag(), fieldMeta.getValue(o), header);
                    }
                }
            }
        }
        return this;
    }

    private NativeFixMessageBuilder(FixEnumDictionary fixEnumDictionary, final StringBuilder body) {
        this.fixEnumDictionary = fixEnumDictionary;
        this.body = body;
    }

    private StringBuilder appendTag(int tag, boolean header) {
        return appendTag((header ? head : body), tag);
    }

    private StringBuilder appendTag(final StringBuilder buf, int tag) {
        return buf.append(tag).append('=');
    }

    private int generateCheckSum(StringBuilder buf) {
        int sum = 0;
        for (int idx = 0; idx < buf.length(); idx++) {
            sum += buf.charAt(idx);
        }
        return sum % 256;
    }

}
