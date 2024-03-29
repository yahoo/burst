![Burst](../../../documentation/burst_h_small.png)

# `Burst Data Structure Architecture`
The combination of our dependency on a standard JVM runtime
and the hot inner loop algorithms of Burst's single pass scan model,
make for extraordinary high demands on CPU/memory architecture.
From the very start of design and implementation, we had to 
be uncompromising in our approach to time and space resources.

## Design Rules
What this translated to was a small number of rules to follow for what we consider
major data structures (_those that are large and/or accessed within hot inner loops_):
* **never** allocate JVM objects during the single pass scan.
* **all** major data structures are contiguous chunks of non-JVM (_native_) memory
* **where possible** keep accesses to major data structures isolated to a single thread
* **where possible** access major data structures in a forward-only access pattern
* **always** examine and optimize generated bytecode for major data structures
* **where possible** avoid locks/synchronization on high concurrency, high
transaction rate data structures.

Put another way, do not allow the JVM to manage your objects and try to help out
the CPU cache line memory system.

This then translated to a series of design patterns:
* create our own custom data structures in non JVM allocated off-heap memory
* allocate and access those data structures using per-thread pools based on lock-free off-heap
queues
* grow data structures via appending memory
* do not use java accessors - do your own memory ptr math

## Non JVM Structures (off-heap)
Burst was committed to a JVM runtime environment at the beginning of our research and design
phase. As we developed our base line algorithms, i.e. the single-pass scan combined with
the basic set of counting/grouping calculus operations, we discovered right away
that the required transaction 
rates against required scale scale datasets would quickly break down where even a single JVM object
was allocated in an inner loop. That is a sobering reality and one we had to grapple with in a big way.
While the modern JAVA GC subsystem was nothing short of miraculous in being good at what it does, 
and could be fine tuned a million different ways, it was completely inadequate to meet our needs.
So like many other JVM based systems we had to code all of our inner loop operations completely
outside the JVM womb.

## Off Heap JVM
The way we did this is by using basically the same techniques that are used in the Java libraries. 
While not explicitly supported, it is possible to reach below the Java veneer and directly
allocate, manipulate (_via standard memory pointer arithmetic_), and free process level memory. 
This is often called _off-heap_ or _native_ memory. It is at time challenging to use, especially
in the lack of direct tools support, but it was a lifesaver.

## Scala Value Classes
While we gave up on standard JVM objects, we did get some help from
the fact that Burst is written in Scala. Scala has a language 
feature called 
[Value Classes](https://docs.scala-lang.org/overviews/core/value-classes.html). 
These are a special category of Scala Objects that  can
in a well-defined set of usage patterns create a compiler driven _illusion_ of an object
without in most cases actually creating an underlying JVM object.
This allows Burst to take an off-heap physical memory pointer or physical memory offset referencing some part
of native, off-heap memory, and have the compiler 'wrap' that in a set of static
accessor that deliver much of the
convenience and design elegance of objects.

## Locking and concurrency
The high transaction rates and large data sizes that Burst demanded also showed us that
many (most?) standard data structure such as standard JAVA queues did not perform well when stressed by
high rates of fine-grained operations especially with more than one thread involved. They also tended
to _shed_ or _churn_ JVM objects. Luckily we did not have to write our own lock free off heap queues.
Luckily we found them in [JCTools](https://github.com/JCTools/JCTools). Our off heap data structures
are _pooled_ using their lock free queues.

## Thread binding and Locality
We were also very careful to organize/pool off heap memory into regions that were restricted to
use only by a single thread. While we do not have **direct evidence** this method delivered
to our expectations, our working theory of operations regarding modern multiprocessor/memory
systems would seem to favor this.

## linux disk-backed memory
Burst single pass scans large datasets stored in disk backed caches. 
These caches need to be able to flip this data
 in and out of memory quickly and efficiently. Like
many systems out there, Burst makes heavy use of the OS provided (linux) disk mmap operations.
These operations can be coaxed to work with our off-heap data structures. The combination of 
off heap, mmap, regions, caching, async IO, all come together harmoniously in standard Java lib
disk operations. This allows us to outsource much of our memory management to the
operating system. This performed very well for us especially since we also scanned memory
in a forward only mode which works well with standard OS aggressive page prefetching models.

