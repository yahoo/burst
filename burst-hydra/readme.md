![Burst](../doc/burst_small.png "")
--

![](./doc/hydra.png "")

___Hydra___ is the distributed execution language that is used by higher level query
languages e.g. [EQL](../burst-eql/readme.md), and brave
programmers directly to execute distributed parallel _(multi-node/multi-core)_
behavioral analysis. Hydra is the `string representation` of
[Felt](../burst-felt/readme.md) along with the request and result processing
front end. For the overall semantics of building an analysis
and the more gory details of the Felt object model, please refer to the
Felt documentation. Here
we will focus on issues of request processing, source language  constructs,
execution, and result processing.

* [Requests](doc/execution/requests.md)
* [Language](./doc/language.md)
* [Execution](doc/execution/execution.md)
* [Results](doc/execution/results.md)


#### Configuration
|  system property |  default |  description |
|---|---|---|
|  burst.hydra.parser.count |  4 |  pool size for hydra parsers  |



---
------ [HOME](../readme.md) --------------------------------------------
