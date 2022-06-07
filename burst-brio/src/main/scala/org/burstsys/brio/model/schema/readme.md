![Burst](../../../../../../../../../../doc/burst_small.png "")
![](../../../../../../../../../doc/brio_small.png "")

# Brio Schema


___Schema___  files are the way that the types, structures, and relationships of a Brio
[Model](model.md) are specified to inform and support
write ([Press](../src/main/scala/org/burstsys/brio/press/pressing.md)) and read ([Lattice](lattice.md)) operations against
Brio [Blobs](blob.md) at runtime.


### Antlr Grammar
[Grammar](../src/main/antlr4/org/burstsys/brio/grammar/BrioSchemaGrammar.g4)

### Versions
Any given Brio model defined by a schema, can be versioned by providing multiple numbered specification Schema.
The runtime's support both scalar and vector version heterogeneity i.e. a reference scalar can point to
instances of any of the set versioned types possible. Correspondingly Brio supports type heterogenic
vectors, where any of the members of that vector can be of any arbitrary version of the same collection's type.

