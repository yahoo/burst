namespace * org.burstsys.agent.api

include "agentTypes.thrift"

/*******************************************************************************
    cache operations
 *******************************************************************************/


typedef i64 QueryByteCount
typedef i64 QueryEpochTime
typedef double QuerySkewRatio
typedef double QuerySampleRatio
typedef double QuerySizeRatio

/*
 * TODO
 */
enum BurstQueryGenerationState {
    Cold,
    Hot,
    Warm,
    NoData,
    Mixed,
    Failed
}

/*
 * TODO
 */
enum BurstQueryCacheOperation {
    Search,
    Flush,
    Evict
}

/*
 * TODO
 */
enum BurstQueryOperator {
    LT,
    GT,
    EQ
}

/*
 * TODO
 */
enum BurstQueryCacheParameter {
    ByteCount,       // bytes in the scanned generation
    ItemCount,       // items in the scanned generation
    SliceCount,      // slices in the scanned generation
    RegionCount,     // regions in the scanned generation

    ColdLoadAt,      // epoch time that the cold load occurred
    ColdLoadTook,    // elapsed time in ms that the cold load took
    WarmLoadAt,      // epoch time that the last warm load occurred
    WarmLoadTook,    // elapsed time in ms that the last warm load took
    WarmLoadCount,   // count of warm loads (also number of evicts)

    SizeSkew,        // (max-size - min-size) / min-size
    TimeSkew,        // (max-time - min-time) / min-time
    ItemSize,        // the actual average bytes/item for this dataset
    ItemVariation,   // the actual variation factor for item sizes
    LoadInvalid,     // true if the next-dataset-size and next-sample-rate were not achievable

    EarliestLoadAt,      // epoch time in ms after which there would be a cold load
    RejectedItemCount,   // number of items rejected cause they exceeded next-item-max
    PotentialItemCount,  // The potential items the generation would hold had it not been capped
    SuggestedSampleRate, // The system calculated sample rate recommended for next load
    SuggestedSliceCount, // The system calculated slice count recommended for next load
}

/*
 * TODO
 */
struct BurstQueryCacheGenerationMetrics {
    1: required agentTypes.BurstQueryApiGenerationKey generationKey    // full identity of the generation this group was run on
    3: required QueryByteCount byteCount        // bytes in the scanned generation
    4: required agentTypes.QueryTally itemCount            // items in the scanned generation
    5: required agentTypes.QueryTally sliceCount           // slices in the scanned generation
    6: required agentTypes.QueryTally regionCount          // regions in the scanned generation

    7: required QueryEpochTime coldLoadAt       // epoch time that the cold load occurred
    8: required agentTypes.QueryElapsedMs coldLoadTook     // elapsed time in ms that the cold load took
    9: required QueryEpochTime warmLoadAt       // epoch time that the last warm load occurred
    10: required agentTypes.QueryElapsedMs warmLoadTook    // elapsed time in ms that the last warm load took
    11: required agentTypes.QueryTally warmLoadCount       // count of warm loads (also number of evicts)

    12: required QuerySkewRatio sizeSkew        // (max-size - min-size) / min-size
    13: required QuerySkewRatio timeSkew        // (max-time - min-time) / min-time
    14: required QuerySizeRatio itemSize        // the actual average bytes/item for this dataset
    15: required double itemVariation           // the actual variation factor for item sizes
    16: required bool loadInvalid               // true if the next-dataset-size and next-sample-rate were not achievable

    17: required QueryEpochTime earliestLoadAt  // epoch time in ms after which there would be a cold load
    18: required agentTypes.QueryTally rejectedItemCount   // number of items rejected cause they exceeded next-item-max
    19: required agentTypes.QueryTally potentialItemCount  // The potential items the generation would hold with no size or sampling constraints
    20: required QuerySampleRatio suggestedSampleRate // The system calculated sample rate recommended for next load
    21: required agentTypes.QueryTally suggestedSliceCount // The system calculated slice count recommended for next load
    22: required agentTypes.QueryTally expectedItemCount // the number of items expected to be in the generation
}

/*
 * TODO
 */
struct BurstQueryCacheGeneration {
    1: required agentTypes.BurstQueryApiGenerationKey identity
    2: required BurstQueryGenerationState state
    3: required BurstQueryCacheGenerationMetrics metrics
}

/*
 * TODO
 */
struct BurstQuerySliceKey {
    1: required i64 sliceId
    2: required string hostname
}

/*
 * TODO
 */
struct BurstQueryCacheSlice {
    1: required BurstQuerySliceKey identity
    2: required BurstQueryGenerationState state
    3: BurstQueryCacheGenerationMetrics metrics
}

/*
 * TODO
 */
union BurstQueryCacheParamValue {
    1: i64 longVal
    2: double doubleVal
    3: bool boolVal
}

/*
 *  parameters for query operations.
 *  e.g. {items LT 1000}, {regions GT 200}, {slices LT 200}, {bytes GT 50MB} <- translated into a long val, obviously
 */
struct BurstQueryCacheOperationParameter {
    1: BurstQueryCacheParameter name
    2: BurstQueryOperator relation
    3: BurstQueryCacheParamValue value
}

/*
 * TODO
 */
struct BurstQueryCacheGenerationResult {
    1: required agentTypes.BurstQueryApiResultStatus resultStatus
    2: required agentTypes.QueryStatusMessage resultMessage
    3: optional list<BurstQueryCacheGeneration> generations
}

/*
 * TODO
 */
struct BurstQuerySliceGenerationResult {
    1: required agentTypes.BurstQueryApiResultStatus resultStatus
    2: required agentTypes.QueryStatusMessage resultMessage
    3: optional list<BurstQueryCacheSlice> slices
}
