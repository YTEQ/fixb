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
import org.fixb.FixSerializer;
import org.fixb.meta.FixMetaDictionary;
import quickfix.ConfigError;
import quickfix.DataDictionary;
import quickfix.InvalidMessage;
import quickfix.Message;

/**
 * The implementation of <tt>FixSerializer</tt> based on <tt>quickfix.Message</tt> from QuickFIX/J library.
 *
 * @author vladyslav.yatsenko
 */
public class QuickFixSerializer implements FixSerializer<Message> {
    private final DataDictionary dataDictionary;

    public QuickFixSerializer(final String fixVersion, FixMetaDictionary fixMetaRepository) {
        this.dataDictionary = initDataDictionary(fixVersion, fixMetaRepository);
    }

    @Override
    public String serialize(Message message) {
        return message.toString();
    }

    private DataDictionary initDataDictionary(final String fixVersion, FixMetaDictionary fixMetaRepository) {
        try {
            return new FixMetaDataDictionary(fixVersion, fixMetaRepository);
        } catch (ConfigError e) {
            throw new RuntimeException("Error loading FIX data dictionary: " + e.getMessage(), e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Message deserialize(String fixMessage) {
        final Message message = new Message();
        try {
            message.fromString(fixMessage, dataDictionary, true);
        } catch (InvalidMessage e) {
            throw new FixException(e.getMessage(), e);
        }

        return message;
    }
}
