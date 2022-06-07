namespace * org.burstsys.agent.api

include "agentTypes.thrift"
include "agentCache.thrift"
include "agentData.thrift"

/*******************************************************************************
    group analysis results management
 *******************************************************************************/

typedef string QueryGroupName
typedef i32 QueryResultIndex
typedef string QueryResultName
typedef map<string, string> QueryPropertySet

/*
 * metrics associated with the overall result of a group analysis execution
 */
struct BurstQueryApiExecutionMetrics {
    1: required agentTypes.QueryElapsedNs scanTime     // the elapsed scan time
    2: required agentTypes.QueryElapsedNs scanWork     // the total of scan times across all items
    3: required agentTypes.QueryTally queryCount       // the total of queries in this group
    4: required agentTypes.QueryTally rowCount         // total number of rows across all queries in the group
    5: required agentTypes.QueryTally succeeded        // total number of sucessful queries
    6: required agentTypes.QueryTally limited          // total number of limited queries
    7: required agentTypes.QueryTally overflowed       // total number of overflowed queries
    8: required agentTypes.QueryElapsedNs compileTime  // the amount of time taken for in compilation
    9: required agentTypes.QueryTally cacheHits        // the number of cache hits
}

/*
 * metrics associated with a single result set within the group analysis execution
 */
struct BurstQueryApiResultSetMetrics {
    1: required bool succeeded
    2: required bool limited
    3: required bool overflowed
    4: required agentTypes.QueryTally rowCount
    5: required QueryPropertySet properties
}

/*
 * In a group analysis the language defines a tabular result schema that has a set of rows with
 * a set of columns. Each column is either a aggregation (count) or a dimension (group by)
 */
enum BurstQueryApiCellType {
    AggregationCell, DimensionCell
}

/*
 * the data associated with a single column in a single row in a single result set in a single group analysis
 */
struct BurstQueryApiResultCell {
    1: required agentData.BurstQueryDataType bType
    2: required bool isNull
    3: required bool isNan
    4: required agentData.BurstQueryApiDatum bData
    5: required BurstQueryApiCellType cellType
}

/*
 * A single multi-dimensional result (set of rows) in a result group
 */
struct BurstQueryApiResultSet {
    1: required QueryResultIndex resultIndex
    2: required QueryResultName resultName
    3: required BurstQueryApiResultSetMetrics metrics
    4: required list<string> columnNames
    5: required list<agentData.BurstQueryDataType> columnTypes
    6: required list<list<BurstQueryApiResultCell>> rowSet
}

/*
 * uniquely and in a user friendly way identify a group analysis execution
 */
struct BurstQueryApiGroupKey {
    // each group analysis executed has a 'group uid' which is used to track the analysis though the pipeline
    1: required agentTypes.QueryGroupUid groupUid
    // each group analysis executed has a 'group name' which is used to provide a user friendly name for a possibly re-used analysis
    2: required QueryGroupName groupName
}

/*
 *  result group metrics (metrics across all result sets)
 */
struct BurstQueryApiResultGroupMetrics {
    1: required BurstQueryApiGroupKey groupKey  // identity of this group operation
    2: required agentTypes.BurstQueryApiGenerationKey generationKey
    3: required agentTypes.BurstQueryApiResultStatus resultStatus
    4: required agentTypes.QueryStatusMessage resultMessage
    5: required agentCache.BurstQueryCacheGenerationMetrics generationMetrics // code generation/data metrics
    6: required BurstQueryApiExecutionMetrics executionMetrics // execution/scan
}

/*
 *  a single result group result in a group analysis execution
 */
struct BurstQueryApiResultGroup {
    1: required BurstQueryApiGroupKey groupKey
    2: required agentTypes.BurstQueryApiResultStatus resultStatus
    3: required agentTypes.QueryStatusMessage resultMessage
    4: required BurstQueryApiResultGroupMetrics groupMetrics
    5: required map<QueryResultName, QueryResultIndex> nameMap
    6: required map<QueryResultIndex, BurstQueryApiResultSet> resultSets
}

/*
 *   general request return status
 */
struct BurstQueryApiExecuteResult {
    1: required agentTypes.BurstQueryApiResultStatus resultStatus
    2: required agentTypes.QueryStatusMessage resultMessage
    3: optional BurstQueryApiResultGroup resultGroup
}

