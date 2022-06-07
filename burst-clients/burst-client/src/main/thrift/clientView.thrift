namespace * org.burstsys.gen.thrift.api.client.view

include "clientTypes.thrift"

struct BTView {
    /** The PK is used internally, but should not be relied upon by clients */
    1: optional i64 pk
    /** The user defined key (UDK) is a unique key that should be used to create/fetch domains */
    2: required string udk
    /** The moniker is a user-friendly display string for this domain */
    3: required string moniker
    /** The UDK of the domain that this view belongs to */
    4: required string domainUdk
    /** The epoch timestamp when the view was last updated or loaded */
    5: optional i64 generationClock
    /** Store properties are used to pass additional information to the store during the ETL phase */
    6: required map<string, string> storeProperties
    /** The motif statement used to select data to load during ETL */
    7: required string viewMotif
    /** View properties are used to store additional information about the view */
    8: required map<string, string> viewProperties
    /** Labels allow users to classify domains, but aren't used internally by Burst */
    9: required map<string, string> labels
    /** The name of the schema used by the view */
    10: required string schemaName
    /** The epoch second when the view was created */
    11: optional i64 createTimestamp
    /** The epoch second when the view was updated */
    12: optional i64 modifyTimestamp
    /** The epoch second when the most recenty query was run against the view */
    13: optional i64 accessTimestamp
}

struct BTViewResponse {
    /** The outcome of the request */
    1: required clientTypes.BTRequestOutcome outcome
    /** Miscellaneous metadata about the request */
    2: required map<string, string> meta
    /** The view returned by the request */
    3: optional BTView view
    /** The list of views returned by the request */
    4: optional list<BTView> views
}

