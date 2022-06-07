![Burst](../doc/burst_small.png "")
--

![](./doc/api.png "")


___Api___ is the common types and functions used by all Burst Thrift APIs.
Bursts uses the [Twitter Finagle Thrift ](https://github.com/twitter/finagle) library along with
[Twitter Scrooge](https://github.com/twitter/scrooge) Thrift endpoint code generation.

## Metrics
These metrics are automatically exposed by all dependent Apis e.g. Catalog,
Agent, SampleStore.

	${apiName} is the value apiName in the API service
	${server/client} is 'client' for client side, 'server' for server side

|  metric name | description |
|---|---|
|  api.${apiName}.${server/client}.connections | current connections  |
|  api.${apiName}.${server/client}.requests | total requests  |
|  api.${apiName}.${server/client}.succeeds | total succeeds  |
|  api.${apiName}.${server/client}.fails | total fails  |
|  api.${apiName}.${server/client}.opens | total opens  |
|  api.${apiName}.${server/client}.closes | total closes  |
|  api.${apiName}.${server/client}.send_bytes | total sent bytes  |
|  api.${apiName}.${server/client}.recv_bytes | total received bytes  |

---
------ [HOME](../readme.md) --------------------------------------------
