namespace * org.burstsys.samplestore.api

/*****************************************************/
/* Sample Store SERVICE */
/*****************************************************/

enum BurstSampleStoreApiRequestState {

    /* request sokay */
   BurstSampleStoreApiRequestSuccess,

   /* request timed-out */
   BurstSampleStoreApiRequestTimeout,

   /* request threw exception */
   BurstSampleStoreApiRequestException,

   /* request not accepted as valid*/
   BurstSampleStoreApiRequestInvalid,

   /* system not currently up, or going down (or crashing!) */
   BurstSampleStoreApiNotReady
}

/*
 * request status
 */
struct BurstSampleStoreApiRequestContext {
  1: required string guid // global operation UID
  2: required BurstSampleStoreApiRequestState state = BurstSampleStoreApiRequestState.BurstSampleStoreApiRequestSuccess
  3: required string message = "OK"
}

/*
 * data transfer endpoint
 */
struct BurstSampleStoreApiDataLocus {
    1: required string suid
    2: required string hostAddress
    3: required string hostName
    4: required i32 port
    5: required map<string, string> partitionProperties
}

/*
 * list of data transfer endpoints
 */
struct BurstSampleStoreApiViewGenerator {
    1: required BurstSampleStoreApiRequestContext context
    2: required string generationHash // unique hash for this generation instance to compare to previous ones
    3: optional list<BurstSampleStoreApiDataLocus> loci
    4: optional string motifFilter
}

/*
 * equivalent to FabricDomain
 * TODO remove after fabric does not depend on spark and use FabricDomain directly
 */
struct BurstSampleStoreDomain {
    1: required i64 domainKey
    2: required map<string, string> domainProperties    // the domain properties
}

/*
 * equivalent to FabricView
 * TODO remove after fabric does not depend on spark and use FabricView directly
 */
struct BurstSampleStoreView {
    1: required i64 viewKey
    2: required string schemaName                       // the brio schema name
    3: required string viewMotif                        // the view definition
    4: required map<string, string> storeProperties     // the store properties
    5: required map<string, string> viewProperties      // the view properties
}

/*
 * equivalent to FabricDataSource
 * TODO remove after fabric does not depend on spark and use FabricDataSource directly
 */
struct BurstSampleStoreDataSource {
    1: required BurstSampleStoreDomain domain
    2: required BurstSampleStoreView view
}

