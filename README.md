FixB (FIX Bindings)
=====

FixB is a library for Java/FIX bindings that simplifies serialization of Java objects to/from FIX protocol messages. The bindings are defined using Java annotations.

Example (tags are fictitious):
```java
    @FixMessage(type = "Q")
    public class FxQuote extends BaseQuote {

        public static enum Side { BUY, SELL }

        @FixField(tag = 40)
        private final Side side;

        @FixField(tag = 12)
        private final String symbol;

        @FixGroup(tag = 13, componentTag = 14)
        private final List<Integer> amounts;

        @FixGroup(tag = 20)
        private final List<Params> paramsList;

        @FixBlock
        private final Params params;

        public FxQuote(
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

        public List<Integer> getAmounts() {
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

To start serializing your POJOs into FIX messages and vice versa it's enough to create an instance of a FixSerializer as below:

```java
    final FixMetaRepository fixMetaRepository = new FixMetaRepositoryImpl("my.package.with.fix.classes");
    final FixSerializer<Object> fixSerializer = new NativeFixSerializer<>("FIX.5.0", fixMetaRepository);
```

And actually using the FixSerializer:

```java
    final FxQuote fxQuote = new FxQuote(...);

    // Convert a POJO into a FIX message
    final String fixMessage = fixSerializer.serialize(fxQuote);

    // Read a FIX string as a FxQuote instance
    final FxQuote fxQuote = fixSerializer.deserialize(fixMessage);
```