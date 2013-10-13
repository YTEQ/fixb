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

package org.fix4j;

import org.fix4j.adapter.GenericFixAdapter;
import org.fix4j.annotations.FixBlock;
import org.fix4j.annotations.FixField;
import org.fix4j.meta.FixMessageMeta;
import org.fix4j.meta.FixMetaScanner;
import org.fix4j.quickfix.QuickFixFieldExtractor;
import org.fix4j.quickfix.QuickFixMessageBuilder;
import org.junit.Test;
import quickfix.FieldNotFound;
import quickfix.InvalidMessage;
import quickfix.Message;
import quickfix.field.MsgType;

import java.util.List;

import static java.util.Arrays.asList;
import static org.fix4j.FixConstants.MSG_TYPE_TAG;
import static org.fix4j.quickfix.QuickFixMessageBuilder.Factory;
import static org.fix4j.quickfix.test.data.TestModels.Params;
import static org.fix4j.quickfix.test.data.TestModels.QuoteFixFields.*;
import static org.fix4j.quickfix.test.data.TestModels.SampleQuote;
import static org.fix4j.quickfix.test.data.TestModels.SampleQuote.Side;
import static org.junit.Assert.assertEquals;

public class GenericFixAdapterWithQuickFixTest {
    private final FixFieldExtractor<Message> fixFieldExtractor = new QuickFixFieldExtractor();
    private final Factory fixMessageBuilder = new QuickFixMessageBuilder.Factory();
    private final FixAdapter<SampleQuote, Message> adapter = new GenericFixAdapter<SampleQuote, Message>("4.3",
            fixFieldExtractor, fixMessageBuilder, (FixMessageMeta<SampleQuote>) FixMetaScanner.scanClass(SampleQuote.class)
    );

    @Test
    public void testFromFix() {
        // Given
        quickfix.Message fixMessage = QuickFixMessageBuilder.fixMessage()
                .setField(MSG_TYPE_TAG, "Q", true)
                .setField(QUOTE_ID, "ID-123123")
                .setField(SYMBOL, "EUR/USD")
                .setField(P1, "Param1")
                .setField(P2, "Param2")
                .setField(SIDE, 2)
                .setGroups(AMOUNT_GR, AMOUNT, asList(500, 678, 1))
                .setGroups(PARAM_GR,
                        FixMetaScanner.scanClass(Params.class).getFields(),
                        asList(new Params("P01", "P02"),
                                new Params("P11", "P12")))
                .build();

        // When
        SampleQuote sampleQuote = adapter.fromFix(fixMessage);

        // Then
        assertEquals("ID-123123", sampleQuote.getQuoteId());
        assertEquals("EUR/USD", sampleQuote.getSymbol());
        assertEquals("Param1", sampleQuote.getParams().getParam1());
        assertEquals("Param2", sampleQuote.getParams().getParam2());
        assertEquals(SampleQuote.Side.SELL, sampleQuote.getSide());
        assertEquals(asList(500, 678, 1), sampleQuote.getAmounts());
        assertEquals("P01", sampleQuote.getParamsList().get(0).getParam1());
        assertEquals("P02", sampleQuote.getParamsList().get(0).getParam2());
        assertEquals("P11", sampleQuote.getParamsList().get(1).getParam1());
        assertEquals("P12", sampleQuote.getParamsList().get(1).getParam2());
    }

    @Test(expected = FixException.class)
    public void testFromFixWithInvalidMessageType() {
        // Given
        quickfix.Message fixMessage = QuickFixMessageBuilder.fixMessage()
                .setField(MSG_TYPE_TAG, "NOT_Q").build();

        // When
        adapter.fromFix(fixMessage);

        // Then
        // expect FixException
    }

    @Test
    public void testToFix() throws FieldNotFound, InvalidMessage {
        // Given
        SampleQuote sampleQuote = new SampleQuote("ID-123123", Side.BUY, "EUR/USD",
                asList(500, 200),
                asList(new Params("P01", "P02"), new Params("P11", "P12")),
                new Params("P1", "P2"));

        // When
        quickfix.Message fixMessage = adapter.toFix(sampleQuote);

        // Then
        assertEquals("Q", fixMessage.getHeader().getString(MsgType.FIELD));
        assertEquals("TEST", fixMessage.getHeader().getString(HDR));
        assertEquals("ID-123123", fixMessage.getString(QUOTE_ID));
        assertEquals("EUR/USD", fixMessage.getString(SYMBOL));
        assertEquals("BODY1", fixMessage.getString(18));
        assertEquals("BODY2", fixMessage.getString(19));

        assertEquals(2, fixMessage.getGroupCount(AMOUNT_GR));
        assertEquals(500, fixMessage.getGroup(1, AMOUNT_GR).getInt(AMOUNT));
        assertEquals(200, fixMessage.getGroup(2, AMOUNT_GR).getInt(AMOUNT));
        assertEquals("P1", fixMessage.getString(P1));
        assertEquals("P2", fixMessage.getString(P2));

        assertEquals(2, fixMessage.getGroupCount(PARAM_GR));
        assertEquals("P01", fixMessage.getGroup(1, PARAM_GR).getString(P1));
        assertEquals("P02", fixMessage.getGroup(1, PARAM_GR).getString(P2));
        assertEquals("P11", fixMessage.getGroup(2, PARAM_GR).getString(P1));
        assertEquals("P12", fixMessage.getGroup(2, PARAM_GR).getString(P2));
    }

    @Test(expected = FixException.class)
    public void testToFixWithInvalidObjectType() {
        // Given
        class Another extends SampleQuote {
            public Another(@FixField(tag = QUOTE_ID) final String quoteId,
                           @FixField(tag = SIDE) final Side side,
                           @FixField(tag = SYMBOL) final String symbol,
                           @FixField(tag = AMOUNT_GR) final List<Integer> amounts,
                           @FixField(tag = PARAM_GR) final List<Params> paramsList,
                           @FixBlock final Params params) {
                super(quoteId, side, symbol, amounts, paramsList, params);
            }
        }

        // When
        adapter.toFix(new Another(null, null, null, null, null, null));

        // Then
        // expect FixException
    }
}

