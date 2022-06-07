namespace * org.burstsys.catalog.api

include "catalogTypes.thrift"
include "catalogResponse.thrift"

/*****************************************************/
/* Masters */
/*****************************************************/

struct BurstCatalogApiMaster {
  1: required catalogTypes.BurstCatalogApiKey pk
  2: required catalogTypes.BurstCatalogApiMoniker moniker
  3: required catalogTypes.BurstCatalogApiHostName nodeName
  4: required catalogTypes.BurstCatalogApiHostAddress nodeAddress
  5: required catalogTypes.BurstCatalogApiHostPort masterPort
  6: required catalogTypes.BurstCatalogApiKey siteFk
  7: optional catalogTypes.BurstCatalogApiKey cellFk
  8: required map<string, string> masterProperties
  9: optional map<string, string> labels
}

struct BurstCatalogApiMasterResponse {
  1: required catalogResponse.BurstCatalogApiResult result
  2: optional BurstCatalogApiMaster master
  3: optional list<BurstCatalogApiMaster> masters
}

/*****************************************************/
/* Workers */
/*****************************************************/

struct BurstCatalogApiWorker {
  1: required catalogTypes.BurstCatalogApiKey pk
  2: required catalogTypes.BurstCatalogApiMoniker moniker
  3: required catalogTypes.BurstCatalogApiHostName nodeName
  4: required catalogTypes.BurstCatalogApiHostAddress nodeAddress
  5: required catalogTypes.BurstCatalogApiKey siteFk
  6: optional catalogTypes.BurstCatalogApiKey cellFk
  7: required map<string, string> workerProperties
  8: optional map<string, string> labels
}

struct BurstCatalogApiWorkerResponse {
  1: required catalogResponse.BurstCatalogApiResult result
  2: optional BurstCatalogApiWorker worker
  3: optional list<BurstCatalogApiWorker> workers
}

/*****************************************************/
/* Sites */
/*****************************************************/

struct BurstCatalogApiSite {
  1: required catalogTypes.BurstCatalogApiKey pk
  2: required catalogTypes.BurstCatalogApiMoniker moniker
  3: required map<string, string>  siteProperties
  4: optional map<string, string> labels
}

struct BurstCatalogApiSiteResponse {
  1: required catalogResponse.BurstCatalogApiResult result
  2: optional BurstCatalogApiSite site
  3: optional list<BurstCatalogApiSite> sites
}

/*****************************************************/
/* Cells */
/*****************************************************/

struct BurstCatalogApiCell {
  1: required catalogTypes.BurstCatalogApiKey pk
  2: required catalogTypes.BurstCatalogApiMoniker moniker
  3: required catalogTypes.BurstCatalogApiKey siteFk
  4: required map<string, string> cellProperties
  5: optional map<string, string> labels
}

struct BurstCatalogApiCellResponse {
  1: required catalogResponse.BurstCatalogApiResult result
  2: optional BurstCatalogApiCell cell
  3: optional list<BurstCatalogApiCell> cells
}
