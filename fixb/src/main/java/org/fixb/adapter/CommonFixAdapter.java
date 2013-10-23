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

import org.fixb.FixFieldExtractor;
import org.fixb.FixMessageBuilder;
import org.fixb.meta.FixMessageMeta;
import org.fixb.meta.FixMetaDictionary;

/**
 * I am an implementation of AbstractMetaFixAdapter which works with domain objects of any
 * type registered with the provided FixMetaDictionary.
 *
 * @author vladyslav.yatsenko
 * @see AbstractMetaFixAdapter
 */
public class CommonFixAdapter<M> extends AbstractMetaFixAdapter<Object, M> {
    private final FixMetaDictionary fixMetaRepository;

    /**
     * @param protocolVersion   a FIX protocol version to use in the generated messages, e.g. "FIX.5.0"
     * @param fixFieldExtractor a specific FixFieldExtractor used to extract single FIX fields
     *                          and groups from FIX messages represented by type <tt>M</tt>
     * @param fixMessageBuilder a specific FixMessageBuilder used to build FIX objects of type
     *                          <tt>M</tt> from single FIX fields and groups
     * @param fixMetaRepository a FIX mapping meta repository
     */
    public CommonFixAdapter(String protocolVersion,
                            FixFieldExtractor<M> fixFieldExtractor,
                            FixMessageBuilder.Factory<M, ? extends FixMessageBuilder<M>> fixMessageBuilder,
                            FixMetaDictionary fixMetaRepository) {
        super(protocolVersion, fixFieldExtractor, fixMessageBuilder);
        this.fixMetaRepository = fixMetaRepository;
    }

    @Override
    protected FixMessageMeta<Object> getFixMetaForMessageType(final String fixMessageType) {
        return fixMetaRepository.getMetaForMessageType(fixMessageType);
    }

    @Override
    protected FixMessageMeta<Object> getFixMetaForClass(final Class<Object> type) {
        return fixMetaRepository.getMetaForClass(type);
    }
}
