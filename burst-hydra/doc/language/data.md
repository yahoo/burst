![Burst](../doc/burst_small.png "") ![](../../doc/hydra_small.png "")
--

![](data.png "")
--

Hydra's data model is a specialized but conformant
 manifestation of the [Brio Data Model](../../../burst-brio/readme.md)
 with many added features.
Hydra adds constructs that are syntactic and semantic sugar around the various
access patterns but conforms to all of its semantics and restrictions. The basic
nature of a Hydra analysis is to scan a Brio `object-tree` and collect data from it.

## Elements
* [Schema](#schema)
* [Datatypes](#datatypes)
* [Paths](#paths)
* [Primitive Literals](#primitive-literals)
* [Complex Literals](#complex-literals)
* [Variables](variables.md)
* [Parameters](#parameters)

### Data
Hydra analysis interacts with three types of data.
1. __Domain:__ scanned Brio Blob encoded/traversed object trees
2. __Temporaries:__ code generated variables, parameters, literals defined in the analysis itself
3. __Collectors:__ specialized read/write data structures used during the traversal .
3. __Range:__ read/write data structures used to return results.

Note that some __collectors__ e.g. __Cubes__ are used both during the traversal _and_ to return results.

### Schema
All Hydra defined analytics must specify/provide and are bound/validated to a specific Brio Schema. All data access
of scanned data is Brio encoded and conforms to the Brio type system.

### Datatypes
Hydra uses the same set of primitive datatypes that Brio does.

###### Grammar
    BOOLEAN_TYPE: 'boolean' ;
    BYTE_TYPE: 'byte' ;
    SHORT_TYPE: 'short' ;
    INTEGER_TYPE: 'integer' ;
    LONG_TYPE: 'long' ;
    DOUBLE_TYPE: 'double' ;
    STRING_TYPE: 'string' ;

###### Examples
    val foo:integer = 0
    dim1:verbatim[string]

###### Extended Datatypes
Hydra supports Brio extended datatypes however all of these are presented as normal datatypes (`LONG`)
at the level Hydra operates at.


###   Primitive Literals
###### Grammar
    primitiveLiteral: fixedLiteral | floatLiteral | stringLiteral | booleanLiteral | nullLiteral ;
    fixedLiteral: FIXED_LITERAL ;
    booleanLiteral : BOOLEAN_LITERAL ;
    stringLiteral : STRING_LITERAL ;
    nullLiteral : NULL_LITERAL ;
    floatLiteral: FLOAT_LITERAL ;
    
    STRING_LITERAL : ('"' ( ~'"' | '""' )* '"') |  ('\'' ( ~'\'' | '\'\'' )* '\'');

    NULL_LITERAL: 'null' ;
    
    BOOLEAN_LITERAL : 'true' | 'false' ;
    
    FIXED_LITERAL:  DIGIT+  ;
    
    FLOAT_LITERAL
        : DIGIT+ '.' DIGIT*
        | '.' DIGIT+
        | DIGIT+ ('.' DIGIT*)? EXPONENT
        | '.' DIGIT+ EXPONENT
        ;

###### Examples
    val foo:string = "hello" // string literal initialization


###   Complex Literals

###### Grammar
    complexLiteral: arrayLiteral | mapLiteral ;
    
    arrayLiteral:
        ARRAY LP
            valueExpression
            (SEP valueExpression)+
        RP
        ;
    
    mapLiteral:
        MAP LP
            mapAssociation
            (SEP mapAssociation)*
        RP
        ;
    
    mapAssociation: valueExpression ARROW_ASSOC valueExpression ;

###### Examples
    kljshfg

###   Paths
###### Grammar
    path : identifier ('.' identifier)* (LSB valueExpression RSB)? ;
###### Examples
    user.sessions.id
    user.sessions.parameters

###   Parameters
###### Grammar
###### Examples

###   Accessors
Accessors are built-in functions that support the access to data.
###### Grammar
###### Examples
    key(user.sessions.events.parameters)
    value(user.sessions.events.parameters)
    size(user.sessions.events.parameters)
    contains(user.sessions.events.parameters, 'foo')

---
------ [UP](../readme.md) ---  [HOME](../../readme.md) --------------------------------------------
