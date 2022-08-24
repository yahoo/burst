![Burst](../../doc/burst_small.png "") ![](./felt_small.png "")
--

# Functions
Felt has a small set of _built-in_ ___functions___.

## Brio Functions
Felt exports a small set of ___Brio___ functions which allow interaction 
with data model semantics.

###### size
    
    size(p:Path): Integer
The `size` function takes a path `p`  which should refer to a relation that can be
tested for size. The size of that relationship is returned as an integer.

###### contains
    
    contains(p:Path, value_expression+): Boolean
The `contains` membership test 
function takes a path `p`  which should refer to a `value-vector` 
and one or more `value_expressions` to be tested. If any of the value_expressions
reduce to a value that is in the value-vector, this function returns true.

###### keys
    
    keys(p:Path): value-vector
The `keys` function takes a path `p` referring to a value map and returns a
`value-vector` containing the set of keys for that map.

###### values
    
    values(p:Path): value-vector
The `values` function takes a path `p` referring to a value map and returns a
`value-vector` containing the set of values for that map.

###### value
    
    value(p:Path, key:value-expression): brio-datatype
The `value` function takes a path `p` referring to a value map, and a 
value-expression `key` and returns a
`brio-datatype` instance that is the value for that key in the map.

---
------ [UP](../readme.md) ---  [HOME](../../readme.md) --------------------------------------------
