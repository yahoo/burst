namespace * org.burstsys.gen.thrift.api.client.query

include "clientTypes.thrift"

struct BTParameter {
    /** the name of this parameter */
    1: required string name
    /** if the parameter is a scalar, vector, or map */
    2: required clientTypes.BTDataFormat format
    /** the type of the parameter, or if the parameter is a map the type of they map's keys */
    3: required clientTypes.BTDataType primaryType
    /** unused, unless the parameter is a map, then the type of the map's values */
    4: optional clientTypes.BTDataType secondaryType
    /** the acutal value of the parameter */
    5: required clientTypes.BTDatum datum
    /** if data is a null value */
    6: required bool isNull
}

/** Coordinates of the dataset used to fulfil the query */
struct BTViewGeneration {
    /** The UDK of the query domain */
    1: required string domainUdk
    /** The UDK of the queried view */
    2: required string viewUdk
    /** The epoch timestamp of when the view was loaded */
    3: required i64 generationClock
}

/** Statistics about the loaded dataset */
struct BTGenerationMetrics {
    /** The number of bytes scanned */
    1: required i64 byteCount
    /** The number of items scanned */
    2: required i64 itemCount
    /** The number of slices scanned */
    3: required i64 sliceCount
    /** The number of regions scanned */
    4: required i64 regionCount

    /** The epoch time of the cold load */
    5: required i64 coldLoadAt
    /** The elapsed duration of the cold load, in ms */
    6: required i64 coldLoadTookMs
    /** The epoch time of the last warm load */
    7: required i64 warmLoadAt
    /** The elapsed duration of the last warm load */
    8: required i64 warmLoadTookMs
    /** The count of warm loads (also number of evicts - 1) */
    9: required i64 warmLoadCount

    /** The skew in size between slices. (max-size - min-size) / min-size */
    10: required double sizeSkew
    /** The skew in execution time between slices. (max-time - min-time) / min-time */
    11: required double timeSkew
    /** The average bytes per item in the dataset */
    12: required double itemSize
    /** The variation factor for item sizes */
    13: required double itemVariation
    /** An indicator of whether the sample-rate needs to be adjusted. (the next-dataset-size and next-sample-rate were not achievable) */
    14: required bool loadInvalid

    /** The epoch time in ms after which there would be a cold load */
    15: required i64 earliestLoadAt
    /** The number of items rejected cause they exceeded next-item-max */
    16: required i64 rejectedItemCount
    /** The potential number of items the generation would hold without sampling or size constraints */
    17: required i64 potentialItemCount
    /** The system calculated sample rate recommended for next load */
    18: required double suggestedSampleRate
    /** The system calculated slice count recommended for next load */
    19: required i64 suggestedSliceCount
    /** The number of items the store expected to load */
    20: required i64 expectedItemCount
}

struct BTExecutionMetrics {
    /** The elapsed scan time in ns */
    1: required i64 scanTimeNs
    /** The aggregate scan time across all items */
    2: required i64 scanWorkNs
    /** The number of queries submitted */
    3: required i64 queryCount
    /** The total number of rows across all queries in the group */
    4: required i64 rowCount
    /** The total number of sucessful queries */
    5: required i64 succeeded
    /** The total number of row-limited queries */
    6: required i64 limited
    /** The total number of queries with dictionary overflows */
    7: required i64 overflowed
    /** The amount of time taken to compile the queries */
    8: required i64 compileTimeNs
    /** The number of queries that were reused from cache */
    9: required i64 cacheHits
}

struct BTResultSetMeta {
    /** If the query succeeded */
    1: required bool succeeded
    /** If the query was row-limited */
    2: required bool limited
    /** If the query had a dictionary overflow */
    3: required bool overflowed
    /** The number of rows returned */
    4: required i64 rowCount
    /** Extra properties for the query */
    5: required map<string, string> properties
}

enum BTCellType {
    Dimension, Aggregation
}

struct BTCell {
    /** If the cell is a dimension or an aggregation */
    1: required BTCellType cType
    /** The datatype of the cell */
    2: required clientTypes.BTDataType dType
    /** The cell's data */
    3: required clientTypes.BTDatum datum
    /** If the cell's data is null */
    4: required bool isNull
    /** If the cell's data is Not a Number */
    5: required bool isNaN
}

struct BTResultSet {
    /** The name of the query that produced the result set (generated if not provided by the user) */
    1: required string name
    /** Metadata about the result set */
    2: required BTResultSetMeta meta
    /** The list of column names in the order they appear in each row */
    3: required list<string> columnNames
    /** The list of datatypes for each cell in the row */
    4: required list<clientTypes.BTDataType> columnTypes
    /** A list of rows, which is a list of cells */
    5: required list<list<BTCell>> rows
}

struct BTResult {
    /** A gloably unique identifer for the query */
    1: required string guid
    /** A summary message describing the query results */
    2: required string message
    /** The coordinates of the queried dataset */
    3: required BTViewGeneration generation
    /** Metrics about the queried dataset */
    4: required BTGenerationMetrics generationMetrics
    /** Metrics about the execution of the query */
    5: required BTExecutionMetrics executionMetrics
    /** A map of query name to result set */
    6: required map<string, BTResultSet> resultSets
}

struct BTQueryResponse {
    /** The outcome of the request */
    1: required clientTypes.BTRequestOutcome outcome
    /** Miscellaneous metadata about the request */
    2: required map<string, string> meta
    /** The query result, if the query succeeded */
    3: optional BTResult result
}
