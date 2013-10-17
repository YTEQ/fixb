FixB (FIX Bindings)
=====

FixB is a library for Java/FIX bindings that simplifies serialization/deserialization of Java objects to/from FIX protocol messages. The bindings are defined using Java annotations.

Example (tags are fictitious):
```java
    @FixMessage(type = "Q")
    public class FxQuote extends BaseQuote {

        public static enum Side { BUY, SELL }

        @FixField(tag = 40)
        private final Side side;

        @FixField(tag = 12)
        private final String symbol;

        @FixGroup(tag = 13, componentTag = 14, component = Integer.class)
        private final List amounts;

        @FixGroup(tag = 20, component = Params.class)
        private final List<Params> paramsList;

        @FixBlock
        private final Params params;

        public Quote(
                final String quoteId,
                final Side side,
                final String symbol,
                final List<Integer> amounts,
                final List<Params> paramsList,
                final Params params) {
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
```

