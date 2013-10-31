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

package org.fixb;

import org.fixb.adapter.CommonFixAdapter;
import org.fixb.meta.FixMetaDictionary;
import org.fixb.meta.FixMetaScanner;
import org.fixb.quickfix.FixMetaDataDictionary;
import org.fixb.quickfix.QuickFixFieldExtractor;
import org.fixb.quickfix.QuickFixMessageBuilder;
import org.junit.Test;
import quickfix.Message;

import static junit.framework.Assert.assertEquals;
import static org.fixb.FixConstants.MSG_TYPE_TAG;
import static org.fixb.quickfix.QuickFixMessageBuilder.fixMessage;
import static org.fixb.quickfix.test.data.TestModels.*;

public class CommonFixAdapterWithQuickFixTest {
    private final FixMetaDictionary fixMetaDictionary = FixMetaScanner.scanClassesIn("org.fixb.quickfix.test.data");
    private final FixAdapter<Object, Message> adapter = new CommonFixAdapter<>("FIX.5.0",
            new QuickFixFieldExtractor(),
            new QuickFixMessageBuilder.Factory(),
            fixMetaDictionary);

    @Test
    public void testToFix() throws Exception {
        // Given
        final Message1 msg1 = new Message1("TYPE", new Component("TEST1"));
        final Message2 msg2 = new Message2("TYPE", new Component("TEST2"));

        // When
        final Message fixMessage1 = adapter.toFix(msg1);
        final Message fixMessage2 = adapter.toFix(msg2);

        // Then
        assertEquals("M1", fixMessage1.getHeader().getString(MSG_TYPE_TAG));
        assertEquals("TEST1", fixMessage1.getString(100));
        assertEquals("M2", fixMessage2.getHeader().getString(MSG_TYPE_TAG));
        assertEquals("TEST2", fixMessage2.getString(100));

        fixMessage1.fromString(fixMessage1.toString(), new FixMetaDataDictionary("FIX.5.0", fixMetaDictionary), true);
    }

    @Test
    public void testFromFix() {
        // Given
        final Message msg1 = fixMessage()
                .setField(MSG_TYPE_TAG, "M1", true)
                .setField(568, "TYPE")
                .setField(100, "TEST1").build();

        final Message msg2 = fixMessage()
                .setField(MSG_TYPE_TAG, "M2", true)
                .setField(567, "TYPE")
                .setField(100, "TEST2").build();

        // When
        final Message1 obj1 = (Message1) adapter.fromFix(msg1);
        final Message2 obj2 = (Message2) adapter.fromFix(msg2);

        // Then
        assertEquals("TEST1", obj1.component.value);
        assertEquals("TEST2", obj2.component.value);
    }
}
