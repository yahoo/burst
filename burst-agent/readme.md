![Burst](../doc/burst_small.png "")

![](./doc/agent.png "")

___Agent___ is the external client API supporting analytic query execution requests and
result handling data model. Also provided are various ancillary functions such as cache
manipulation. An Agent server is deployed/hosted in each [Master](../burst-master/readme.md) deployed.

#### Thrift API
The relevant Thrift types and service endpoints are
defined in  [BurstAgentApi.thrift](src/main/thrift/agentService.thrift)

## Model
The Agent implements execution/result types and basic semantics as defined
in the [Fabric](../burst-fabric/readme.md) module. These types are
used throughout the Burst ecosystem, however the Agent is where
these data structures are accessible via external APIs.
The Burst Master hosts the Agent server and the Agent client is used internally
and made available externally.


#### Configuration
|  system property |  default |  description |
|---|---|---|
|  burst.agent.api.host |  getPublicHostAddress |  interface to bind Thrift API  |
|  burst.agent.api.port |  37000 |  port to bind Thrift API  |
|  burst.agent.api.timeout.ms |  5 minutes |  Thrift API timeout (in ms)  |
|  burst.agent.server.connect.life.ms |  infinite |  Thrift API server connection lifetime (in ms)  |
|  burst.agent.server.connect.idle.ms |  infinite |   Thrift API server connection idle (in ms)   |


## Endpoints
Agent exposes the following Thrift endpoints.

##### Group Execute
Execute a group query. This could be [Eql](../burst-eql/readme.md),
or [Hydra](../burst-hydra/readme.md) language source text with optional parameters as well as
an identifying GUID for troubleshooting.
Results are in a form compatible
with the [Fabric](../burst-fabric/readme.md) results model

	BurstQueryApiExecuteResult groupExecute(
		1: required QueryLanguageSource source
		2: required BurstQueryApiOver over
		3: optional QueryGroupUid groupUid
		4: optional BurstQueryApiParameterization parameters
	)

##### Cache Search
Search the Fabric cache for information about loaded data.

	/*
	 *  search the cache for information on generations
	 */
	BurstQueryApiCacheResult cacheSearch (
		1: required BurstQueryApiGenerationKey generationKey
	)

##### Cache Evict
Evict a loaded dataset from memory.

	/*
	 * Remove all generations for a view from memory cache
	 */
	BurstQueryApiCacheResult cacheEvict(
		1: required BurstQueryApiGenerationKey generationKey
	)

##### Cache Flush
Flush a loaded dataset from memory.

	/*
	 * Remove all generations for a view from memory and disk cache
	 */
	BurstQueryApiCacheResult cacheFlush(
		1: required BurstQueryApiGenerationKey generationKey
	)




---
------ [HOME](../readme.md) --------------------------------------------
