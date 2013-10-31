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

import org.fixb.FixAdapter;
import org.fixb.FixException;
import org.fixb.FixSerializer;
import org.fixb.adapter.CommonFixAdapter;
import org.fixb.meta.FixMessageMeta;
import org.fixb.meta.FixMetaDictionary;

import static org.fixb.FixConstants.MSG_TYPE_TAG;

/**
 * An implementation of FixSerializer that is used to serialize/deserialize instances of FIX annotated classes to/from
 * raw FIX messages.
 *
 * @author vladyslav.yatsenko
 */
public class NativeFixSerializer<T> implements FixSerializer<T> {
    private final FixAdapter<Object, String> fixAdapter;
    private final NativeFixFieldExtractor extractor;
    private final FixMetaDictionary fixMetaDictionary;

    /**
     * @param protocolVersion   a FIX protocol version (used to build a header of the resulting FIX messages)
     * @param fixMetaDictionary a FIX bindings meta dictionary
     */
    public NativeFixSerializer(String protocolVersion, FixMetaDictionary fixMetaDictionary) {
        this.fixMetaDictionary = fixMetaDictionary;
        this.extractor = new NativeFixFieldExtractor(fixMetaDictionary);
        this.fixAdapter = new CommonFixAdapter<>(
                protocolVersion,
                extractor,
                new NativeFixMessageBuilder.Factory(fixMetaDictionary),
                fixMetaDictionary);
    }

    @Override
    public String serialize(T message) {
        return fixAdapter.toFix(message);
    }

    @Override
    public T deserialize(String fixMessage) {
        FieldCursor cursor = FieldCursor.create(fixMessage);
        if (!cursor.nextField(MSG_TYPE_TAG)) {
            throw new FixException("Invalid FIX message, MsgType(35) is missing in: " + fixMessage);
        }


        FixMessageMeta<Object> meta = fixMetaDictionary.getMetaForMessageType(cursor.lastValue());
        return (T) extractor.extractFixBlock(cursor, meta);
    }
}
