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

package org.fix4j.quickfix.test.data;

import org.fix4j.annotations.FixBlock;
import org.fix4j.annotations.FixField;
import org.fix4j.annotations.FixGroup;
import org.fix4j.annotations.FixMessage;

import java.util.List;

import static org.fix4j.quickfix.test.data.TestModels.QuoteFixFields.*;

public final class TestModels {
    @FixMessage(type = "M1")
    public static class Message1 {
        @FixField(tag = 568)
        public final String type;
        @FixBlock
        public final Component component;

        public Message1(@FixField(tag = 568) final String type, @FixBlock final Component component) {
            this.type = type;
            this.component = component;
        }
    }

    @FixMessage(type = "M2")
    public static class Message2 {
        @FixField(tag = 567)
        public final String type;
        @FixBlock
        public final Component component;

        public Message2(@FixField(tag = 567) final String type, @FixBlock final Component component) {
            this.type = type;
            this.component = component;
        }
    }

    @FixBlock
    public static class Component {
        @FixField(tag = 100)
        public final String value;

        public Component(@FixField(tag = 100) final String value) {
            this.value = value;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final Component component = (Component) o;

            if (value != null ? !value.equals(component.value) : component.value != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }
    }

    //////////////////////
    // Quote

    public static final class QuoteFixFields {
        public static final int QUOTE_ID = 11;
        public static final int SYMBOL = 12;
        public static final int AMOUNT_GR = 13;
        public static final int AMOUNT = 14;
        public static final int PARAM_GR = 20;
        public static final int P1 = 21;
        public static final int P2 = 22;
        public static final int HDR = 33;
        public static final int SIDE = 40;
    }

    public abstract static class BasicQuote {
        @FixField(tag = QUOTE_ID)
        private final String quoteId;

        public BasicQuote(@FixField(tag = QUOTE_ID) final String quoteId) {
            this.quoteId = quoteId;
        }

        public String getQuoteId() {
            return quoteId;
        }
    }

    @FixBlock
    public static class Params {
        @FixField(tag = P1)
        private final String param1;
        @FixField(tag = P2)
        private final String param2;

        public Params(@FixField(tag = P1) final String param1,
                      @FixField(tag = P2) final String param2) {
            this.param1 = param1;
            this.param2 = param2;
        }

        public String getParam1() {
            return param1;
        }

        public String getParam2() {
            return param2;
        }
    }

    @FixMessage(type = "Q",
            header = @FixMessage.Field(tag = HDR, value = "TEST"),
            body = {@FixMessage.Field(tag = 18, value = "BODY1"),
                    @FixMessage.Field(tag = 19, value = "BODY2")})
    public static class SampleQuote extends BasicQuote {


        public static enum Side {
            BUY, SELL
        }

        @FixField(tag = SIDE)
        private final Side side;

        @FixField(tag = SYMBOL)
        private final String symbol;

        @FixGroup(tag = AMOUNT_GR, componentTag = AMOUNT, component = Integer.class)
        private final List amounts;

        @FixGroup(tag = PARAM_GR, component = Params.class)
        private final List<Params> paramsList;

        @FixBlock
        private final Params params;

        public SampleQuote(
                @FixField(tag = QUOTE_ID) final String quoteId,
                @FixField(tag = SIDE) final Side side,
                @FixField(tag = SYMBOL) final String symbol,
                @FixField(tag = AMOUNT_GR) final List<Integer> amounts,
                @FixField(tag = PARAM_GR) final List<Params> paramsList,
                @FixBlock final Params params) {
            super(quoteId);
            this.side = side;
            this.symbol = symbol;
            this.amounts = amounts;
            this.paramsList = paramsList;
            this.params = params;
        }

        public String getSymbol() {
            return symbol;
        }

        public Side getSide() {
            return side;
        }

        public List getAmounts() {
            return amounts;
        }

        public Params getParams() {
            return params;
        }

        public List<Params> getParamsList() {
            return paramsList;
        }
    }

    @FixMessage(type = "XXX")
    public static class SymbolList {
        @FixGroup(tag = 9000, componentTag = SYMBOL, optional = true)
        private final List<String> symbols;

        public SymbolList(@FixField(tag = 9000) final List<String> symbols) {
            this.symbols = symbols;
        }
    }

}
