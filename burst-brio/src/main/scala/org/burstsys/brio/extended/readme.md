![Burst](../../../../../../../..//../documentation/burst_h_small.png "")
![](../../../../../../../../doc/brio_small.png "")

# `Brio Extended Types`

---
_Proposal for more efficient encoding of real world data in Brio blobs using additional 
type system features_

---

This is a skeletal proposal for the Brio type extensions.
These extensions promise to make Brio Blob more efficient
for real world data such as the application data that Flurry stores 
routinely. Applying VUE types has the potential to reduce Blob size 
to as much as 20% of original for Sessions and to 18% of original for 
Events where parameters do not dominate. Structural subtyping may 
have similar levels of compression for upcoming features such as crash, 
revenue, and standard events in general where different types of events 
vary widely in the quantity and types of fields.

## `Variable Unsigned Encoding (VUE)`
VUE types are stored as unsigned values in variable length arrays of bytes. 
The idea is to have the underlying byte storage as compressed and dense as 
possible, but have the presentation at the data access layer (as used during 
query scans) to be as query friendly as possible. Though the underlying
storage format is manipulated as inherently unsigned, the presentation as a 
Java or Scala primitive is signed.  This means the  numeric range as 
presented by a Java Long is never more than a Long primitive’s positive
signed range  (0 through 263- 1). The underlying primitive type is stored 
as signed bytes for compatibility with Java/Scala code but is converted to
and from that form on the fly before presentation in the Brio runtime layer.
In addition, each of these numbers are stored in a variable resolution. 
This means some number of LSBs are truncated on read, and restored as
zeros when read. All encoding/decoding and conversations for writing 
and presentation at the read layer are done automatically and efficiently
in native memory (off heap) storage/accessors.

## `Type`
|  types |  bytes |  Valuemax= 2 (8 * bytes ) - 1  |
|---|---|---|
| unsigned byte | 1 | 255 |
| unsigned short | 2 | 65,535
| unsigned trio | 3 |  16,777,215
|  unsigned integer | 4 | 4,294,967,295
|  unsigned quintet |   5 |   1,099,511,627,775
|  unsigned sextet |   6 |   281,474,976,710,655
|  unsigned septet |   7 |   72,057,594,037,927,939
|  unsigned long |   8 |   9,223,372,036,854,775,807

## `Examples`
### `Time values`
We often store time values as epoch time at millisecond resolution in 8 byte longs.  This is inefficient in the following ways:
We generally do not actually need say millisecond resolution though native formats support that.
Any given Brio Blob is generally limited to a very small time-frame lookback  e.g. two years or 30 days. Having a per Brio Blob time start offset in its header and only recording times as an offset from that is much more efficient.

### `DB Primary Keys`
We often store database row primary keys in 8 bytes e.g. Flurry event ids. There may be only a few hundred of any given event id for a particular user in a particular application.  If we stored a lookup table in the header of the Brio Blob, we could reduce that 8 bytes to one (255 different events) or two bytes (65,535 different events).

### `Elastic Vue Datatype`
<pre>
elastic_datatype := 'Elastic' '('  bytes ',' resolution ',' offset ')'
bytes := the number of bytes of storage
resolution := the number of bits to truncate in the LSBs
offset := the name of the encoded long offset for the values in a blob
</pre>
  
<pre>
structure YourStructure {
    0) "field1" : String  key
    1) "field2" : Elastic(3, 1000, "StartOffset")
}
</pre>

Stored in VUE encoding

## `Lookup Vue Datatype`
<pre>
lookup_datatype := 'Lookup' '('  bytes ','  lookup ')'
bytes := the number of bytes of storage
lookup := the name of the per blob lookup table for the values
</pre>

<pre>
structure SomeStructure1 {
    0) "field1" : String  key
    1) "field2" : Lookup(3, "LookupTable")
}
</pre>

## `Value Vector`

<pre>
structure SomeStructure2 {
    0) "field1" : Collection[Long]  value_vector1
    1) "field2" : Collection[Elastic(1, "foo")]  value_vector2
}
</pre>

These are stored as normal Brio datatypes or optionally as VUE types. (Extra credit to allow for bitmapped value vectors)

## `Structural Subtyping`

<pre>
structure SuperType {
    0) "field1" : String  key
}
</pre>

<pre>
// SubType will have both field1 and field2
structure SubType extends SuperType {
    1) "field2" : Boolean flag // the names and numbers must not collide
}
</pre>

The support in Brio for versions will be extended to include subtyping 
since they are essentially the same encoding challenge and share much of 
the same set heterogeneity challenges and solutions. You can think of the
super type as version one with all subtypes being additional versions. The
same rules we apply for versions will apply for subtypes.
All field names and numbers must be non-conflicting. The entire type hierarchy 
must enforce unique names and numbers for fields.
You will always be able to write queries for fields that exist in super types. 
If the field is in a subtype that the iterative member is not a type of, the 
reference will return NULL. (note this last sentence is a big deal - more 
discussion needed.
There will be a test for the ‘type’ of a reference during visits. This can be
used to skip or execute alternate Felt code for different subtypes.
We will add a similar test for ‘version’ of a reference during visits.
Note that mixing of subtypes and versions is not going to be pretty or easy. 
It will work however.


## `TODO`
* change all ZapMemoryOffset to be Ints (BurstMemoryOffset)
* map codec send only values and rehash on remote size?
---
------ [HOME](../../../../../../../../../readme.md) -------------------------------------------- 
