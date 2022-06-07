![Burst](../../doc/burst_small.png "") ![Yahoo](../../doc/yahoo_small.png "")

_Motif:_ ```'recurring salient thematic element...'```

# Value Expressions
    valueExpression:
        ...
        | path
        | constant
        ...
    ;

A value expression is one that returns another value expression. There are three types, __constants__,
__paths__, and __complex__.

### Constants
    constant: 
        booleanLiteral | byteLiteral | shortLiteral | integerLiteral
        | longLiteral | doubleLiteral | stringLiteral 
        ;
A constant is a simple value expression that is manifest as a literal within the query text e.g. a string literal such
as _'hello'_.  These reflect values that match the datatypes in the Motif schema model.  These are
__static__ constructs in that their value can be determined at parse time.

### Paths
    path: identifier ('.' identifier)* mapKey? ;
    mapKey :'[' expression ']' ;
 A __path__ is a __runtime__ access to an instance or a relation within an instance in the object-tree 
 being filtered. These are __dynamic__ constructs in that their value can not be determined at parse time.
    
### Casts
    valueExpression:
        ...
        | 'CAST' '(' expression 'AS' datatype ')'
        ...
    ;
Casts are used to match certain datatypes to be compatible with other datatypes
before using them in expressions that require that. 

###### Converting Numbers to Larger Numbers
    CAST(user.sessions.events.aByteParameter AS LONG)
Any number can be cast to a larger datatype e.g. byte to short, short to integer.

###### String to Non-string Datatype casts
Strings can be cast to non string datatypes based on standard STRING representations.
   
    CAST(user.sessions.events.parameters['currency_amount'] AS DOUBLE)
would take a `STRING` value and cast it to a double -- assuming it contained a
string that represented a double such as `'1.3'`.
 

###### String to Non-string Datatype casts
Additionally the cast function can be used to handle __ISO 8601__ formatted datetime strings. e.g.

    CAST("2017-02-16" AS DATETIME) > user.sessions.startTime
This would create a __DATETIME__ value (syntactic sugar for a __LONG__) that represents that 
date. 

### Complex Value Expressions
Complex value expressions are combinations of other expressions that when evaluated return another
value expression.
    
#### Unary Expressions
    valueExpression:
        ...
        | PLUS valueExpression     // non inverted
        | MINUS valueExpression    // inverted
        ...
    ;

These are operations that take a single value expressions, expresses an operator function 
on it, returning another value expression. Right now the only operators are __'+'__, and
__'-'__. Yes the __'+'__ is a bit nonsensical but is there for completeness.

#### Binary Expressions
    valueExpression:
        ...
        | valueExpression ASTERISK valueExpression      // multiple
        | valueExpression SLASH valueExpression         // divide
        | valueExpression PERCENT valueExpression       // modulo
        | valueExpression PLUS valueExpression          // addition
        | valueExpression MINUS valueExpression         // subtraction
        ...
    ;

These are operations that take two value expressions, express an operator function 
on it, return another value expression.

#### Aggregate Expressions
    valueExpression:
        ...
        | (SUM | MIN | MAX | COUNT)  valueExpression
        ...
    ;

Aggregate expressions are operations on valueExpression which are evaluated at __runtime__ collecting data across the traversal
and then having that aggregate value used in a _deferred_ modality to 
impact the final result of the expression. These are very special aspects to
the Motif world since they are only _deferred_ operations. 



#### [DateTime Expressions](datetime.md) 
