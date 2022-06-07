![Burst](../../../../../../../..//../doc/burst_small.png "")
![](../../../../../../../../doc/brio_small.png "")

![Burst](./dictionaries.png "")

___Dictionaries___ are a zero object churn off-heap data structure for
efficiently storing string values. All strings are encoded as `Short` Keys.
Dictionaries supports  `key->string`, and `string->key` bidirectional
mapping.

###### Mutable and Immutable Forms
Dictionaries can be __mutable__ as is needed for Brio pressing, and
for runtime result collection. Dictionaries are also available in an
__immutable__ or __static__ form as is needed for brio encoded data.

###### Memory Layout
| Field | Type | Description |
|---|---|---|
| Encoding Version | Int | The version of the dictionary encoding (future) |
| Word Count | Int | the count of words in the dictionary |
| Pool Id | Int | The pool this dictionary was sourced from |
| Next Slot Offset | Int | the location of the end of this dictionary (available for more slots to be added) |
| Buckets | Array[Int] | the set of buckets for this dictionary (each bucket is an offset from the start of the dictionary) |
| Slots | Array[Byte] | the variable length field that contain dictionary slots (words) |


---
------ [HOME](../../../../../../../../../readme.md) -------------------------------------------- 
