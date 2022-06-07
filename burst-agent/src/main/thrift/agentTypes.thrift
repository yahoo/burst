namespace * org.burstsys.agent.api

/*******************************************************************************
    common types
 *******************************************************************************/

/*
 * all requests to the Agent API return a status that needs to
 * be checked as part of processing the response. New values should be added at the end
 */
enum BurstQueryApiResultStatus {
   BurstQueryApiUnknownStatus, // eh?
   BurstQueryApiSuccessStatus, // sokay
   BurstQueryApiTimeoutStatus, // query timed-out
   BurstQueryApiExceptionStatus, // request threw exception
   BurstQueryApiInvalidStatus, // request not accepted as valid
   BurstQueryApiNotReadyStatus, // system not currently fully up (or its crashing!)
   BurstQueryApiNoDataStatus, // request had no data (the target dataset was empty)
   BurstQueryApiStoreErrorStatus // error loading data from store
   BurstQueryApiInProgressStatus, // request still in flight
}

/*
 * TODO
 */
enum BurstQueryApiLoadMode {
    UnknownLoad,
    ErrorLoad,
    NoDataLoad,
    ColdLoad,
    WarmLoad,
    HotLoad
}

////////////////////////////////////////////////////////////////////////////////
// REQUEST DATATYPES
////////////////////////////////////////////////////////////////////////////////

typedef string QueryStatusMessage
typedef string QueryGroupUid
typedef i64 QueryTally
typedef i64 QueryDomainKey
typedef i64 QueryViewKey
typedef i64 QueryGenerationClk
typedef i64 QueryElapsedMs
typedef i64 QueryElapsedNs

////////////////////////////////////////////////////////////////////////////////
// RESULT GROUPS
////////////////////////////////////////////////////////////////////////////////

/*
 * A representation of a fabric generation.
 * Used for making queries, so fully specified
 */
struct BurstQueryApiGenerationKey {
    1: required QueryDomainKey domainKey
    2: required QueryViewKey viewKey
    3: required QueryGenerationClk generationClock
}

/*
    A representation of a fabric generation.
    Used for cache operations, any unspecified fields are assumed to match all values.
    Fields must be provided from least specific to most specific.
    e.g. if viewKey is provided then domainKey MUST BE provided
 */
struct BurstQueryCacheGenerationKey {
    1: optional QueryDomainKey domainKey
    2: optional QueryViewKey viewKey
    3: optional QueryGenerationClk generationClock
}



