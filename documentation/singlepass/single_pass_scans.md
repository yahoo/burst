![Burst](../burst_h.png "")

# `The single pass scan game`

If there is any one _most salient_ technological aspect of the Burst world it's
the `single-pass-scan` and all the various attendant
challenges associated with that getting that right.
The `single-pass-scan` is the critical `inner-loop` of Burst
[Behavioral Analysis](../behavior/behavioral_analysis.md) and 
the way we approach that game is where many of its  performance wins lie.

## `the game`
Burst analytics are significant calculations across 
_high-cardinality_ sets of behavioral _entities_ where each
of those entities is an `object-tree` with generally _high-cardinality_
collections of behavioral '_events_'. All of that data needs to be
filtered/measured/categorized as fast and as efficiently as
technologically practical.

#### `the challenges`
The _bad_ news is that Burst needs to provide high transaction-rate,
low-latency calculations day in and day out, on very 
large entities sets where each entity can be quite large and the basic
algorithms so efficient as to be 
limited by the simply reading and writing of memory.  Very simple
changes to how memory is read or how an instruction is turned into
byte code can make dramatic differences.

This means:
1. We need to strenuously limit the number of VM objects created and
   carefully manage non VM memory as well.
1. We need to carefully optimize how multiple CPUs and cores
   and their cache lines interact with 
   the various cache levels of the memory architecture.
1. We need to be sure we are using best practices with our multicore thread
   usage especially as regards synchronization.

#### `the opportunities`
The _good_ news come in two forms:
1. There is no need to calculate direct inter-entity relationships i.e. most of
   the calculus ends up with a high degree of locality within the
   entity object-tree. This allows us to divide up our processing across
   multiple cores and multiple nodes.
1. processing is inherently ordered by causality/time i.e. there is a high degree
of directionality in our algorithm. This allows us to take advantage of modern
   hardware's innate forward moving path optimization.

## `how to win`
All this translates to a finite number of design practices:
1. rigorously (no exceptions) translate all analytic processing of the
   entity object-model into a single pass depth first traversal (easier said than done)
1. don't create _**any**_ VM objects during the scan. Evens small
   amounts of GC are death at Burst operation rates.
1. on a given worker node, batch entities into contiguous memory 'regions' and 
   bind all operations to a single thread/core.
1. place all significant data structures into off-heap memory 'parts'
1. manage parts using lock-free, off-heap queues (thanks JCL)
1. always move forward in a byte order sense when accessing large chunks of 
off heap memory (such as the Brio Blob)
1. carefully divide threads into finite sized 'cpu bound' and 
   cached 'async request' pools.
1. be mindful of concurrency levels and transaction rates on queues
1. have the OS do what it is best at e.g. `mmap` files
1. _generate_ the final analysis algorithm into reusable maximally 'efficient' 
   bytecode and allow that byte code to JIT optimize.
1. highly specialized data structures such as [Felt Cubes and Routes](../../burst-felt)
   
## `the winner`
The `single-pass-scan` was an enormous architectural _bet_ made in the very
early stages of the Burst architecture. It was not even always clear that it _could_
be done i.e. that all the questions we would want to ask could be answered
that way. Fortunately, it did in fact turn out to be a successful bet.
This decision permeates throughout the architecture. However, take
an especally close look at these relevant modules for a deeper dive:
* [Brio](../../burst-brio) -- single pass scan encoded binary data format
* [Tesla](../../burst-tesla) -- thread and memory management
* [EQL](../../burst-eql) -- declarative language with single pass scan semantic output
* [Felt](../../burst-felt) -- an execution semantic object model for single pass scans
* [Fabric](../../burst-fabric) -- multi-node / multi-core distributed processing
* [Zap](../../burst-zap) -- high performance off heap data structures for single pass calculus

---
------ [HOME](../../readme.md) -------------------------------------------- 


