![Burst](../../../../../../../../../doc/burst_small.png "")
![Burst](../../../../../../../../fabric_small.png "")

 ![](topology.png "")
 
___Topology___ is Fabric support for a  _multi-master_/_multi-worker_ distributed
data processing environment. Topology is designed for cloud/container deployment, specifically
[Kubernetes](https://kubernetes.io/). 

###### Key Principles

* __loose coupling__ -- between masters and workers 
* __multiple-masters__  -- allowed/supported within a given Cell
* __smart workers__  -- processing smarts pushed to workers, high level declarative languge based communication
* __dumb masters__  -- masters focus on worker vectoring and load balancing
* __dynamic worker counts__ -- failure resilience, load balancing
* __dynamic worker cell assignment__ -- cells can share/swap workers
* __centralized persistent store__ -- sql database used for both master and worker configuration, shared
across cells as long as they share the same RDBMS store.
---
------ [HOME](../../../../../../../../../readme.md) --------------------------------------------
