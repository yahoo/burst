![Burst](../../doc/burst_small.png "")

# Java Runtime Support

### Usage

To load your schemas you should set the `burst.motif.schemaPackage` JVM property. This property tells the motif
subsystem which packages contain your implementation of the `MotifSchemaProvider` class(es).

```java
// initialize motif
Motif motif = Motif.build();

// a suitable schema
String schemaSpecification;

// parse to get a schema object model
Schema schema = motif.parseSchema(schemaSpecification);

// parse to get an informative string representation of a schema object model
String schemaExplanation = motif.explainSchema(schemaSpecification);

// a suitable filter specification
String filterSpecification = "include user where user.sessions.events.eventId != 987356";

// parse to get a filter object model
Filter filter = motif.parseFilter(schema, filterSpecification);

// parse to get an informative string representation of a schema object model
String filterExplanation = motif.explainFilter(schema, filterSpecification);
```

A few important rules:
* The output models for schema, expressions, and filters are to be considered __READ-ONLY__. Please do not
modify these trees or subclass any types.
* The output models are subject to change and optimizations - especially the expressions. Try to 
keep your coupling as loose as possible.

### Concurrency
Motif objects are ___not___ thread-safe. You will need to have a unique one for 
concurrent thread. The cost of constructing one is...

### burst-motif.jar
The deployed artifact is a java library contained within a single uber jar called ___burst-motif.jar___.
This jar has all of its transitive dependencies flattened and packaged. Some of these transitive dependencies will
 be _relocated_ so that the particular internal version is __hidden__ and does not conflict with external client
systems.

### Constant Reduction
    Expression expression; // any kind of expression
    if(expression.canReduceToConstant()) {
        Constant constant = expression.reduceToConstant();
    }
Sometimes you want all the __constant__ expressions in your tree (such as for authoring) sometimes
you want to get rid of them (for execution models). If for any expression or sub-expression this
is possible, you can use the design pattern above to filter out sub expressions that reduce
to a single constant.

### Optimized Expressions
    Expression expression; // any kind of expression including filters
    Expression optimizedExpression = expression.optimize();
A filter and all other expression types can be optimized. The `optimize()` method
returns a rewritten expression with constant reduction (thats mostly what optimization is)
