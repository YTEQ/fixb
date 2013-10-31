FixB (FIX Bindings)
=====

FixB is a library for Java/FIX bindings that simplifies serialization of Java objects to/from FIX protocol messages.
The bindings are implemented using Java annotations.

The library is especially useful when there is a need to interchange custom non-standard FIX messages.

Features include:

* Custom FIX tags
* Supports any protocol version as long as the format is FIX
* No requirement for classes mutability, inheritance, constructor etc.
* Model classes can be as complex as needed including composition and inheritance
* Supports FIX fields, repeating groups, blocks/components
* FIX bindings for enum types (with default behaviour based on ordinal value)
* FIX bindings for JodaTime types
* Repeating groups binding directly to java collections
* Optional QuickFIX/J adapter (fixb-quickfix)

Usage
-----

To include FixB into a maven project add the following repository in your settings.xml or pom.xml:
```xml
<repository>
    <id>fixb-repo</id>
    <url>http://raw.github.com/YTEQ/fixb/mvn-repo/</url>
    <snapshots>
        <enabled>true</enabled>
        <updatePolicy>always</updatePolicy>
    </snapshots>
</repository>
```

And add the following dependency in your pom.xml (check for the latest version):
```xml
<dependency>
    <groupId>org.fixb</groupId>
    <artifactId>fixb</artifactId>
    <version>1.0-beta</version>
</dependency>
```

### Annotations example (tags are fictitious):
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

To start serializing your POJOs into FIX messages and vice versa it's enough to create an instance of a FixSerializer as
below:
```java
FixMetaDictionary fixMetaDictionary = FixMetaScanner.scanClassesIn("my.fix.classes.package");
FixSerializer<Object> fixSerializer = new NativeFixSerializer<>("FIX.5.0", fixMetaDictionary);
```

And actually using the created FixSerializer:
```java
FxQuote fxQuote = new FxQuote(...);

// Convert a POJO into a FIX message
String fixMessage = fixSerializer.serialize(fxQuote);

// Read a FIX string as a FxQuote instance
FxQuote fxQuote = fixSerializer.deserialize(fixMessage);
```
