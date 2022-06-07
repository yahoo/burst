![Burst](../doc/burst_small.png "") ![](../../doc/hydra_small.png "")
--
![](control.png "")
--

A Burst distributed execution is a scatter/gather algorithm that merges a
large number of independent object-tree scans together into a final result set.
The ___Control___ plane is a set of Hydra features that is used to define where in
each of these object-tree scans we want to execute what _expressions_ and ultimately what information
is read from the [data plane](data.md) and what information is then
placed into the [collector plane](collectors.md)  plane. Each of these locations in
the schema  defined scan are called [visits](visits.md) and each
Visit is divided into a set of phases called [actions](actions.md).

### Schema Defined Object Trees

### Depth First Traversal Scans

### Error Handling



---
------ [UP](../readme.md) ---  [HOME](../../readme.md) --------------------------------------------
