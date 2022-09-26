namespace * org.burstsys.agent.api

include "agentTypes.thrift"
include "agentAnalysis.thrift"
include "agentResults.thrift"
include "agentCache.thrift"


/***********************************************************************************
    The Burst thrift group analysis execution API IDL
    This is where 'clients' of the Burst query engine use thrift clients to
    contact a Burst ''supervisor'' to initiate a group analysis request or perform other
    management operations such as ''cache'' operations.
***********************************************************************************/
service BurstQueryApiService {

    /*
     * Execute a group analysis
     */
    agentResults.BurstQueryApiExecuteResult groupExecute(
        1: optional agentTypes.QueryGroupUid groupUid
        2: required agentAnalysis.QueryLanguageSource source
        3: required agentAnalysis.BurstQueryApiOver over
        4: optional agentAnalysis.BurstQueryApiCall call
    )

    /*
     *  Execute a data cache management operation
     */
    agentCache.BurstQueryCacheGenerationResult cacheOperation(
        1: optional agentTypes.QueryGroupUid groupUid
        2: required agentCache.BurstQueryCacheOperation operation
        3: required agentTypes.BurstQueryCacheGenerationKey generationKey
        4: optional list<agentCache.BurstQueryCacheOperationParameter> parameters
    )

    /*
     *  Execute a data cache information fetch
     */
    agentCache.BurstQuerySliceGenerationResult sliceFetch(
        1: optional agentTypes.QueryGroupUid groupUid
        2: required agentTypes.BurstQueryApiGenerationKey generation
    )

}

