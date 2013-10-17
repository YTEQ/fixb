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

package org.fixb.adapter;

import org.fixb.FixException;
import org.fixb.FixFieldExtractor;
import org.fixb.FixMessageBuilder;
import org.fixb.meta.FixMessageMeta;

/**
 * I am an implementation of AbstractMetaFixAdapter that can serve only one
 * type of message or domain objects. A separate instance of GenericFixAdapter is required for each
 * domain object type that needs to be translated to/from a FIX message object of the type defined by <code>M</code>.
 *
 * @param <T> the type of Java objects to translate to/from FIX message objects.
 * @param <M> the type that represents FIX message objects.
 * @author vladyslav.yatsenko
 * @see AbstractMetaFixAdapter
 */
public class GenericFixAdapter<T, M> extends AbstractMetaFixAdapter<T, M> {
    private final FixMessageMeta<T> fixMeta;

    /**
     * @param protocolVersion   a FIX protocol version to use in the generated messages, e.g. "FIX.5.0"
     * @param fixFieldExtractor a specific FixFieldExtractor used to extract single FIX fields
     *                          and groups from FIX messages represented by type <tt>M</tt>
     * @param fixMessageBuilder a specific FixMessageBuilder used to build FIX objects of type
     * @param fixMessageMeta    a FIX mapping configuration for the type T
     */
    public GenericFixAdapter(String protocolVersion,
                             FixFieldExtractor<M> fixFieldExtractor, FixMessageBuilder.Factory<M, ? extends FixMessageBuilder<M>> fixMessageBuilder, FixMessageMeta<T> fixMessageMeta) {
        super(protocolVersion, fixFieldExtractor, fixMessageBuilder);
        this.fixMeta = fixMessageMeta;
    }

    @Override
    protected FixMessageMeta<T> getFixMetaForMessageType(final String fixMessageType) {
        if (!fixMeta.getMessageType().equals(fixMessageType)) {
            throw new FixException("Expected message type: " + fixMeta.getMessageType() + ", received: " + fixMessageType);
        }
        return fixMeta;
    }

    @Override
    protected FixMessageMeta<T> getFixMetaForClass(final Class<T> type) {
        if (type != fixMeta.getType()) {
            throw new FixException("Expected object type: " + fixMeta.getType() + ", " + "received: " + type);
        }
        return fixMeta;
    }
}
