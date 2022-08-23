![Burst](../documentation/burst_h_small.png "")


![Brio](./doc/brio.png "")

___Brio___ is Burst's data model.

It provides the following functionality:

* modeling/type system specialized for
behavioral-analysis `object-tree` `entities`.
* parsed schema Brio language for model
* disk/memory Brio binary format `binary-encoding` efficient
disk/memory format specialized for zero `GC-churn`
`single-pass` `depth-first-traversal` (**DFS**)
linearized byte-order scanning reads.
* runtime support for Brio format writing and reading

# `Components`

* [Model](src/main/scala/org/burstsys/brio/model/readme.md) --
type and object modeling system library
* [Blobs](src/main/scala/org/burstsys/brio/blob/readme.md) --
runtime disk/memory binary encoding format
* [Pressing](src/main/scala/org/burstsys/brio/press/readme.md) --
runtime encoding (write) system
* [Lattice](src/main/scala/org/burstsys/brio/configuration/readme.md) --
runtime encoding (read) system
* [Provider](src/main/scala/org/burstsys/brio/provider/readme.md) --
runtime schema discover/load system
* [Types](src/main/scala/org/burstsys/brio/types/readme.md) --
Brio types
* [Dictionaries](src/main/scala/org/burstsys/brio/dictionary/readme.md) --
runtime string to key and key to string lookup support
* [Extended Types](src/main/scala/org/burstsys/brio/extended/readme.md) --
future planned extensions to model
* [Configuration](src/main/scala/org/burstsys/brio/configuration/readme.md) --
configuration properties

---
------ [HOME](../readme.md) --------------------------------------------
