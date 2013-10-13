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

package org.fix4j.adapter;

import org.fix4j.*;
import org.fix4j.meta.*;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * I am a base implementation of the FixAdapter interface. I leverage mapping of domain objects
 * to FIX. The process of retrieving the mapping configuration, FixMessageMeta, is defined in subclasses.
 *
 * @author vladyslav.yatsenko
 * @see org.fix4j.FixAdapter
 * @see org.fix4j.meta.FixMetaRepositoryImpl
 */
public abstract class AbstractMetaFixAdapter<T, M> implements FixAdapter<T, M> {
    private final String protocolVersion;
    private final FixFieldExtractor<M> fixFieldExtractor;
    private final FixMessageBuilder.Factory<M, ? extends FixMessageBuilder<M>> fixMessageBuilder;

    /**
     * Minimum constructor.
     *
     * @param protocolVersion   a FIX protocol version to use in the generated messages, e.g. "FIX.5.0"
     * @param fixFieldExtractor a specific FixFieldExtractor used to extract single FIX fields
     *                          and groups from FIX messages represented by type <tt>M</tt>
     * @param fixMessageBuilder a specific FixMessageBuilder used to build FIX objects of type
     *                          <tt>M</tt> from single FIX fields and groups
     */
    public AbstractMetaFixAdapter(String protocolVersion,
                                  FixFieldExtractor<M> fixFieldExtractor,
                                  FixMessageBuilder.Factory<M, ? extends FixMessageBuilder<M>> fixMessageBuilder) {
        this.protocolVersion = protocolVersion;
        this.fixFieldExtractor = fixFieldExtractor;
        this.fixMessageBuilder = fixMessageBuilder;
    }

    /**
     * @param fixMessageType the FIX message type
     * @return FixMessageMeta for the given FIX message type (FIX tag 35).
     */
    protected abstract FixMessageMeta<T> getFixMetaForMessageType(String fixMessageType);

    /**
     * @param type the domain object type with appropriate FIX mappings defined
     * @return FixMessageMeta for the given domain object type.
     */
    protected abstract FixMessageMeta<T> getFixMetaForClass(Class<T> type);

    @Override
    @SuppressWarnings("unchecked")
    public T fromFix(M fixMessage) {
        final Map<FixFieldMeta, Object> values = new LinkedHashMap<FixFieldMeta, Object>();
        final String msgType = fixFieldExtractor.getFieldValue(fixMessage,
                String.class,
                FixConstants.MSG_TYPE_TAG,
                true);
        final FixMessageMeta<T> fixMeta = getFixMetaForMessageType(msgType);

        for (FixFieldMeta fieldMeta : fixMeta.getFields()) {
            if (!(fieldMeta instanceof FixConstantFieldMeta)) {
                final Object fieldValue;
                if (fieldMeta.isGroup()) {
                    FixGroupMeta groupMeta = (FixGroupMeta) fieldMeta;
                    if (groupMeta.isSimple()) {
                        fieldValue = fixFieldExtractor.getGroups(fixMessage,
                                (Class<Collection<Object>>) groupMeta.getType(),
                                groupMeta.getTag(),
                                (Class<Object>) groupMeta.getComponentType(),
                                groupMeta.getComponentTag(),
                                groupMeta.isOptional());
                    } else {
                        fieldValue = fixFieldExtractor.getGroups(fixMessage,
                                (Class<Collection<Object>>) groupMeta.getType(),
                                groupMeta.getTag(),
                                (FixBlockMeta<Object>) groupMeta.getComponentMeta(),
                                groupMeta.isOptional());
                    }
                } else {
                    fieldValue = fixFieldExtractor.getFieldValue(fixMessage,
                            fieldMeta.getType(),
                            fieldMeta.getTag(),
                            fieldMeta.isOptional());
                }
                values.put(fieldMeta, fieldValue);
            }
        }

        return fixMeta.createModel(values);
    }

    @Override
    public M toFix(T data) {
        final FixMessageMeta<?> fixMeta = getFixMetaForClass((Class<T>) data.getClass());

        final FixMessageBuilder<M> builder = fixMessageBuilder.create()
                .setField(FixConstants.BEGIN_STRING_TAG, protocolVersion, true);

        for (FixFieldMeta fieldMeta : fixMeta.getFields()) {
            if (fieldMeta.isGroup()) {
                FixGroupMeta groupMeta = (FixGroupMeta) fieldMeta;
                Collection<?> value = groupMeta.getValue(data);
                if (!groupMeta.isOptional() && (value == null || value.isEmpty())) {
                    throw new FixException("Non-empty tag is required for FIX group " + groupMeta.getTag());
                }
                if (groupMeta.isSimple()) {
                    builder.setGroups(groupMeta.getTag(), groupMeta.getComponentTag(), value, groupMeta.isHeader());
                } else {
                    builder.setGroups(groupMeta.getTag(),
                            groupMeta.getComponentMeta().getFields(),
                            value,
                            groupMeta.isHeader());
                }
            } else {
                Object value = fieldMeta.getValue(data);
                if (!fieldMeta.isOptional() && value == null) {
                    throw new FixException("Non-empty tag is required for FIX field " + fieldMeta.getTag());
                }
                builder.setField(fieldMeta.getTag(), value, fieldMeta.isHeader());
            }
        }

        return builder.build();
    }
}
