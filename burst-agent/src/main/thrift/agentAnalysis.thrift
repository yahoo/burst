namespace * org.burstsys.agent.api

include "agentTypes.thrift"
include "agentData.thrift"

/*******************************************************************************
    group analysis execution datatypes
 *******************************************************************************/

/*
 * the source language group analysis specification as a text string
 */
typedef string QueryLanguageSource

/*
 * the particulars for identifying the data and locale information
 */
struct BurstQueryApiOver {
    1: required agentTypes.QueryDomainKey domainKey
    2: required agentTypes.QueryViewKey viewKey
    3: optional string timeZone
}

/*
 *   parameters for group analysis execution
 */
struct BurstQueryApiParameter {
    // parameter name in signature
    1: required string name
    // is this parameter null??
    2: required bool isNull
    // the value for this parameter
    3: required agentData.BurstQueryApiDatum data
    // the ''form'' for this value
    4: required agentData.BurstQueryApiDataForm form
    // the datatype for this value
    5: required agentData.BurstQueryDataType valueType
    // the datatype for a possible ''key'' for this value (maps)
    6: optional agentData.BurstQueryDataType keyType
}

/*
 *   the 'call' i.e. the part of the group analysis execution that can change each time
 */
struct BurstQueryApiCall {
    // a subset of the ''signature'' of parameters in force for this analysis that are provided in the request
    1: required list<BurstQueryApiParameter> parameters
}
