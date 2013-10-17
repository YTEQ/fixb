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

import org.fixb.FixConstants;
import org.fixb.FixSerializer;
import org.fixb.meta.FixMetaRepositoryImpl;
import org.fixb.meta.FixMetaScanner;
import org.fixb.quickfix.test.data.TestModels;
import org.junit.Test;
import quickfix.ConfigError;
import quickfix.FieldNotFound;
import quickfix.InvalidMessage;
import quickfix.Message;

import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static org.fixb.FixConstants.BEGIN_STRING_TAG;
import static org.fixb.FixConstants.MSG_TYPE_TAG;
import static org.fixb.quickfix.QuickFixMessageBuilder.fixMessage;
import static org.fixb.quickfix.test.data.TestModels.QuoteFixFields;

public class QuickFixSerializerTest {

    private static final FixMetaRepositoryImpl fixMetaRepository = new FixMetaRepositoryImpl("org.fixb");

    private final FixSerializer<Message> serializer = new QuickFixSerializer("FIX.5.0", fixMetaRepository);

    @Test
    public void testSerialize() {
        // Given
        final quickfix.Message message = fixMessage()
                .setField(BEGIN_STRING_TAG, "FIX.4.4", true)
                .setField(FixConstants.MSG_TYPE_TAG, "TEST", true)
                .setField(100, "Some Value").build();

        // When / Then
        assertEquals("8=FIX.4.4\u00019=23\u000135=TEST\u0001100=Some Value\u000110=099\u0001",
                serializer.serialize(message));
    }

    @Test
    public void testDeserialize() throws FieldNotFound {
        // Given
        final String arbitraryFixMessage = "8=FIX.4.4\u00019=23\u000135=TEST\u0001100=Some Value\u000110=099\u0001";

        // When / Then
        final Message message = serializer.deserialize(arbitraryFixMessage);
        assertEquals("TEST", message.getHeader().getString(35));
        assertEquals("Some Value", message.getString(100));
    }

    @Test
    public void testRepeatingGroups() throws ConfigError, InvalidMessage {
        // Given
        quickfix.Message fixMessage = QuickFixMessageBuilder.fixMessage()
                .setField(BEGIN_STRING_TAG, "FIX.5.0", true)
                .setField(MSG_TYPE_TAG, "Q", true)
                .setField(QuoteFixFields.QUOTE_ID, "ID-123123")
                .setField(QuoteFixFields.SYMBOL, "EUR/USD")
                .setField(QuoteFixFields.SIDE, 1)
                .setGroups(QuoteFixFields.AMOUNT_GR, QuoteFixFields.AMOUNT, asList(500, 678, 1))
                .setGroups(QuoteFixFields.PARAM_GR,
                        FixMetaScanner.scanClass(TestModels.Params.class).getFields(),
                        asList(new TestModels.Params("P01", "P02"),
                                new TestModels.Params("P11", "P12"))).build();

        String fixString = fixMessage.toString();

        // When
        quickfix.Message tmpMessage = new Message();
        tmpMessage.fromString(fixString, new FixMetaDataDictionary("FIX.5.0", fixMetaRepository), true);

        // Then
        assertEquals(fixMessage.toString(), tmpMessage.toString());
    }
}
