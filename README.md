![Burst](documentation/burst_h.png "")

## The Burst Behavioral Analysis Engine

**Burst** is designed to support fast, rich, and flexible behavioral study of enormous and noisy real world event
datasets generated by mobile applications as they are used day to day by their end users over long periods of time. It
was developed at a small mobile application analytics startup called **Flurry**, later bought by **Yahoo**, and has been
serving production scale request workloads (for free!) 24x7 to its customers for many years. It is very good at what it
does and is now available to you as an open source platform.

## Is Burst For You?

We suggest you start any effort to understand Burst and what it does is by first taking a look at how we define
[Behavioral Analysis][behavioral-analysis]. This is there to allow you to quickly understand if your data, and the
questions you want to ask of it, match well what Burst does well. Then we suggest turning to the overview of the
Burst [Data Model][data-model], the Burst [Execution Model][execution-model], and finally the Burst
[Runtime Model][runtime-model]. These high level presentations should help you get a cleaner and deeper sense of what
Burst is, how it works, and how you might envision it working for you. Extra credit to dig into a unique approach that
Burst takes called the [Single Pass Scan][single-pass-scan], as well as high level discussions
of [Performance][performance], [Security][security], and [Sampling][sampling].

## What Burst Is Not

Equally important is to spend a moment clarifying what Burst is _not_...

**a database**

Burst is _not_ a general purpose query engine _nor_ is it a persistent, authoritative, or transactional database. It is
an _online analysis engine_ that scans imported data snapshots. To analyze your data you must first _import_ a dataset
from your data storage system into the memory/disk cache of a suitable Burst compute _Cell_, where you can then run one
or more analysis requests across that data snapshot.

**real time**

Burst does not support what can be considered _real time_ or _streaming_ data access, it does provide services and
protocols that can be used to build efficient, massive parallel import pipelines that can fetch up-to-date data quickly.
The data Burst analyzes will be as current or up to date as the last import done. Burst has features that allow you to
control the time window (lookback) of the 'view' you import.

**conformant SQL**

Burst has a rich front end language called EQL, and where possible we have tried to make that language conform to and
look the same as SQL. However, though the world of behavioral data and questions significantly overlaps the world of
relational data models and relational calculus, EQL and the underlying semantics are simply not the same, _nor are they
intended to be the same_, as SQL and its underlying semantics.

## Prerequisites

In order to stand up a Burst compute cell and use it to analyze your data you will need these basics:

1. **A Burst Compute Cell:** One or more nodes with a Java runtime environment to set up a Burst Supervisor/Worker
process or container topology e.g. [Kubernetes](https://kubernetes.io/). The Burst runtime is distributed either as a
docker container or an executable jar that can be placed into virtual containers or any other packaging/deployment
environment appropriate to your needs. With a few reasonable limits, you can scale Burst horizontally to service
larger datasets and vertically to provide faster computations.
1. **Metadata Catalog:** Burst uses a MySQL DB as a _Catalog_ that stores metadata. For most scenarios this DB does not
need to be particularly high performance though for high/concurrent analysis request rates, it should be able to
provide low latency indexed table lookups
1. **Remote Datasource:** A datasource system/cluster, with access to your data, where the Burst Java remote data import
system endpoint can be stood up. This can be colocated on the Burst compute cell. If you have a parallel (multi-node)
data storage system such as HBASE, the Burst data import system is quite good at spreading remote data feed endpoints
across numerous data nodes.

## Next Steps

If you want to get up close and personal we have a few more steps for you to take...

* [explore Burst using a local cell][exploring]
* [build the Burst source tree][building]
* _(coming soon)_ launch Burst with your schema, your data, and your questions <!-- [link][launchpad] -->

## Digging Deeper

If you are still with us, and you want to understand and/or vette the implementation, we suggest you take a look at the
[individual subsystem documentation][subsystems] and as well as become familiar with
our [external dependencies][dependencies].

---
------ [HOME](./readme.md) --------------------------------------------

[behavioral-analysis]: documentation/behavior/behavioral_analysis.md

[data-model]: documentation/data/data_model.md

[execution-model]: documentation/execution/execution_model.md

[runtime-model]: documentation/runtime/runtime_model.md

[single-pass-scan]: documentation/singlepass/single_pass_scans.md

[performance]: documentation/performance/performance.md

[security]: documentation/security/security.md

[sampling]: documentation/sampling/sampling.md

[subsystems]: documentation/subsystems.md

[dependencies]: documentation/dependencies.md

[building]: documentation/building.md

[exploring]: kubernetes/readme.md

[launchpad]: documentation/launchpad.md
