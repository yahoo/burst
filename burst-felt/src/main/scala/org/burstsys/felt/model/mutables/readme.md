![Burst](../../../../../../../../../../doc/burst_small.png "")
![](../../../../../../../../../doc/felt_small.png "")

# Felt Mutables
Mutables are one of a set of collection types that are used to implement all
non-primitive [variables](../visits/readme.md). There are three
types of mutables: [Arrays](#Mutable_Arrays), [Sets](#Mutable_Sets),
_and_ [Maps](#Mutable_Maps)

### Mutable Arrays
    val <variable_name> : Array[ <primitive_type> ] = <initializer>

Arrays are a variable-sized `positional` collection of primitive types.
Arrays can contain zero or more `duplicates` and/or `nulls`.

###### initializers

### Mutable Sets
    val <variable_name> : Set[ <primitive_type> ] = <initializer>
Sets are a variable-sized `membership` based collection of primitive types.
Sets cannot contain `duplicates` or `nulls`.

###### initializers


### Mutable Maps
    val <variable_name> : Map[ <primitive_type> ] = <initializer>
Maps are a variable-sized set of associations each mapping one primitive type
to another. Maps cannot contain `duplicates` or `nulls`.

###### initializers

