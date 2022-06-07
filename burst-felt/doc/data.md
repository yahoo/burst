![Burst](../doc/burst_small.png "") ![](./felt_small.png "")
--

# Data Model

### brio value data types
All Felt `expressions` return either a brio value datatype or a `null`. 

###### brio value data types
The `Brio` value types are:
1. `value-scalars` atomic value of the following types:
    1. `boolean`
    1. `byte`
    1. `short`
    1. `integer`
    1. `long`
    1. `double`
    1. `string`
1. `value-vectors` array of value-scalar
1. `value-maps` map of a value-scalar to a value-scalar

Felt does ___not___ manage the passing of `objects` or `instances` i.e. `reference-scalars` or
`reference-vectors`.


---
------ [UP](../readme.md) ---  [HOME](../../readme.md) --------------------------------------------
