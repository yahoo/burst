![Burst](../documentation/burst_h_small.png "")

![](./doc/tesla.png "")

___Tesla___ is the `memory` and `thread` management library for Burst.
Burst has a no holds barred
low latency and high processing efficiency goals and
so the way we use memory and threads is a really big deal.

## `memory management`
The JVM world of automatic
garbage collection (**GC**) is enormously convenient for its no muss
no fuss allocation and deallocation. And while there is a
truly inspiring history of technology advances related to
making GC high performance, it's unfortunately a myth (at least currently)
that the performance of GC is good enough to support the sort of
enormous processing rates of Burst single pass scans.

We assert two basic
rules to optimize these two demands when using JVM objects:
1. Create as few objects as possible in inner loops, keep them simple, allocate per thread, and reuse where possible
2. Where possible do not use on-heap JVM garbage collected objects, allocate off heap in largish contiguous chunks.

#### Per Thread Pooled Objects
GC fanboys will tell you that you don't need to pool your objects - just let them go to the GC gods but that simply is not
true if you are trying to do billions of operations in under a second. If each op creates only 10 emphemeral objects
(a very low number), you quickly die of GC pressure induced asphyxia. We provide in this library some generally useful
object pools. We highly recommend you use these pools one per worker thread accessed via a thread local - its much
better to not have to synchronize the internals and face cross thread concurrency slowdowns. Its also important to
design your objects so that they can be __reused__.

#### Off Heap Objects
Burst embraces the reality that JVM ___objects___ are only useful for the __non-big-data__ parts of your code. JVM garbage
collection can only handle object allocation/deallocation in very small heap sizes if
you care at all about throughput and/or latency (which we do). We provide the basics in this package for storing data
off heap (google the Sun 'unsafe' object)

#### *Blocks* - Common offheap multimodal shared pool
* one stop shop for all off heap memory allocations
* fixed page size/aligned allocations
* direct buffers
* boundaries for maximum memory usage with free/release as limits reached
* understanding of scenarios such as unit test vs production

## `thread management`

## `imported data structure libraries`
* [trove](https://bitbucket.org/trove4j/trove) -- _high performance data structures_
* [jcl](https://github.com/JCTools/JCTools) -- _off-heap lock free data structures_

## `tesla data structures`
* [blocks]() -- _sdfgsdf_
* [buffers]() -- _sdfgsdf_
* [directors]() -- _sdfgsdf_
* [flex]() -- _sdfgsdf_
* [offheap]() -- _sdfgsdf_
* [parcels]() -- _sdfgsdf_
* [parts]() -- _sdfgsdf_
* [pools]() -- _sdfgsdf_
* [scatters]() -- _sdfgsdf_
* [threads]() -- _sdfgsdf_

## `Configuration`
|  system property |  default |  description |
|---|---|---|
|  tesla.worker.threads |  Runtime.getRuntime.availableProcessors |  the fixed worker thread pool size  |
|  tesla.parts.tender.frequency |  15 |  frequency of part factory pool tending in seconds  |



---
------ [HOME](../readme.md) --------------------------------------------
