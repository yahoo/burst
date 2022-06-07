namespace * org.burstsys.catalog.api

include "catalogTypes.thrift"
include "catalogResponse.thrift"


struct BurstCatalogApiDomain {
  1: required catalogTypes.BurstCatalogApiKey pk
  2: required catalogTypes.BurstCatalogApiMoniker moniker
  3: required map<string, string> domainProperties
  4: optional catalogTypes.BurstCatalogApiUdk udk
  5: optional map<string, string> labels
  6: optional catalogTypes.BurstTimestamp createTimestamp
  7: optional catalogTypes.BurstTimestamp modifyTimestamp
}

struct BurstCatalogApiView {
  1: required catalogTypes.BurstCatalogApiKey pk
  2: required catalogTypes.BurstCatalogApiMoniker moniker
  3: required catalogTypes.BurstCatalogApiKey domainFk
  4: required catalogTypes.BurstCatalogApiKey generationClock
  5: required map<string, string> storeProperties
  6: required string viewMotif
  7: required map<string, string> viewProperties
  8: optional map<string, string> labels
  9: required string schemaName
  10: optional catalogTypes.BurstTimestamp createTimestamp
  11: optional catalogTypes.BurstTimestamp modifyTimestamp
  12: optional catalogTypes.BurstTimestamp accessTimestamp
  13: optional catalogTypes.BurstCatalogApiUdk udk
}


struct BurstCatalogApiDomainResponse {
  1: required catalogResponse.BurstCatalogApiResult result
  2: optional BurstCatalogApiDomain domain
  3: optional list<BurstCatalogApiDomain> domains
}

struct BurstCatalogApiViewResponse {
  1: required catalogResponse.BurstCatalogApiResult result
  2: optional BurstCatalogApiView view
  3: optional list<BurstCatalogApiView> views
}

struct BurstCatalogApiDomainAndViewsResponse {
  1: required catalogResponse.BurstCatalogApiResult result
  2: optional BurstCatalogApiDomain domain
  3: optional list<BurstCatalogApiView> views
}
