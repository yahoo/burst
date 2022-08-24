![Burst](../../doc/burst_small.png "") ![Burst](../fabric_small.png "")
---

 ![](cache.png "")

The ___Cache___ is the Worker side emphemeral storage for Brio datasets. Each
dataset is _generated_ from a __View__ and is divided up into a set of ___Slices___
that are distributed across an appropriately chosen set of Worker nodes.
Once in the Cache, each Slice can be efficiently `mmap'd` from local disk into
memory. It can also be even more efficiently `free'd`  from memory. This
allows the OS to do the sorts of
macro level optimizations for host memory and disk utilization
that it is best in a position to provide.
This ability to efficiently flip Slices in
and out of memory is critical to the cost effective use of memory in Burst.

### Slices
Each individual Slice is located on a single Worker. It is possible to have
more than one Slice for a given _generated_ View reside on the _same_ Worker,
but since Burst strives for maximum fanout of engaged Workers for
[Waves](waves.md), this
would be considered non optimal and generally would be done as a response
to an unusual situation such as a dynamic node failure or non standard
test configuration.

### Region Files

| Item | Type |Description |
|---|---|---|
|magic |Byte | magic number (__85__) |
|version |Byte | version of encoding (__1__) |
|blobs |`Array[BrioBlob]` |uncompressed encoded Blobs |

___Regions___ are a subdivision of a given Slice into a partitioned set of page aligned,
physically memory-contiguous, cpu-core-thread affine, individual chunks.
Each Region is manifest as a uncompressed  __Java NIO2__  managed
flat file with a simple fixed size binary header followed by
a linear sequence of [Brio Blobs](../../burst-brio/doc/blobs.md).

### Slice + Region lifecycle
In the worker a __Slice__ is owned by a __Snap__. During the load process, the cache passes a store worker a
snap to fill with data. Each loader implementation must make the following calls on the slice:

1. `FabricSliceData.openForWrites` once to initialize the slice for writing
1. `FabricSliceData.queueParcelForWrite` once for each parcel to write to the slice
1. `FabricSliceData.waitForWritesToComplete` once, _after all parcels have been queued_, so that the slice's region files are marked as complete
1. `FabricSliceData.closeForWrites` once to clean up filesystem related artifacts

Store workers must ensure any call to `.openForWrites` _is always_ matched by a call to `.closeForWrites`.
Each slice manages a set of regions (each backed by a file) and once `FabricSliceData.waitForWritesToComplete` 
is called the slice notifies the regions that they will not receive any further parcels to queue.  

### Spindles
Region Files are stored in Worker _node-local_ file systems. The Cache
is configured to use a specific set of individual disk entities
called _spindles_, to store these Region files, ensuring that
parallel reads and writes are as fast as possible. This holds for
either magnetic or solid state drives, though the latter case may
have unique characteristics such as a single physical disk per node.

### Reading
Each Region, once resident in memory, can be scanned directly without any other
pre processing such as bulk deserialization. Each Region is bound to a
single unchanging worker _cpu-core-thread_ which is the only thread
that can be assigned to scan the Blobs in that Region.

### Writing
During View _generation_, Slice data is _round-robin_ written to the full set of region files 
as the Blobs are received from the [Nexus](../../burst-nexus/readme.md) protocol.
This generally ensures the Blobs are evenly distributed across Regions.

The general model is that there are a set `impellers` assigned to each
provided `spindle` folder (presumably separate disk entities though
they do not have to be) That impeller has a thread and a queue and so
there are a fixed set of worker threads per spindle and all region
writers vector their IO through that fixed pool. This provides for a
carefully selected max parallelism to a given disk entity, and no
bias towards any region's write list since queues are written one buffer
at a time.

###### Asynchronous Disk Writes
_We would like to support the ability to write to memory synchronously
and to disk asynchronously. This would allow us to keep up with higher
write speeds and provide Slices to be scanned with lower latency. There
is additional complexity due to managing possible disk failures_ **after** _the
memory is being scanned, but this is probably worth tackling._



---
------ [UP](../readme.md) ---  [HOME](../../readme.md) --------------------------------------------
