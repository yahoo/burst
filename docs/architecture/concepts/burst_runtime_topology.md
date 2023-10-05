![Burst](../../../documentation/burst_h_small.png)

# `Burst Runtime Architecture`

![](../../../docs/assets/img/tech/burst_runtime.svg)

The Burst runtime topology is a fairly standard single-supervisor, multiple-worker 
cluster that is called 
a _Cell_.  It managed requests from clients and imports data from one or more
SampleStores.

## Cell Runtime Components
The top level elements in a Cell are:

1. Supervisor Node
2. Worker Node(s)
3. Metadata Catalog
4. Client Protocol
5. Fabric Protocol
6. SampleStore Protocol
7. Nexus Protocol
8. Data Caches
9. SampleStore Instance

#### Supervisor Node Responsibilities
There is a single Supervisor node. It is conceptually possible to have multiple supervisors,
and in fact the team toyed with that idea since there are some workloads that
_can_ bottleneck during supervisor node processing, but the added value was 
not worth the attendant
implementation complexity _for the current use cases_. 

The Supervisor node is responsible for:
1. destination for incoming requests from clients via client protocol
2. pre-processing of the requests
3. scattering processed requests out to worker nodes via Fabric protocol
4. gathering processed results from the worker nodes via Fabric protocol
5. returning results to the clients via client protocol
6. managing metadata via the Catalog via JDBC protocol

#### Worker Node Responsibilities
There are one or more Worker nodes. Each is responsible for:
1. Receiving incoming (scatter) requests from the Supervisor node via the Fabric protocol.
2. Fetch/Load data through the Data Cache, using samplestore protocol
as appropriate for the request
3. process requests into execution scans that produce results
4. send results back to Supervisor node (gather) via the Fabric protocol

#### The Catalog
The Catalog is a relational store for the metadata that is used throughout
the Burst ecosystem. The primary use is to define datasets and how/where they
are located and fetched. Burst uses a very simple JDBC interface to a very 
simple relational model. MySql and Derby are currently supported out of the box
but due to the dead simple model used, it is considered easy to port this to
just about any SQL DBMS.

#### Cell Deployment
The Burst codebase does not dictate or provide for any specific form of deployment
for the Supervisor and Worker nodes. Supervisor and Worker nodes can be
started up via JVM jar files that read traditional
_property_ based configurations as well as a few command line parameters. Burst Cells have
been deployed via standalone scripts, chef, and currently kubernetes.

## SampleStore Runtime Components
Any given Burst Cell can import data from one or more SampleStore instances. The SampleStore
can be thought of as being separated into a single Supervisor role along with one or more Worker
roles. These can coexist on the same runtime deployment process or container or be on
separate contexts where it is valuable to import data via massive parallelism
where you want to distribute the Worker role across many nodes e.g.
a store such as HBASE.
At its heart, the sample store is an extraction, transform, and load (ETL) pipeline, optimized for partitoning the task into
a large number of independent parallel shards. Its responsibilities are:
1. Receive incoming dataset fetch requests from Cell Supervisor node via SampleStore protocol
2. Plan the fetch request into a set of parallelizable fetch tasks that untimately will be distributed to the worker nodes.
3. Receive parallel data fetch requests from the Cell worker nodes via SampleStore protocol to each SampleStore workers.
4. Each SampleStore worker fetches the individual items from the data source and presses it into a Brio instance.
5. Each SampleStore worker sends the Brio instance back to the Cell worker via SampleStore protocol.

If you require a SampleStore that is not currently supported, it is easy to implement.  The repository has 


