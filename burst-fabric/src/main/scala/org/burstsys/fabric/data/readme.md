![Burst](../../../../../../../../doc/burst_small.png "")
![Fabric](../../../../../../../fabric_small.png "")

# Fabric Data
## Fabric Distributed Data Model
![](../../../../../../../../doc/data_model.png )
Fabric has a well defined distributed data model. There are various meta-data
and data artifacts associated with model that are carefully defined and managed.

### Brio underpinnings
[Brio](../../../../../../../../../burst-brio/readme.md) data modeling and runtime support is used as an underpinning to all Fabric data model purposes.
Brio provides schema driven _object-tree_ modeling along with encoding and decoding runtime libraries. The Brio
schema is a compatible extended version of the [Motif](../burst-motif/readme.md) schema.

### Slices, Regions, & Items
The physical data reality of a Generation is a set of data chunks, distributed across Worker instances formed into
__Slices__ and __Regions__. A Slice is a proper partition of a the Generation that is located physically on a
Cell Worker. A given Slice can exist on more than one Worker if for instance the previous Worker host
has failed in some way, or if Burst determines having it on one more than one Worker has a performance optimization.
Any given scan will of course only contain one instance of a Slice.

A __Region__ is a subset of a slice, located on a Worker, that is assigned/bound to a specific CPU core/thread. More
specifically, this manifests as a single mmap'ed file which the Fabric Snap Cache can load into and evict from
memory quickly.

An __Item__ is a subset of a Region representing a single __Brio__ _object-tree_ __Blob__ as defined by the
View's __Brio__ schema.


---
------ [HOME](../../../../../../../../../readme.md) --------------------------------------------
