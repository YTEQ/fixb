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

package org.fix4j.quickfix;

import org.fix4j.FixMessageBuilder;
import org.fix4j.meta.FixFieldMeta;
import org.joda.time.*;
import quickfix.Message;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * The implementation of <tt>FixMessageBuilder</tt> based on QuickFIX/J library (<tt>quickfix.Message</tt>).
 *
 * @author vladyslav.yatsenko
 */
public class QuickFixMessageBuilder extends QuickFixFieldMapBuilder<Message> {
    protected QuickFixMessageBuilder() {
        super(new Message());
    }

    protected QuickFixMessageBuilder(Message message) {
        super(message);
    }

    public static class Factory implements FixMessageBuilder.Factory<Message, FixMessageBuilder<Message>> {

        @Override
        public QuickFixMessageBuilder create() {
            return new QuickFixMessageBuilder();
        }

        @Override
        public FixMessageBuilder<Message> createWithMessage(Message fixMessage) {
            return new QuickFixMessageBuilder(fixMessage);
        }
    }

    /**
     * @return a new instance of this builder.
     */
    public static QuickFixMessageBuilder fixMessage() {
        return new QuickFixMessageBuilder();
    }

    @Override
    public QuickFixFieldMapBuilder<Message> setField(int tag, String value, boolean header) {
        return setField((header ? message.getHeader() : message), tag, value);
    }

    @Override
    public FixMessageBuilder<Message> setField(int tag, Enum<?> value, boolean header) {
        return setField((header ? message.getHeader() : message), tag, value);
    }

    @Override
    public QuickFixFieldMapBuilder<Message> setField(int tag, BigDecimal value, boolean header) {
        return setField((header ? message.getHeader() : message), tag, value);
    }

    @Override
    public QuickFixFieldMapBuilder<Message> setField(int tag, int value, boolean header) {
        return setField((header ? message.getHeader() : message), tag, value);
    }

    @Override
    public QuickFixFieldMapBuilder<Message> setField(int tag, double value, boolean header) {
        return setField((header ? message.getHeader() : message), tag, value);
    }

    @Override
    public FixMessageBuilder<Message> setField(int tag, char value, boolean header) {
        return setField((header ? message.getHeader() : message), tag, value);
    }

    @Override
    public QuickFixFieldMapBuilder<Message> setField(int tag, boolean value, boolean header) {
        return setField((header ? message.getHeader() : message), tag, value);
    }

    @Override
    public QuickFixFieldMapBuilder<Message> setField(int tag, Instant value, boolean header) {
        return setField((header ? message.getHeader() : message), tag, value);
    }

    @Override
    public FixMessageBuilder<Message> setField(int tag, Date value, boolean header) {
        return setField((header ? message.getHeader() : message), tag, value);
    }

    @Override
    public QuickFixFieldMapBuilder<Message> setField(int tag, LocalDate value, boolean header) {
        return setField((header ? message.getHeader() : message), tag, value);
    }

    @Override
    public FixMessageBuilder<Message> setField(int tag, LocalTime value, boolean header) {
        return setField((header ? message.getHeader() : message), tag, value);
    }

    @Override
    public FixMessageBuilder<Message> setField(int tag, LocalDateTime value, boolean header) {
        return setField((header ? message.getHeader() : message), tag, value);
    }

    @Override
    public FixMessageBuilder<Message> setField(int tag, DateTime value, boolean header) {
        return setField((header ? message.getHeader() : message), tag, value);
    }

    @Override
    public QuickFixFieldMapBuilder<Message> setGroups(int groupTag,
                                                      int componentTag,
                                                      Collection<?> collection,
                                                      boolean header) {
        return setGroups((header ? message.getHeader() : message), groupTag, componentTag, collection);
    }

    @Override
    public QuickFixFieldMapBuilder<Message> setGroups(int groupTag,
                                                      List<FixFieldMeta> fields,
                                                      Collection<?> collection,
                                                      boolean header) {
        return setGroups((header ? message.getHeader() : message), groupTag, fields, collection);
    }
}
