![Burst](../../../../../../../../../../documentation/burst_h_small.png "")
![Burst](../../../../../../../../doc/fabric_small.png "")

# Fabric Configuration

## Worker Caches
|  system property |  default |  description |
|---|---|---|
|  burst.fabric.cache.tend.enable |  false |  XXX  |
|  burst.fabric.cache.tend.period.hours |  1 hour |  XXX  |
|  burst.fabric.cache.stale.hours |  24 hours |  XXX  |
|  burst.fabric.cache.regions |  worker thread count |  XXX  |
|  burst.fabric.cache.spindles |  single tmp folder |  a ';' separated list of folders on separate drives  |
|  burst.fabric.impeller.threads |  Runtime.getRuntime.availableProcessors |  how many impeller threads   |
|  burst.fabric.cache.parcel.enable |  false |  enable parcel pipeline mode   |


## Execution
|  system property |  default |  description |
|---|---|---|
|  burst.fabric.worker.heap.gb |  4 |  size of worker heap mem in GB  |
|  burst.fabric.worker.direct.gb |  4 |  size of worker direct mem in GB  |
|  burst.fabric.worker.core.count |  Runtime.getRuntime.availableProcessors |  cores per worker  |
|  burst.fabric.worker.count |  1 |  number of workers in cell  |

## Spaces
|  system property |  default |  description |
|---|---|---|
|  burst.fabric.space.tend.ms |  3 minutes |  XXX  |
|  burst.fabric.space.idle.ms |  15 minutes |  XXX  |
|  burst.fabric.space.evict.ms |  3 minutes |  XXX  |
|  burst.fabric.space.flush.ms |  3 minutes |  XXX  |
|  burst.fabric.space.tend.ms |  3 minutes |  XXX  |


---
------ [HOME](../../../../../../../../../readme.md) --------------------------------------------
