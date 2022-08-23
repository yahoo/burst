![Burst](../../../../../../../../../documentation/burst_h_small.png "")
![](../../../../../../../../doc/felt_small.png "")


# Collectors
Collectors are mutable relatively complex specialized
runtime structures within a Felt analysis that are used
to collect data during traversal. Collectors are very specifically
designed to implement a specific type of analysis during the single
pass scan.

## frame hosted
Collectors are always hosted in a [Frame](../frame/readme.md) container. 

* [Cube Collectors](../collectors/cube/readme.md) -- multidimensional hierarchical table structures
* [Route Collectors](../collectors/route/readme.md) -- temporal/causal reasoning
* [Tablet Collectors](../collectors/tablet/readme.md) -- segmentation reasoning
* [Shrub Collectors](../collectors/shrub/readme.md) -- object tree raw data output structures

