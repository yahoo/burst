![Burst](../../doc/burst_small.png "")

_Motif:_ ```'recurring salient thematic element...'```

# The Motif Language

### Components
* [Schema Language](schema.md) -  define associated object-tree typing and structure
* [View Language](views.md) - define views
* [Expression Language](expressions.md) - define filtering specifics
* [Eql Language](eql.md) - create queries over data
* [Segments](segments.md) - define segments groups
* [Funnels](funnels.md) - define funnels


#### Case Sensitivity
Motif is case sensitive. Except for the Schema language, all
keywords can be expressed in either upper or lower (not mixed) case. All
identifiers can be mixed case and are case sensitive.

#### Encoding
Generally __UTF-8__ encoding is used throughout the Motif languages. This means any string can be expressed
using unicode characters. User identifiers can, but are not recommended to be, expressed using unicode. Note
that unicode escape sequences are not yet supported but will be soon.

#### White Space
Throughout the Motif languages we support C standard line `// comment` and block style `/* comment */` comments.
 Line ends are flexible and no semicolons are required to terminate clauses.
 
#### Datatypes
    dataType
        : BOOLEAN_TYPE
        | BYTE_TYPE
        | SHORT_TYPE
        | INTEGER_TYPE
        | LONG_TYPE
        | DOUBLE_TYPE
        | STRING_TYPE
        | DATETIME_TYPE
        ;

The following primitive datatypes are used throughout:

|  type |  description | range | byte(s) |
|---|---|---|---|
| boolean | logical | true or false| 1 |
| byte |  8-bit signed two's complement integer | -128 to 127 | 1 |
| short | 16-bit signed two's complement integer. |  -32,768 to 32,767| 1 |
| integer | 32-bit signed two's complement integer | -2,147,483,648 to 2,147,483,647 | 4 |
| long | 64-bit signed two's complement integer  | approx -9e18 to (9e18 - 1)  | 8 |
| double | double-precision 64-bit IEEE 754 floating point | [read this](https://docs.oracle.com/javase/specs/jls/se7/html/jls-4.html#jls-4.2.3)  |8 |
| string | UTF-8 string |   |variable |
| datetime | 64-bit signed two's complement integer |   |8 |

Note that __datetime__ is not a storage format. It is actually syntactic sugar for conversions in the Motif
expression language e.g. `cast('2017-02-13' as DATETIME)`  will take  ISO 8601 formatted String
and turn it into a epoch time in millis.
