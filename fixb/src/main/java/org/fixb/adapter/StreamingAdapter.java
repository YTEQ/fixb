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

import org.fixb.FixAdapter;
import org.fixb.impl.FieldCursor;
import org.fixb.meta.FixFieldMeta;
import org.fixb.meta.FixMessageMeta;
import org.fixb.meta.FixMetaRepository;

import java.util.HashMap;
import java.util.Map;

import static org.fixb.FixConstants.MSG_TYPE_TAG;

/**
 * I am a stream adapter optimized for deserialization of raw FIX messages. Serialisation is not supported.
 *
 * @author vladyslav.yatsenko
 */
public class StreamingAdapter implements FixAdapter<Object, String> {

    private final FixMetaRepository fixMetaRepository;

    /**
     * @param fixMetaRepository a FIX mapping meta repository
     */
    public StreamingAdapter(FixMetaRepository fixMetaRepository) {
        this.fixMetaRepository = fixMetaRepository;
    }

    @Override
    public Object fromFix(String fixMessage) {
        FieldCursor cursor = FieldCursor.create(fixMessage);

        FixMessageMeta<?> meta = null;
        Map<FixFieldMeta, Object> params = new HashMap<FixFieldMeta, Object>();

        while (cursor.nextField()) {
            int tag = cursor.lastTag();
            if (tag == MSG_TYPE_TAG) {
                meta = fixMetaRepository.getMetaForMessageType(cursor.lastValue());
            }
        }

        if (meta == null) {
            throw new RuntimeException();
        }

        return meta.createModel(params);
    }

    @Override
    public String toFix(Object data) {
        throw new UnsupportedOperationException("Streaming adapter can only read fix messages");
    }
}
