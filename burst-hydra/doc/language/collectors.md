![Burst](../../../documentation/burst_h_small.png "") ![](../../doc/hydra_small.png "")
--
![](collectors.png "")
--
 

Generally Hydra is a way to scan an input object tree and as it does so,
create some sort of result. Collectors are the way that data is collected
during that scan so that it can be used as a ephemeral _scan-lifetime_ data or 
longer lived _result-sets_ which are passed to the next stage in the pipeline. 

###### efficient
Collectors need to be extremely efficient both in time and space since 
they are mutating during the scan often within inner loops of high cardinality
data input sets. These are implemented using [tesla](../../../burst-tesla/readme.md) 
and [zap](../../../burst-zap/readme.md)  libraries in order
to optimize resource requirements.

* [Cubes](cubes.md) -- multidimensional table _result-sets_
* [Dictionaries](dictionaries.md) -- efficient string storage for input and output
* [Tablets](tablets.md) -- _scan-lifetime_ mutable joinable value vectors
* [Wheels](wheels.md) -- _scan-lifetime_ rolling windows
* [Routes](routes.md) -- _scan-lifetime_ graph traversal logger
* [Shrubs](shrubs.md) -- _object-tree_ _result-sets_

---
------ [UP](../readme.md) ---  [HOME](../../readme.md) --------------------------------------------
