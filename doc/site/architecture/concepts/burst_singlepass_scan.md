![Burst](../../../../doc/burst_small.png)



# `Burst Single Pass Scan Architecture`

The single pass scan is one of the earliest and most aggressive decisions in the Burst architecture.
Processing speed of large datasets is center stage and everything else takes on a supporting role.

### Behavioral Calculus
Behavioral analysis on high scale datasets involves 
scanning stunning quantities
of data records each of which represents an individual entity whose 
behavior is being studied and compared to other entity data records.
The aggregate analysis counts and groups and generally measures/organizes
a rich set of facts about the various entity cohorts as well as the
the total superset. Each of these data records is 
a complex object-tree of nested structures and relationships. The analysis
scans through this object-tree with opportunities to evaluate contained
value fields, constantly updating running tallies of
value counts that are carefully grouped by associated dimension values.
The evaluations use language constructed predicates and aggregating functions
to determine what result data to collect.

The single pass scan is how Burst does that fast and efficiently.

### The single pass scan
![](../../../image/burst_single_pass_scan.svg)

The way that the Burst architecture scales up to 
these hyper transaction rates, is something we call the single pass scan.
The basics of this approach are in how we _linearize_, _regionalize_,
_loading_, _threading_ and _collecting_.

###### linearize
Each entity is _encoded_ into a binary data 
format that _linearizes_ a _depth-first-search_
of the entity object-tree into a single byte-sequence. 
What that means is that during the evaluation of a
particular entity, all data access operations are a 
constantly advancing CPU/memory system 
march
through a linear sequence of bytes in a single block of contiguous physical memory. 
The algorithm can jump _forward_ in order to skip _unused_ data, 
but it never goes backwards.  

###### regionalize
Each of these byte-sequences is assembled into large groupings called Regions. 
The scans always move forward through the entities within a single Region.
Entities are always managed as groups of Regions.

###### threading
All the byte-sequences in a Region are always scanned by the same CPU thread. 
This reduces the need for thread synchronization and CPU cache flushing. 

###### loading
Each Regions is loaded from disk cache into memory as
a read-only OS level `MMAP` operation. 
The mapping to physical memory and the managed
relationship to disk IO operations is controlled
by the sophisticated virtual memory systems of a modern operating system.

###### collecting
Along with this scan of linearized contiguous read-only data by a single thread is a set
of mutable data structures that collect data that is used as temporary
and final results during the scan. Each of these
data structures are  carefully designed to avoid lock waits and to keep all data
in a single contiguous memory block, always growing by extending the end of the
block.

## Scanning and the JVM
Burst is written primarily in Scala which is a Java VM based execution environment.
What this means is that part of the Burst challenge is leveraging and mitigating
various Java performance constructs.

### don't use Java objects
Burst invests a significant amount into making sure its data structures and
algorithms _don't create Java objects_ during scans. This involves the use
of careful coding practices and the extensive use of direct manipulation of
native memory, sometimes called off-heap memory.

### optimize at the byte-code level
The Burst pipeline architecture, discussed in more depth elsewhere, 
does full code-generation for each query executed. This means very complicated
algorithms are based on standard code generation templates that are carefully
designed to be relentlessly byte-code friendly.

### help the JIT compiler
The same code generated system and scan access patterns are also designed
to be effectively JIT compiled.
