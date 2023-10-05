![Burst](../documentation/burst_h_small.png "")
--

![](./doc/sampler.png "")

___Sample Store___ is a distributed store for importing data into a Burst Cell. Any given Burst Cell can import data from one or more SampleStore instances. The SampleStore
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

If you require special SampleStore that is not currently supported, it is easy to implement.  

This module containes a number of submodules that implement the various SampleStore pieces:

* [cell side samplestore](./burst-cell-samplestore/readme.md) The cell side of the samplestore.  These are the methods that are called by the cell supervisor and workers to interact with the samplestore. 
* [base-samplestore](./burst-base-samplestore/readme.md) A more concrete implementation of a samplestore.  
  that builds on the Fabric topology used by the Cell supervisor and workers.  It does the heavy lifting managing the 
  topology of scalable number of workers and the communication between them.  It uses the [samplesource](../burst-samplestore/readme.md) which is the ETL pipeline each
  individual worker.
* [samplesource](./burst-samplesource/readme.md) The high level interface for **samplesource** pipeline utilized by a samplestore worker built
  by the [base](./burst-base-samplestore). It includes support for the Nexus protocol used between cell and samplestore workers. 
* [samplestore-api](./burst-samplestore-api/readme.md) The API that is used by the cell supervisor to interact with the samplestor supervisor.
* [synthetic-samplestore](./burst-synthetic-samplestore/readme.md) A samplestore that generates synthetic data.  It is used 
  for testing and as a simple example of a fully functional samplestore implementation.


* [view properties](./doc/view_properties.md)


---
------ [HOME](../README.md) --------------------------------------------
