# Base Samplestore

The base samplestore builds on the [burst-samplesource](../burst-samplesource/readme.md) with more concrete implementation of a samplestore 
that builds on the Fabric topology used by the Cell supervisor and workers.
This is a simple samplestore that is used for testing
and as a base for other samplestores.  It does the heavy lifting managing the topology of scalable number of workers
and the communication between them.  It uses the [samplesource](../burst-samplesource/readme.md) which is the ETL pipeline each
individual worker.

## Scanning Samplesource 
A scanning samplesource is a samplesource that simply iterates over the items it is transforming to Burst Brio instances. 
It abstracts the details of setting up a scan and iterating over each item and manages the interaction with the Cell workers,
the pressing of the items into Brio instances, and the sending of the Brio instances back to the Cell workers.  It is
the base class for the [synthetic samplestore](../burst-synthetic-samplestore/readme.md) and an internal Hbase scanner used by
Yahoo.
