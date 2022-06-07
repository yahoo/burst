namespace * org.burstsys.catalog.api

include "catalogTypes.thrift"
include "catalogResponse.thrift"
include "catalogQuery.thrift"
include "catalogTopology.thrift"
include "catalogDatasets.thrift"

service BurstCatalogApiService {

    /**
     * query CRUD
     */
    catalogQuery.BurstCatalogApiQueryResponse allQueries(1:  optional i32 limit)
    catalogQuery.BurstCatalogApiQueryResponse findQueryByPk(1: catalogTypes.BurstCatalogApiKey pk)
    catalogQuery.BurstCatalogApiQueryResponse findQueryByMoniker(1: catalogTypes.BurstCatalogApiMoniker BurstCatalogApiMoniker)
    catalogResponse.BurstCatalogApiEntityPkResponse insertQuery(1: catalogQuery.BurstCatalogApiQuery query)
    catalogResponse.BurstCatalogApiEntityPkResponse updateQuery(1: catalogQuery.BurstCatalogApiQuery query)
    catalogResponse.BurstCatalogApiEntityPkResponse deleteQuery(1: catalogTypes.BurstCatalogApiKey pk)
    catalogQuery.BurstCatalogApiQueryResponse searchQueries(1: string descriptor, 2:  optional i32 limit)
    catalogQuery.BurstCatalogApiQueryResponse searchQueriesByLabel(1: string label, 2: optional string value, 3:  optional i32 limit)

    /**
     * master CRUD
     */
    catalogTopology.BurstCatalogApiMasterResponse allMasters(1:  optional i32 limit)
    catalogTopology.BurstCatalogApiMasterResponse allMastersForSite(1: catalogTypes.BurstCatalogApiKey siteFk, optional i32 limit)
    catalogTopology.BurstCatalogApiMasterResponse allMastersForCell(1: catalogTypes.BurstCatalogApiKey cellFk, optional i32 limit)
    catalogTopology.BurstCatalogApiMasterResponse findMasterByPk(1: catalogTypes.BurstCatalogApiKey pk)
    catalogTopology.BurstCatalogApiMasterResponse findMasterByMoniker(1: catalogTypes.BurstCatalogApiMoniker BurstCatalogApiMoniker)
    catalogResponse.BurstCatalogApiEntityPkResponse insertMaster(1: catalogTopology.BurstCatalogApiMaster master)
    catalogResponse.BurstCatalogApiEntityPkResponse updateMaster(1: catalogTopology.BurstCatalogApiMaster master)
    catalogResponse.BurstCatalogApiEntityPkResponse deleteMaster(1: catalogTypes.BurstCatalogApiKey pk)
    catalogResponse.BurstCatalogApiEntityPkResponse deleteMastersForSite(1: catalogTypes.BurstCatalogApiKey siteFk)
    catalogResponse.BurstCatalogApiEntityPkResponse deleteMastersForCell(1: catalogTypes.BurstCatalogApiKey cellFk)
    catalogTopology.BurstCatalogApiMasterResponse searchMasters(1: string descriptor, 2:  optional i32 limit)
    catalogTopology.BurstCatalogApiMasterResponse searchMastersByLabel(1: string label, 2: optional string value, 3:  optional i32 limit)

    /**
     * worker CRUD
     */
    catalogTopology.BurstCatalogApiWorkerResponse allWorkers(1:  optional i32 limit)
    catalogTopology.BurstCatalogApiWorkerResponse allWorkersForSite(1: catalogTypes.BurstCatalogApiKey siteFk, optional i32 limit)
    catalogTopology.BurstCatalogApiWorkerResponse allWorkersForCell(1: catalogTypes.BurstCatalogApiKey cellFk, optional i32 limit)
    catalogTopology.BurstCatalogApiWorkerResponse findWorkerByPk(1: catalogTypes.BurstCatalogApiKey pk)
    catalogTopology.BurstCatalogApiWorkerResponse findWorkerByMoniker(1: catalogTypes.BurstCatalogApiMoniker BurstCatalogApiMoniker)
    catalogResponse.BurstCatalogApiEntityPkResponse insertWorker(1: catalogTopology.BurstCatalogApiWorker worker)
    catalogResponse.BurstCatalogApiEntityPkResponse updateWorker(1: catalogTopology.BurstCatalogApiWorker worker)
    catalogResponse.BurstCatalogApiEntityPkResponse deleteWorker(1: catalogTypes.BurstCatalogApiKey pk)
    catalogResponse.BurstCatalogApiEntityPkResponse deleteWorkersForSite(1: catalogTypes.BurstCatalogApiKey siteFk)
    catalogResponse.BurstCatalogApiEntityPkResponse deleteWorkersForCell(1: catalogTypes.BurstCatalogApiKey cellFk)
    catalogTopology.BurstCatalogApiWorkerResponse searchWorkers(1: string descriptor, 2:  optional i32 limit)
    catalogTopology.BurstCatalogApiWorkerResponse searchWorkersByLabel(1: string label, 2: optional string value, 3:  optional i32 limit)

    /**
     * site CRUD
     */
    catalogTopology.BurstCatalogApiSiteResponse allSites(1:  optional i32 limit)
    catalogTopology.BurstCatalogApiSiteResponse findSiteByPk(1: catalogTypes.BurstCatalogApiKey pk)
    catalogTopology.BurstCatalogApiSiteResponse findSiteByMoniker(1: catalogTypes.BurstCatalogApiMoniker BurstCatalogApiMoniker)
    catalogResponse.BurstCatalogApiEntityPkResponse insertSite(1: catalogTopology.BurstCatalogApiSite site)
    catalogResponse.BurstCatalogApiEntityPkResponse updateSite(1: catalogTopology.BurstCatalogApiSite site)
    catalogResponse.BurstCatalogApiEntityPkResponse deleteSite(1: catalogTypes.BurstCatalogApiKey pk)
    catalogTopology.BurstCatalogApiSiteResponse searchSites(1: string descriptor, 2:  optional i32 limit)
    catalogTopology.BurstCatalogApiSiteResponse searchSitesByLabel(1: string label, 2: optional string value, 3:  optional i32 limit)

    /**
     * cell CRUD
     */
    catalogTopology.BurstCatalogApiCellResponse allCells(1:  optional i32 limit)
    catalogTopology.BurstCatalogApiCellResponse allCellsForSite(1: catalogTypes.BurstCatalogApiKey siteFk, optional i32 limit)
    catalogTopology.BurstCatalogApiCellResponse findCellByPk(1: catalogTypes.BurstCatalogApiKey pk)
    catalogTopology.BurstCatalogApiCellResponse findCellByMoniker(1: catalogTypes.BurstCatalogApiMoniker BurstCatalogApiMoniker)
    catalogResponse.BurstCatalogApiEntityPkResponse insertCell(1: catalogTopology.BurstCatalogApiCell cell)
    catalogResponse.BurstCatalogApiEntityPkResponse updateCell(1: catalogTopology.BurstCatalogApiCell cell)
    catalogResponse.BurstCatalogApiEntityPkResponse deleteCell(1: catalogTypes.BurstCatalogApiKey pk)
    catalogResponse.BurstCatalogApiEntityPkResponse deleteCellsForSite(1: catalogTypes.BurstCatalogApiKey siteFk)
    catalogTopology.BurstCatalogApiCellResponse searchCells(1: string descriptor, 2:  optional i32 limit)
    catalogTopology.BurstCatalogApiCellResponse searchCellsByLabel(1: string label, 2: optional string value, 3:  optional i32 limit)

    /**
     * domain CRUD
     */
    catalogDatasets.BurstCatalogApiDomainResponse allDomains(1:  optional i32 limit)
    catalogDatasets.BurstCatalogApiDomainResponse findDomainByPk(1: catalogTypes.BurstCatalogApiKey pk)
    catalogDatasets.BurstCatalogApiDomainResponse findDomainByUdk(1: catalogTypes.BurstCatalogApiUdk udk)
    catalogDatasets.BurstCatalogApiDomainAndViewsResponse findDomainWithViewsByUdk(1: catalogTypes.BurstCatalogApiUdk udk)
    catalogDatasets.BurstCatalogApiDomainResponse findDomainByMoniker(1: catalogTypes.BurstCatalogApiMoniker BurstCatalogApiMoniker)
    catalogResponse.BurstCatalogApiEntityPkResponse ensureDomain(1: catalogDatasets.BurstCatalogApiDomain domain)
    catalogResponse.BurstCatalogApiEntityPkResponse insertDomain(1: catalogDatasets.BurstCatalogApiDomain domain)
    catalogResponse.BurstCatalogApiEntityPkResponse updateDomain(1: catalogDatasets.BurstCatalogApiDomain domain)
    catalogResponse.BurstCatalogApiEntityPkResponse deleteDomain(1: catalogTypes.BurstCatalogApiKey pk)
    catalogDatasets.BurstCatalogApiDomainResponse searchDomains(1: string descriptor, 2:  optional i32 limit)
    catalogDatasets.BurstCatalogApiDomainResponse searchDomainsByLabel(1: string label, 2: optional string value, 3:  optional i32 limit)

    /**
     * view CRUD
     */
    catalogDatasets.BurstCatalogApiViewResponse allViews(1:  optional i32 limit)
    catalogDatasets.BurstCatalogApiViewResponse allViewsForDomain(1: catalogTypes.BurstCatalogApiKey domainPk, optional i32 limit)
    catalogDatasets.BurstCatalogApiViewResponse findViewByPk(1: catalogTypes.BurstCatalogApiKey pk)
    catalogDatasets.BurstCatalogApiViewResponse findViewByUdk(1: catalogTypes.BurstCatalogApiUdk udk)
    catalogDatasets.BurstCatalogApiViewResponse findViewByMoniker(1: catalogTypes.BurstCatalogApiMoniker BurstCatalogApiMoniker)
    catalogResponse.BurstCatalogApiEntityPkResponse ensureView(1: catalogDatasets.BurstCatalogApiView view)
    catalogResponse.BurstCatalogApiEntityPkResponse insertView(1: catalogDatasets.BurstCatalogApiView view)
    catalogResponse.BurstCatalogApiEntityPkResponse updateView(1: catalogDatasets.BurstCatalogApiView view)
    catalogResponse.BurstCatalogApiEntityPkResponse deleteView(1: catalogTypes.BurstCatalogApiKey pk)
    catalogResponse.BurstCatalogApiEntityPkResponse deleteViewsForDomain(1: catalogTypes.BurstCatalogApiKey pk)
    catalogDatasets.BurstCatalogApiViewResponse searchViews(1: string descriptor, 2:  optional i32 limit)
    catalogDatasets.BurstCatalogApiViewResponse searchViewsByLabel(1: string label, 2: optional string value, 3:  optional i32 limit)

}

