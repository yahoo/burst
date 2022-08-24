![Burst](../../../../../../../../doc/burst_small.png "")
![Fabric](../../../../../../../fabric_small.png "")

# Fabric Configuration


## Master Connection
|  system property |  default |  description |
|---|---|---|
|  burst.fabric.master.host |  0.0.0.0 |  host name or IP to bind protocol to  |
|  burst.fabric.master.port |  37040 |  port to bind protocol to  |

## Worker Assessment
|  system property |  default |  description |
|---|---|---|
|  burst.fabric.assess.period.ms |  15 seconds |  frequency of fabric worker assessment  |
|  burst.fabric.assess.timeout.ms |  20 seconds |  timeout for fabric worker assessment  |


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

## Protocol
|  system property |  default |  description |
|---|---|---|
|  burst.fabric.net.host |  0.0.0.0 |  host/address for fabric net  |
|  burst.fabric.net.port |  37060 |  port for fabric net  |
|  burst.fabric.net.client.threads |  Runtime.getRuntime.availableProcessors |  XXX  |
|  burst.fabric.net.server.threads |  Runtime.getRuntime.availableProcessors * 2 |  XXX  |
|  burst.fabric.kryo.output.buffer.max.mb |  10 MB |  Max size in MB for fabric kryo output buffer  |



---
------ [HOME](../../../../../../../../../readme.md) --------------------------------------------
