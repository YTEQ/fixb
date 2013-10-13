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

package org.fix4j.test.data;

import org.fix4j.annotations.FixBlock;
import org.fix4j.annotations.FixField;
import org.fix4j.annotations.FixMessage;

import static org.fix4j.test.data.TestModels.QuoteFixFields.*;

public final class TestModels {
    @FixMessage(type = "M1")
    public static class Message1 {
        @FixBlock
        public final Component component;

        public Message1(@FixBlock final Component component) {
            this.component = component;
        }
    }

    @FixMessage(type = "M2")
    public static class Message2 {
        @FixBlock
        public final Component component;

        public Message2(@FixBlock final Component component) {
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

}
