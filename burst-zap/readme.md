![Burst](../documentation/burst_h_small.png "")

![](./doc/zap.png "")

___Zap___ is set of fast and memory efficient _collector_ data structures that are used
during scans to collect results that are categorized into two groups:
1. __Ephemeral Lifetime Collectors:__ data collected during a scan, and _used_ during the scan, but _not_ passed back as
the results/range of the scan function
	1. [Wheels](doc/wheels.md)
	1. [Tablets](./doc/tablets.md)
	1. [Routes](./doc/routes.md)
1. __Scan Lifetime Collectors:__ data collected during a scan _and_ passed back as the part of the results/range of the scan function.
1. [Cubes](./doc/cubes.md)
1. [Shrubs](./doc/shrubs.md)

Because scans are inner loop operations on potentially enormous datasets, collectors are designed to be extremely
fast and memory efficient (low GC pressure).

### Dependencies
This is built on top of:
* [Tesla](../burst-tesla/readme.md)  Native Memory Support
* [Felt](../burst-felt/readme.md) Language Toolkit


---
------ [HOME](../readme.md) --------------------------------------------
