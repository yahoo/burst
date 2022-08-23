![Burst](../../../documentation/burst_small.png)

# `Burst Runtime Architecture`

![](../../../image/burst_runtime.svg)

The Burst runtime topology is a fairly standard single-master, multiple-worker 
cluster that is called 
a _Cell_.  It managed requests from clients and imports data from one or more
SampleStores.

## Cell Runtime Components
The top level elements in a Cell are:

1. Master Node
2. Worker Node(s)
3. Metadata Catalog
4. Client Protocol
5. Fabric Protocol
6. SampleStore Protocol
7. Nexus Protocol
8. Data Caches
9. SampleStore Instance

#### Master Node Responsibilities
There is a single Master node. It is conceptually possible to have multiple masters,
and in fact the team toyed with that idea since there are some workloads that
_can_ bottleneck during master node processing, but the added value was 
not worth the attendant
implementation complexity _for the current use cases_. 

The Master node is responsible for:
1. destination for incoming requests from clients via client protocol
2. pre-processing of the requests
3. scattering processed requests out to worker nodes via Fabric protocol
4. gathering processed results from the worker nodes via Fabric protocol
5. returning results to the clients via client protocol
6. managing metadata via the Catalog via JDBC protocol

#### Worker Node Responsibilities
There are one or more Worker nodes. Each is responsible for:
1. Receiving incoming (scatter) requests from the Master node via the Fabric protocol.
2. Fetch/Load data through the Data Cache, using samplestore protocol
as appropriate for the request
3. process requests into execution scans that produce results
4. send results back to Master node (gather) via the Fabric protocol

#### The Catalog
The Catalog is a relational store for the metadata that is used throughout
the Burst ecosystem. The primary use is to define datasets and how/where they
are located and fetched. Burst uses a very simple JDBC interface to a very 
simple relational model. MySql and Derby are currently supported out of the box
but due to the dead simple model used, it is considered easy to port this to
just about any SQL DBMS.

#### Cell Deployment
The Burst codebase does not dictate or provide for any specific form of deployment
for the Master and Worker nodes. Master and Worker nodes can be
started up via JVM jar files that read traditional
_property_ based configurations as well as a few command line parameters. Burst Cells have
been deployed via standalone scripts, chef, and currently kubernetes.

## SampleStore Runtime Components
Any given Burst Cell can import data from one or more SampleStore instances. The SampleStore
can be thought of as being separated into a single Master role along with one or more Worker
roles. These can coexist on the same runtime deployment process or container or be on
separate contexts where it is valuable to import data via massive parallelism
where you want to distribute the Worker rolee across many nodes e.g.
a store such as HBASE.
Its responsibilities are:
1. Receive incoming dataset fetch requests from Cell Master node via SampleStore protocol
2. 


