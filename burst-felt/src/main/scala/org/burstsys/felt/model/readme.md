![Burst](../../../../../../../../doc/burst_small.png "")
![Felt](../../../../../../../doc/felt_small.png "")

# Felt Model
The Felt model is a set of types that are used
to build a `semantic-tree` that can `code-generate` a set of
executable runtime _artifacts_ that execute a single pass scan
of a [Brio](../burst-brio/readme.md)  dataset that produces a `behavioral-analysis`. 
It is designed to be performant, flexible, semantically rich, and extensible. 

### semantic-tree construction
The basic purpose of Felt is to support the construction of a complex object
that defines a specialized [semantic-tree](../burst-felt/src/main/scala/org/burstsys/felt/model/tree/readme.md). 
This tree structured object model and its component subtrees,
are moved through various phases of processing and validation and ultimately
code generate [Artifacts](Felt Artifacts) that are 
used by a subtype of FabricScanner called the HydraScanner at runtime to scan 
[Brio](../burst-brio/readme.md) 
`encoded` and `schema-compliant` datasets in order to produce desired behavioral analysis results.

### code generated artifacts
As part of code generation the following scala class artifacts are generated:
* [Felt Schema](../burst-felt/src/main/scala/org/burstsys/felt/model/schema/readme.md) - generated _once per
  Brio schema_ referenced (and cached),
  this supports all schema specific code generated semantic including the encoded object tree traversal.
* [Felt Sweep](../burst-felt/src/main/scala/org/burstsys/felt/model/sweep/readme.md) - generated `once per
Felt analysis semantic tree`, this supports the runtime scan that produces the behavioral analysis results.

## Felt Tree Components
1. [Felt Tree](./tree/readme.md) -- support for the overall semantic tree
1. [Felt Schema](./schema/readme.md) -- the FELT support for Brio schema
1. [Felt Sweep](./sweep/readme.md) -- the code generated executable scan closure
1. [Felt Analysis](./analysis/readme.md) -- top of the tree, generates to a Sweep
1. [Felt Visits](./visits/readme.md) -- traversal execution points
1. [Felt Expressions](./expressions/readme.md) -- complex executable statements
1. [Felt Collectors](./collectors/readme.md) -- mutable runtime collection of data
1. [Felt Types](./types/readme.md) -- Felt typing system
1. [Felt Lattice](./lattice/readme.md) -- Brio schema conformant traversed immutable data input
1. [Felt Mutables](./mutables/readme.md) -- built-in mutable data collections e.g. set, array, map
1. [Felt References](./reference/readme.md) - path name to declaration implementation bindings

---
------ [HOME](../../../../../../../../../readme.md) --------------------------------------------
