![Burst](../documentation/burst_h_small.png "")

![](doc/alloy.png "")


___Alloy___ is support for Burst unit _(as well as other types of)_ tests.


### Specialized Test Stores

##### Canned
A specialized Store that supports small stored 'datasets on sequence files' to use
for tests

##### Mini
A specialized 'mini' Fabric store that supports the creation of small targeted datasets that can be
used for semantic/unit/performance tests

##### Sear
The Sear Store is a specialized store for benchmarking the performance
of the fabric cache write pipeline. And `View` specifying this store
will create a 500MB `Slice` on each active `Worker` within the Cell.
Each `Item` in each Slice is a copy of a single Mock Alloy Schema Blob.
This can be used in concert with cache metrics to get write rates to all
spindles in play if there was no slow down in any predecessor steps in the
import pipeline. The Sear Store is always available.

##### Alloy
The Ally Store is a store that is very basic sample store server that loads, transforms, presses, and streams JSON files
to a burst cell.  It is both an example of how a remote sample store is built and it is used for test of the sample
store protocol between a burst cell and the sample store skeleton.


---
------ [HOME](../readme.md) --------------------------------------------
