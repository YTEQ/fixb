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

package org.fix4j.meta;

import org.fix4j.test.data.SampleQuote;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertSame;

public class FixMetaScannerTest {
    @Test
    public void testScanClass() {
        // When
        final FixMetaRepository fixMetaRepository = new FixMetaRepositoryImpl();
        final FixBlockMeta<SampleQuote> meta = FixMetaScanner.scanClassAndAddToRepository(SampleQuote.class, fixMetaRepository);

        // Then
        assertEquals(SampleQuote.class, meta.getType());
        assertEquals(11, meta.getFields().size());
        // Msg type
        assertEquals(35, meta.getFields().get(0).getTag());
        // Constant fields
        assertEquals(33, meta.getFields().get(1).getTag());
        assertEquals(18, meta.getFields().get(2).getTag());
        assertEquals(19, meta.getFields().get(3).getTag());
        // Dynamic fields (in constructor params order)
        assertEquals(11, meta.getFields().get(4).getTag());
        assertEquals(40, meta.getFields().get(5).getTag());
        assertEquals(12, meta.getFields().get(6).getTag());
        assertEquals(13, meta.getFields().get(7).getTag());
        assertEquals(20, meta.getFields().get(8).getTag());
        assertEquals(21, meta.getFields().get(9).getTag());
        assertEquals(22, meta.getFields().get(10).getTag());
        // Meta is stored in the provided repository
        assertSame(meta, fixMetaRepository.getMetaForClass(SampleQuote.class));
    }

}
