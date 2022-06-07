namespace * org.burstsys.catalog.api

include "catalogTypes.thrift"

enum BurstCatalogApiStatus {
   BurstCatalogApiSuccess,      // sokay
   BurstCatalogApiTimeout,      // meta-data operation timed-out
   BurstCatalogApiException,    // meta-data operation threw exception
   BurstCatalogApiInvalid,      // meta-data operation not accepted as valid
   BurstCatalogApiNotReady,     // system not currently fully up (or its crashing!)
   BurstCatalogApiNotFound      // The requested entity was not found using the specified identifier.
}

struct BurstCatalogApiResult {
  1: required BurstCatalogApiStatus status
  2: required string message = "OK"
}

struct BurstCatalogApiEntityPkResponse {
  1: required BurstCatalogApiResult result
  2: optional catalogTypes.BurstCatalogApiKey pk
}
