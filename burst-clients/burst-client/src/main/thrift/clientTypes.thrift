namespace * org.burstsys.gen.thrift.api.client

enum BTResultStatus {
   UnknownStatus,       // catalog+agent
   SuccessStatus,       // catalog+agent
   TimeoutStatus,       // catalog+agent
   ExceptionStatus,     // catalog+agent
   InvalidStatus,       // catalog+agent
   NotReadyStatus,      // catalog+agent
   NoDataStatus,        // agent only
   StoreErrorStatus     // agent only
   InProgressStatus,    // agent only
   NotFound,            // catalog only
   Conflict,            // catalog only
}

enum BTDataType {
    BoolType,   // 0
    ByteType,   // 1
    ShortType,  // 2
    IntType,    // 3
    LongType,   // 4
    DoubleType, // 5
    StringType  // 6
}

enum BTDataFormat {
    /** a single primitive datum */
    Scalar,
    /** a list of data */
    Vector,
    /** a key-value set of data */
    Map
}

/** a polymorphic container for a value */
union BTDatum {
   1: bool   boolVal
   2: byte   byteVal
   3: i16    shortVal
   4: i32    intVal
   5: i64    longVal
   6: double doubleVal
   7: string stringVal

   8:  list<bool>   boolVector
   9:  list<byte>   byteVector
   10: list<i16>    shortVector
   11: list<i32>    intVector
   12: list<i64>    longVector
   13: list<double> doubleVector
   14: list<string> stringVector

    // map values
   15: map<string, bool>   stringBoolMap
   16: map<string, i32>    stringIntMap
   17: map<string, i64>    stringLongMap
   18: map<string, string> stringStringMap
}

/**
 * A result summary included in every thrift response object
 */
struct BTRequestOutcome {
  /** a well defined representation of how the request completed */
  1: required BTResultStatus status
  /** a human readable representation of the request's result */
  2: required string message
}
