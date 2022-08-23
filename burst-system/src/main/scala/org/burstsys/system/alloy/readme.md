![Alloy Sample Source Server](../../../../../../../../../documentation/burst_h_small.png "")
--

___Alloy Source Server___ is an example sample source server that reads Json files, extracts data, transforms it into 
objects and relations, presses them into a datasets and transfers it to a burst cell.

##Purpose
A Burst DBMS is a read only store for behavior analytics queries.  It loads its datasets from an external system of record
using the _sample store API_ defined in the 
[burst-samplestore/burst-samplestore-api](../../../../../../../../../burst-samplestore/burst-samplestore-api/readme.md) module.  
The alloy source server implements the sample store API by building on the sample source skeleton defined in the
[burst-samplestore/burst-samplestore](../../../../../../../../../burst-samplestore/burst-samplesource/readme.md) module.
It reads json files using brio schemas using the alloy reader from the
[burst-alloy](../../../../../../../../../burst-alloy-testing/readme.md) module. 

The alloy source server serves a couple purposes in the burst ecosystem:

* Testing: supporting system level tests checking the interaction of the burst cell with a remote sample source.
  
* Setup: getting a new user of the burst system able to get data into the burst cell.
    
* Teaching: showing a simple sample source that guides a developer on creating a custom sample source.

### Scenarios
There are a number of possible ways we will want to configure the burst cell and the alloy source server:

* Standalone Burst cell and an alloy source with a single worker
* Burst cell with multiple workers and an alloy source with a single worker
* Burst cell with multiple workers and an alloy source with multiple workers
* Burst cell and multiple source servers
* Containerized alloy source that can be scale in a k8s-like deployment.

#Features
* master and worker together, more peer-to-peer
* need a way to find peers, but make it easy to change:  config file, k8 api should be easy to support.
* any master/worker can be contacted to get view information...helps fault-tolerance and with k8 services entry points
    * the initial "master" would need to contact the other "workers" to help find loci?
* use directory structure to group common json files into a loadable dataset
    * notices when files change
    * notices when new files are added

#TODOs
* Support multiple sample source servers in metadata and store


## Questions
* How do we map to "buckets" when there is only a few alloy workers?  fixed size based on property?  there is no negotiation between
cell and store
  * Fragment json files into buckets?
* Do we support multiple sample store servers?
* Motif view statement support for alloy store?
* identifier for common json files?  unique name
* Current sample store assume master can answer view questions without any support to ask the other "workers".  Do we add
  a master->worker API to the skeleton structure to help with this case?

## Configuration

* `alloy.samplestore.load.concurrency`
* `alloy.samplestore.loci.replication`
* `alloy.samplestore.json.rootVersion`
* `alloy.samplestore.stream.skipIndex`
* `alloy.samplestore.location`





---
------ [HOME](../../../../../../../../../readme.md) --------------------------------------------
