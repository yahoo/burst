![Burst](../doc/burst_small.png "")
--

![](./doc/nexus.png "")

```'a connection or series of connections linking two or more things...'```

___Nexus___ is a parallel streaming data xfer protocol meant to support large scale transfers of data from a set of nodes
in one cluster to a set of nodes in another cluster. By large scale we mean Nexus supports connectivity matrices with
lots of clients, servers, and parallel stream transfers each ranging from a few hundred megabytes to a few terabytes.
Nexus is a asynchronous, multi-threaded pipelined protocol written using Netty 4.1 and leveraging pre-existing TCP
protocols and  network fabric. It is a duplex protocol that starts with a client to server bind and accept, followed
by a stream request with some opaque stream specification, a variable length series of stream data chunks, along with
simple progress, error, and final status messages where appropriate.

## Configuration
|  system property |  default |  description |
|---|---|---|
|  burst.nexus.client.threads |  Runtime.getRuntime.availableProcessors |  the fixed client thread pool size  |
|  nexus.client.pool.stale.ms |  1 hour |  age out time for nexus clients  |
|  burst.nexus.client.cache.tender.ms | 1 hour |  frequency to check for stale clients  |
|  burst.nexus.server.threads |  Runtime.getRuntime.availableProcessors |  the fixed server thread pool size  |
|  burst.nexus.server.port  |  1270  |  server port  |
|  burst.nexus.pipe.size |  1E2 |    |
|  burst.nexus.stream.timeout.ms |  60 seconds |  the timeout for stream operations  |
|  burst.nexus.stream.parcel.packer.concurrency |  2  |  The number of parcel packers per stream  |



#### Metrics
|  name | type | description |
|---|---|---|
|  nexus.client.gauge | gauge | tally of active clients  |
|  nexus.server.gauge | gauge | tally of active servers  |
|  nexus.client.connection.gauge | gauge | tally of active client connections  |
|  nexus.server.connection.gauge | gauge | tally of active server connections  |
|  nexus.press.items.rate.meter | meter | item press item rate  |
|  nexus.press.bytes.rate.meter | meter | item press bytes rate  |
|  nexus.press.reject.rate.meter | meter | item press reject rate  |


#### Connection Matrix
Nexus can be thought of as a (C x P x S) connectivity matrix where (0 through C) clients will initiate and transfer
(0 through P) parallel concurrent streams of data from (0 through S) different servers. In practical terms since not
all clients are talking to all servers at the same time and there is a practical limit to the number of
parallel concurrent requests active in the system at any given moment, this number is mercifully not infinite.
However it is imperative, that the Nexus protocol use resources such as connections and memory sparingly as well
as sharing and caching where ever possible.

#### Macro Level Stream Operations
The Nexus protocol is, at a macro level, a combination of client->server stream transfers taking place in parallel in
order to serve a higher level data exchange operation. We would like to have the ability to use this macro level
operation identity in order to do the following:

1. Cancel all streams
1. Progress across streams
1. Errors across all streams

It is not totally clear if this is too ambitious...

#### Sharing and Caching
We want to enforce the following constraints

1. Between any given client JVM and server JVM there is only one _'connection'_ that means there can be multiple
clients in the sense that concurrent access to the server connection can be manifest in the runtime, but all
client streams are sent across the same _accepted_ socket from client to server.
1. There is only one server in a given runtime. All client connections/streams bind to this one server. Each client
has its own accepted client socket.
1. Client side JVMs have a cached set of _'client'_ objects connected to a given server endpoint. These client objects
are returned to pools when streams are finished and can be picked up by other threads as needed. If a given client
object becomes stale in the LRU sense, they are closed and the resources are freed.

#### Protocol
A nexus stream transfer goes through the following states:

1. __BIND__: Nexus Client on a local cluster worker node _'binds'_ to a remote Nexus Server on a remote cluster worker
node. Note the server doing the binding is one per JVM as discussed above.
1. __ACCEPT__: Nexus Server on remote worker 'accepts' this connection. A new client socket is created. Note that
on the client side these accepted server connections are cached as discussed above.
1. __STREAM INITIATE_REQUEST__: Nexus Client on local cluster sends data stream _'request'_ with an associated 'paradata' string,
and a 'queue' for recieved data to remote Nexus Server.
1. __STREAM_INITIATE_REPONSE__: Nexus Server receives this stream request and if it finds the paradata to be valid it goes
into stream mode to this client else if the paradata is not valid or if there is some other problem with satisfying
the request, it will send an 'error' response back to the remote client. This ends the lifecycle.
1. __STREAM_CHUNK_SEND__: Once the stream mode is started, the Nexus Server starts drawing data from the queue associated
with this stream (passed via the listener interface) and sending chunks back to the Nexus Client. When the stream
is complete the server side datasource must place a special _'end of stream'_ object onto the queue.
1. __STREAM_CHUNK_RECEIVE__: Once the stream mode is started, the Nexus Client starts placing received data onto the queue
it was passed as part of the stream request. When the stream is complete, a special _'end of stream'_ object is placed
onto the queue.
1. __STREAM_PROGRESS_UPDATE__: Interspersed with data stream chunks, are 'progress' responses which indicate relative level
of completion of the stream transfer.
1. __STREAM_ERROR_UPDATE__: If at any time during the stream transfer, the server detects an error it sends an _'error'_
response to the client, terminates the stream and cleans up any associated state.
1. __STREAM_CANCEL_REQUEST__: If at any time during the stream transfer, the client detects an error, it can send a _'cancel'_
request to the server. The server then terminates the stream and cleans up any associated state.
1. __STREAM_CANCEL_RESPONSE__: Sent from the client to the server when the _'cancel'_  request has been executed.
1. __STREAM_COMPLETION_UPDATE__: Once the Nexus Server gets an 'end of stream' marker on the remote queue,
a _'completion'_ message is sent from server to client. This terminates the stream transfer.



---
------ [HOME](../readme.md) --------------------------------------------
