![Burst](../../documentation/burst_h_small.png "")  ![](./zap_small.png "")

 ![](./cubes.png "")

___Cubes___ are a very specific data structure designed to support the gathering of dimensional aggregations during Burst scans. It is designed
to be very low GC pressure, very sympathetic to CPU mechanics, and provide generally fast intrinsic state access. 
The Burst _single pass_ hyper-optimized scan concept, central to the Burst architecture, places a critical basis dependency  on
the ability to use language techniques to convert general analytic queries to single pass scans using recursive Zap Cubes to _gather_ dimensional
models for results based on flexible rich semantic definitions.

#### Design/Semantic Rules

 1. Zap Cubes store most (and eventually all) data in a single offheap chunk (per cube instance). When Zap Cubes were
designed, Scala value classes did not exist so these are the one Zap structure that does not take advantage of this.
 1. Zap Cubes are non-reentrant and take advantage of the fact that scans are distributed across cores and
  hence are defined to be single threaded at all times.
 1. Zaps Maps have a fixed size off heap memory chunk that is allocated based on the _row limit_ specified for results.
If the row limit exceeded, this is a special form of exception called a RowLimitException and demands the operation
be retried with a great row limit. It is possible to improve on this but because of the performance optimizations
of Zap Cubes its harder than it sounds.
 1. Zap Cubes have a HashMap style structure with a set of _buckets_ that each potentially point to a linked list of
_rows_. This provides a lower complexity order for all operations.
 1. Zap Cubes define a multi-dimensional model and as such  inherently track a set of _aggregations_ and a set of _dimensions_.  Both
have specific semantics associated with them.
 1. Zap Cubes are append-only in terms of growth. New dimensional buckets or rows are added at the end of the cube.
 1. Zap Cube data transfers (serialization/deserialization) requires only transferring the number of rows actually 
created during updates. The append only semantic enables this.
 1. Zap Cubes are (snappy) compressed/inflated during transfers.
 1. Zap Cubes store __all__ aggregation and dimension values as 64 bit __primitives__. There are conversion routines to and
from the standard
set of Brio Types in the standard byte sizes. This is to reduce the complexity of the numerical routines. The set of types 
supported is
the full Brio datatype set including dictionary keys for strings.
 1. Zap Cubes are designed to be used in a recursive tree traversal engine overlayed on top of a complex object model tree.
The merging of results
from children objects into parent objects is done following very specific __join__ rules that to some degree 
mimic a __cross join__. The mechanics of this are the _secret sauce_ for the whole Burst architecture.
These semantics support the __single-pass__ semantics that Burst provides for a general purpose, arbitrary aggregation, 
arbitrary 
dimension numerical engine with only a single scan required. This is why low GC pressure and fast numerics are so 
critical. Typically, during a scan a very large number of Zap Cube instances are generated per-Fabric Item traversal.

#### Cube Datatypes and Primitives
Cubes support all Brio datatypes i.e _Boolean, Byte, Short, Int, Long, Double, String_. Strings are 
represented by keys in Dictionaries. All datatypes are supported internally using 64 bit __primitives__. We convert 
back and forth between them during import/export.

#### Results Model
Cubes, which can be thought of much like a SQL __TABLE__ are a set of __Tuples__ each of which 
can be thought of as a SQL __Row__ in the table. Each Tuple in the Cube has a set of __Dimension__ 
columns and a set of __Aggregation__ columns. The __Dimension__ columns can be thought of very 
much like __GROUP BY__ columns in a SQL result set. The __Aggregation__ columns
can be thought of as the regular columns in the SQL result set. Each Tuple is uniquely identified
by the values in its set of __Dimension__ columns. 

##### Dimensions
The dimensions can be configured per column into a number of different _bucketing/grouping_ semantics. 
These semantics are extensible but loosely fall into the following categories

* Time Dimension
* Explicit Range Dimensions (Splits/Enums)

##### Aggregations
The aggregations can be configured per column into a number of different _counting_ semantics. These
semantics. These semantics are extensible but for the time being are:

* Sum
* Count
* Uniques
* TopK

#### Aborts
TBD

#### Row Limits
TBD

#### Nesting
TBD

#### Joining
TBD

#### Merging
TBD


### Binary Encoding
The off heap structure of a Zap Cube looks like:

#### Top Level Zap Cube
| Field | Type | Description |
|---|---|---|
| Buckets | Array[Int] | An array of integer offsets into cube memory that are the first row in the bucket list. Zero for empty bucket  |
| Rows | Array[Byte] | A variable size array of bytes that contain a set of Zap Cube Rows |

#### Each Row
| Field | Type | Description |
|---|---|---|
| Null Map | Long| An Long value containing a bit to flag null state for each dimension/aggregation column  |
| Dimensions | Array[Long] | A variable size array of Longs one for each dimension in the schema |
| Aggregations | Array[Long] | A variable size array of Longs one for each aggregation in the schema |
| Next | Int | An integer offset from the beginning of the zap cube pointing to the next row.  |



---
------ [UP](../readme.md) ---  [HOME](../../readme.md) --------------------------------------------
