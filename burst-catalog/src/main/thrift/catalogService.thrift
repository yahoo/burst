namespace * org.burstsys.catalog.api

include "catalogTypes.thrift"
include "catalogResponse.thrift"
include "catalogQuery.thrift"
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

