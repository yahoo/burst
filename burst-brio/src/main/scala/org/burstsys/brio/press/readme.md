![Burst](../../../../../../../../documentation/burst_h_small.png "")
![](../../../../../../../doc/brio_small.png "")

![Burst](./pressing.png "")

___Pressing___ is the forward scan binary encoding of a Brio [Schema](schema.md) conformant Brio data
[Model](model.md) , into a
Brio [Blob](blob.md). This is done in as time and space performant fashion as possible since the input and output
sizes can be extremely large.

##### In situ decoding
All values in the encoded Blob are serialized on the fly ( **in situ** ) if and only if they are accessed
in a scan. This means there is no deserialization unless the value is actually used. There is inherently
no deserialization required for the overall tree structures and the relationships between instances within the tree.

#### Pressing
The act of taking some form of input data and encoding it in the appropriate binary format for a Blob is called
_'pressing'_. There are two forms of pressing

1. __V1 pressing__ _(old school)_ - very inefficient and GC noisy way of creating a Blob. Uses standard on-heap
   Brio Dictionaries.
2. __V2 pressing__ _(zap)_  - Zap format described in the Zap Blob documentation. Very efficient, low GC noise technique.
   Uses Zap Dictionaries.

# Runtime access
Critical to Brio performance goals is the ability to do very fast and GC quiet read scans and write scans.

##### Read Scans
The _Lattice_ is a set of Scala value classes (not actual JVM on heap objects) which support scanning through a
Brio Blob object tree, accessing fields and relationships on the fly.

##### Write-scans
There are two types of write scans the old V1 way which is now deprecated, and the V2 way called _Roll Pressing_. Roll
pressing uses the Scala Value class trick to provide a very efficient way to

# Storage/Retrieval
Brio format can be written to disc and then
read into memory with zero copies or transformations between disc and memory formats.

##### Memory footprint
Brio is meant to be stored as a single block of memory preferably in off heap direct memory. All write and read
scanning functions are coded in zero object allocation design patterns using Scala value classes and recursive
traversals that mean the only objects created as part of that are simple primitives that can be stored on the stack
and so require no allocation/GC.

---
------ [HOME](../../../../../../../../../readme.md) -------------------------------------------- 
