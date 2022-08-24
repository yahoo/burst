![Burst](../../../doc/burst_small.png "") ![](../hydra_small.png "")
--

![](language.png "")
--

Hydra is a specialized _data manipulation language_ (__DML__) 
for distributed/multi-core high complexity analytics metrics gathering over
unlimited size datasets. It is not a generalized programming language. There are no
looping constructs except for the inherent scan traversal of the input object-tree data as
defined by the associated Brio ___Schema___. There is no ability 
to access external libraries ()with unplanned for side effects)
 or write code non conformant to the intended 
single minded purpose of  asking a specific type of multi-part, multi-dimensional question
 of a very large data set
and efficiently getting an answer in a very short period of time. Every language feature
as been thoroughly designed and tested to be 100% stable and performant across the entire 
set  of intended use cases.
 
### Language Processing
Hydra analytics processing is  a language pipeline fed by a source language
unit, which executes a a single request/response cycle. 
The pipeline starts by passing the language unit  through a [antlr](http://www.antlr.org/) parser. 
The resulting syntax tree is then
put into a validation/planning/code-generation pipeline. This processed syntax tree is then
regenerated as a _normalized_ Hydra source snippet and distributed to 
all the appropriate worker nodes. The worker node then processes this normalized source
which ends the code-generation on an executable Scala class which is  instantiated into a Scala
 object and cached on the remote worker nodes. This cached instance can then be 
 used/reused to execute one more scans - optionally
with a different _parametrization_ each time.

### Semantic Planes
The Hydra language can be conceptually divided into a set of different
semantic _planes_;  _data_, _control_, _collector_ and _expression_. 

##### Data Plane
The [data](data.md) plane consists of language features that allow the Hydra author
to access the source input data. This access for now is just Brio _Blobs_, but is intended
to grow to include __Crux__ pipelines. The data plane is immutable.

##### Control Plane
The [control](control.md) plane consists of language features that allow the Hydra author
to specify ___where___ in the object-tree traversal scan to collect information from the data
plane 
and place into the collector plane. This is a
visitor model where expressions can be executed at schema defined relationship _visit_ 
points.

##### Collector Plane
The [collector](collector.md)  plane consists of a set of high performance 
mutable data structures that can
be used to efficiently store information collected during the traversal scan. Some of these collectors
are ephemeral in that they exist only for the lifetime of the scan/traversal. 
Others are ___round trip___ lifetime in that they can be returned back to the client that
executed the analysis.

##### Expression Plane
The [expression](expression.md) plane is where the behavior that gets executed at each
control plane visit point during the traversal is defined. Primarily expressions are used 
to define how information is read
from the data plane and placed into collector plane.

---
------ [UP](../readme.md) ---  [HOME](../../readme.md) --------------------------------------------

