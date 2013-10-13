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
import org.fix4j.annotations.FixGroup;
import org.fix4j.annotations.FixMessage;

import java.util.List;

import static org.fix4j.test.data.TestModels.QuoteFixFields.*;

@FixMessage(type = "Q",
        header = @FixMessage.Field(tag = HDR, value = "TEST"),
        body = {@FixMessage.Field(tag = 18, value = "BODY1"),
                @FixMessage.Field(tag = 19, value = "BODY2")})
public class SampleQuote extends TestModels.BasicQuote {
    public static enum Side {
        BUY, SELL
    }

    @FixField(tag = SIDE)
    private final Side side;

    @FixField(tag = SYMBOL)
    private final String symbol;

    @FixGroup(tag = AMOUNT_GR, componentTag = AMOUNT, component = Integer.class)
    private final List amounts;

    @FixGroup(tag = PARAM_GR, component = TestModels.Params.class)
    private final List<TestModels.Params> paramsList;

    @FixBlock
    private final TestModels.Params params;

    public SampleQuote(
            @FixField(tag = QUOTE_ID) final String quoteId,
            @FixField(tag = SIDE) final Side side,
            @FixField(tag = SYMBOL) final String symbol,
            @FixField(tag = AMOUNT_GR) final List<Integer> amounts,
            @FixField(tag = PARAM_GR) final List<TestModels.Params> paramsList,
            @FixBlock final TestModels.Params params) {
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

    public TestModels.Params getParams() {
        return params;
    }

    public List<TestModels.Params> getParamsList() {
        return paramsList;
    }
}
