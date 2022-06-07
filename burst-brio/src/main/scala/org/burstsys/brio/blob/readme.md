![Burst](../../../../../../../..//../doc/burst_small.png "")
![](../../../../../../../../doc/brio_small.png "")

![Burst](./blobs.png "")

___Blobs___ are  a binary encoding format for the data needed 
to traverse a single Brio object tree.
This includes some header information, the dictionary for the
complex object tree, and the complex Brio object tree itself.
They contain a Brio encoded object tree, but __are not__ Brio objects per se.

###### `Input to scanner functions`
A Blob represents
a single `Item` for the basic Burst execution model `scanner(blob) => result`. All Burst queries consist
of iterative/parallel execution of scanner functions on Blob instances.

###### `Immutable, identical memory/disk format, zero object churn`
A Blob once pressed, is immutable. All data structures have identical format on disk and in memory. All
operations on Blob data structures create no new objects at run time. Blobs on disk can be `mmap'd`
directly into memory and scanned directly.

# `Encoding Formats`
###### `V2 Encoding`
(_without elastic types_)

| Field | Type | Description |
|---|---|---|
| blob encoding version | Int | __'2'__  |
| root object version | Int | root structure schema version |
| dictionary size | Int | dictionary size in bytes |
| dictionary data | Array[Byte] | the byte data for the dictionary |
| root object size | Int | encoded object-tree size in bytes |
| root object data | Array[Byte] | encoded object-tree data |

###### `V3 Encoding`
(_with elastic types_)

| Field | Type | Description |
|---|---|---|
| blob encoding version | Int | __'3'__  |
| root object version | Int | root structure schema version |
| lookup-table size | Int | lookup-table table size in bytes |
| lookup-table data | Array[Byte] | lookup-table data |
| offset-table size | Int | offset-table size in bytes |
| offset-table data | Array[Byte] | byte data for the offset-table |
| dictionary size | Int | dictionary size in bytes |
| dictionary data | Array[Byte] | byte data for the dictionary |
| root object size | Int | encoded object-tree size in bytes |
| root object data | Array[Byte] | encoded object-tree data |

---
------ [HOME](../../../../../../../../../readme.md) -------------------------------------------- 
