![Burst](../doc/burst_small.png "") ![](../../doc/hydra_small.png "")
--
     

![](../../../burst-brio/src/main/scala/org/burstsys/brio/dictionary/dictionaries.png "")
--
[Brio Dictionaries](../../../burst-brio/doc/dictionaries.md) are the way that
_text_ words and phrases are stored and accessed in the Hydra [data plane](data.md).
Dictionaries are bidirectional in that they
associate a `short` key with each word or phrase that can be used
to lookup a word or phrase and the other direction.

What this means for Hydra is mostly hidden from the author since you do not have to
use these keys in the Hydra source language anywhere. However what you might notice
is that it is not easy currently to perform operations on a substring or 
use a regular expression to match one. 

##### Mutable & Static Forms
Dictionaries come in two forms and have two different purposes; _Mutable_ and _Static_.

###### Mutable (output) Form
Dictionaries that can be updated are used during the _pressing_ of Brio _Blobs_ and while
updating _Cubes_ and return the results to the analysis client. When a Hydra expression
writes a string to a Cube, that is updating a Mutable dictionary associated with that Cube.

###### Static (input) Form
Dictionaries that do not need to be updated or _read-only_ dictionaries are embedded 
directly into Brio Blobs and can be stored directly on disk or sent over streams etc.
Each input Blob has a single static dictionary.


---
------ [UP](../readme.md) ---  [HOME](../../readme.md) --------------------------------------------
