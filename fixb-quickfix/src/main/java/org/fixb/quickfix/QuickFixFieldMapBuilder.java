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

import org.fixb.FixMessageBuilder;
import org.fixb.meta.FixFieldMeta;
import org.fixb.meta.FixGroupMeta;
import org.joda.time.*;
import quickfix.FieldMap;
import quickfix.Group;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @param <M> a QuickFix message type, implementation of quickfix.FieldMap.
 * @author vladyslav.yatsenko
 */
public abstract class QuickFixFieldMapBuilder<M extends FieldMap> extends FixMessageBuilder<M> {
    protected final M message;

    protected QuickFixFieldMapBuilder(M message) {
        this.message = message;
    }

    public static QuickFixFieldMapBuilder<Group> fixGroup(int tag, int componentTag) {
        return new QuickFixGroupBuilder(new Group(tag, componentTag));
    }

    protected static class QuickFixGroupBuilder extends QuickFixFieldMapBuilder<Group> {

        protected QuickFixGroupBuilder(Group message) {
            super(message);
        }

        @Override
        public QuickFixFieldMapBuilder<Group> setField(int tag, String value, boolean header) {
            return setField(message, tag, value);
        }

        @Override
        public FixMessageBuilder<Group> setField(int tag, Enum<?> value, boolean header) {
            return setField(message, tag, value);
        }

        @Override
        public QuickFixFieldMapBuilder<Group> setField(int tag, BigDecimal value, boolean header) {
            return setField(message, tag, value);
        }

        @Override
        public QuickFixFieldMapBuilder<Group> setField(int tag, int value, boolean header) {
            return setField(message, tag, value);
        }

        @Override
        public QuickFixFieldMapBuilder<Group> setField(int tag, double value, boolean header) {
            return setField(message, tag, value);
        }

        @Override
        public FixMessageBuilder<Group> setField(int tag, char value, boolean header) {
            return setField(message, tag, value);
        }

        @Override
        public QuickFixFieldMapBuilder<Group> setField(int tag, boolean value, boolean header) {
            return setField(message, tag, value);
        }

        @Override
        public QuickFixFieldMapBuilder<Group> setField(int tag, Instant value, boolean header) {
            return setField(message, tag, value);
        }

        @Override
        public FixMessageBuilder<Group> setField(int tag, Date value, boolean header) {
            return setField(message, tag, value);
        }

        @Override
        public QuickFixFieldMapBuilder<Group> setField(int tag, LocalDate value, boolean header) {
            return setField(message, tag, value);
        }

        @Override
        public FixMessageBuilder<Group> setField(int tag, LocalTime value, boolean header) {
            return setField(message, tag, value);
        }

        @Override
        public FixMessageBuilder<Group> setField(int tag, LocalDateTime value, boolean header) {
            return setField(message, tag, value);
        }

        @Override
        public FixMessageBuilder<Group> setField(int tag, DateTime value, boolean header) {
            return setField(message, tag, value);
        }

        @Override
        public QuickFixFieldMapBuilder<Group> setGroups(int groupTag,
                                                        int componentTag,
                                                        Collection<?> collection,
                                                        boolean header) {
            return setGroups(message, groupTag, componentTag, collection);
        }

        @Override
        public QuickFixFieldMapBuilder<Group> setGroups(int groupTag,
                                                        List<FixFieldMeta> fields,
                                                        Collection<?> collection,
                                                        boolean header) {
            return setGroups(message, groupTag, fields, collection);
        }
    }

    protected QuickFixFieldMapBuilder<M> setField(FieldMap message, int tag, String value) {
        message.setString(tag, value);
        return this;
    }

    protected QuickFixFieldMapBuilder<M> setField(FieldMap message, int tag, Enum<?> value) {
        message.setInt(tag, value.ordinal() + 1);
        return this;
    }

    protected QuickFixFieldMapBuilder<M> setField(FieldMap message, int tag, BigDecimal value) {
        message.setDecimal(tag, value);
        return this;
    }

    protected QuickFixFieldMapBuilder<M> setField(FieldMap message, int tag, int value) {
        message.setInt(tag, value);
        return this;
    }

    protected QuickFixFieldMapBuilder<M> setField(FieldMap message, int tag, char value) {
        message.setChar(tag, value);
        return this;
    }

    protected QuickFixFieldMapBuilder<M> setField(FieldMap message, int tag, double value) {
        message.setDouble(tag, value);
        return this;
    }

    protected QuickFixFieldMapBuilder<M> setField(FieldMap message, int tag, boolean value) {
        message.setBoolean(tag, value);
        return this;
    }

    protected QuickFixFieldMapBuilder<M> setField(FieldMap message, int tag, Instant value) {
        message.setUtcTimeStamp(tag, value.toDate());
        return this;
    }

    protected QuickFixFieldMapBuilder<M> setField(FieldMap message, int tag, LocalDate value) {
        message.setString(tag, value.toString("yyyMMdd"));
        return this;
    }

    protected FixMessageBuilder<M> setField(final FieldMap message, final int tag, final LocalTime value) {
        message.setUtcTimeOnly(tag, value.toDateTimeToday(DateTimeZone.UTC).toDate());
        return this;
    }

    protected FixMessageBuilder<M> setField(FieldMap message, int tag, LocalDateTime value) {
        message.setUtcTimeStamp(tag, value.toDateTime(DateTimeZone.UTC).toDate());
        return this;
    }

    protected FixMessageBuilder<M> setField(FieldMap message, int tag, DateTime value) {
        message.setUtcTimeStamp(tag, value.toDate());
        return this;
    }

    protected FixMessageBuilder<M> setField(FieldMap message, int tag, Date value) {
        message.setUtcTimeStamp(tag, value);
        return this;
    }

    protected QuickFixFieldMapBuilder<M> setGroups(FieldMap message,
                                                   int groupTag,
                                                   int componentTag,
                                                   Collection<?> collection) {
        for (Object value : collection) {
            Group group = fixGroup(groupTag, componentTag).setField(componentTag, value).build();
            message.addGroup(group);
        }
        return this;
    }

    protected QuickFixFieldMapBuilder<M> setGroups(FieldMap message,
                                                   int groupTag,
                                                   List<FixFieldMeta> fields,
                                                   Collection<?> collection) {
        int delimiterTag = fields.get(0).getTag();
        for (Object item : collection) {
            QuickFixFieldMapBuilder<Group> groupBuilder = fixGroup(groupTag, delimiterTag);
            for (FixFieldMeta f : fields) {
                if (f.isGroup()) {
                    FixGroupMeta groupMeta = (FixGroupMeta) f;
                    if (groupMeta.isSimple()) {
                        groupBuilder.setGroups(groupMeta.getTag(),
                                groupMeta.getComponentTag(),
                                groupMeta.getValue(item),
                                groupMeta.isHeader());
                    } else {
                        groupBuilder.setGroups(groupMeta.getTag(),
                                groupMeta.getComponentMeta().getFields(),
                                groupMeta.getValue(item),
                                groupMeta.isHeader());
                    }
                } else {
                    groupBuilder.setField(f.getTag(), f.getValue(item));
                }
            }
            message.addGroup(groupBuilder.build());
        }
        return this;
    }

    @Override
    public M build() {
        return message;
    }
}
