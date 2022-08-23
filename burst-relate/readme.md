![Burst](../documentation/burst_h_small.png "")
--

![](./doc/relate.png "")

___Relate___ is a simplified JDBC oriented persistence. We rely on
[scalikejdbc](http://scalikejdbc.org/) for much of the implementation.

## Simplified
We do not intend this module to be a sophisticated solution to
complicated persistence problems. Rather we want this to be a
simple to use, simple to maintain functionality for
a well considered minimal set of organically performant
use cases that we define to be necessary and sufficient.

* primary high xaction rate use cases are simple table lookups
based on primary key or indexed columns. We eschew joins in anything but
admin or monitoring or other less demanding performance scenarios.
* we try to break down all operations to simple atomic CRUD operations using
a single top level transaction.

## Supported Databases
We have a dialect mechanism to support multiple databases. We currently
support [MySql](https://www.mysql.com/) for production use and
[Derby](https://db.apache.org/derby/) for unit testing. Our schema and
queries are not very advanced so we suspect other mainstream
SQl databases such as postgres might be reasonably
straightforward to use instead.

## Configuration
|  system property |  default |  description |
|---|---|---|
|  burst.relate.debug |  false |  enable sql debugging  |


---
------ [HOME](../readme.md) --------------------------------------------
