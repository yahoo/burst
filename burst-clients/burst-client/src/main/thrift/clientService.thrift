namespace * org.burstsys.gen.thrift.api.client

include "clientDomain.thrift"
include "clientView.thrift"
include "clientQuery.thrift"

service BTBurstService {
    /**
     * ensureDomain performs an upsert to the domain as it may exist in the catalog.
     * Any field in the spec that has a value is assumed to be an update, and any field
     * sent with a `zero value` is merged with the existing catalog value.
     */
    clientDomain.BTDomainResponse ensureDomain(
        /** the domain that should exist in the catalog. It must specify a udk */
        1: required clientDomain.BTDomain domain,
    );

    /**
     * findDomain returns the domain from the catalog, if it exists.
     */
    clientDomain.BTDomainResponse findDomain(
        /** the udk of the domain to fetch from the catalog. */
        1: required string udk,
    );

    /**
     * ensureDomainContainsView performs an upsert to the view in the specified domain, as it may exist in the catalog.
     * ensureDomainContainsView follows the same rules as ensureDomain to determine which fields are updated and
     * which fields are merged.
     */
    clientView.BTViewResponse ensureDomainContainsView(
        /** the udk specifying domain in which the view should exist.  */
        1: required string domainUdk,
        /** the view that should exist. */
        2: required clientView.BTView spec,
    );

    /**
     * listViewsInDomain returns any views defined in the specified domain.
     */
    clientView.BTViewResponse listViewsInDomain(
        /** the udk of the domain containing the views to be returned. */
        1: required string domainUdk,
    );

    /**
     * executeQuery runs a query
     */
    clientQuery.BTQueryResponse executeQuery(
        /** a unique id for the query. If present it must be of the form [a-zA-Z][a-zA-Z0-9_]{31} */
        1: optional string guid,
        /** the domain to run the query over */
        2: required string domainUdk,
        /** the view to run the query over */
        3: required string viewUdk,
        /** the query text */
        4: required string source,
        /** the timezone to use to interpret date times */
        5: required string timezone,
        /** any parameter values to pass to the query */
        6: optional list<clientQuery.BTParameter> params,
    );
}

