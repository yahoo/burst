namespace * org.burstsys.catalog.api

include "catalogTypes.thrift"
include "catalogResponse.thrift"

enum BurstCatalogApiQueryLanguageType {
   Motif, Eql, Hydra
}

struct BurstCatalogApiQuery {
  1: required catalogTypes.BurstCatalogApiKey pk
  2: required catalogTypes.BurstCatalogApiMoniker moniker
  3: required BurstCatalogApiQueryLanguageType languageType
  4: required string source
  5: optional map<string, string> labels
}

struct BurstCatalogApiQueryResponse {
  1: required catalogResponse.BurstCatalogApiResult result
  2: optional BurstCatalogApiQuery query
  3: optional list<BurstCatalogApiQuery> queries
}

