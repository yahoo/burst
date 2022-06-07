![Burst](../doc/burst_small.png "") ![](./felt_small.png "")
--

# Schema
For each Brio schema presented to it, Felt can generate a ``Felt Schema`` that contains
all reusable, schema dependent artifacts. This includes the basic schema DFS traversal including
constants for all schema lookups, runtime axis management, and the basic ``sweep`` traversal event listener
API. The Felt Schema generates to quite a few lines of __Scala__ code. Note that this is one only once during
a VM lifetime for each Brio Schema and hence as much as possible is placed into this code generation.


---
------ [UP](../readme.md) ---  [HOME](../../readme.md) --------------------------------------------
