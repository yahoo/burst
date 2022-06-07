namespace * org.burstsys.agent.api

/*******************************************************************************
    data passing types
 *******************************************************************************/

 /*
  *  The set of primitive value types that a result row column/cell can have
  */
enum BurstQueryDataType {
    BooleanType,
    ByteType,
    ShortType,
    IntegerType,
    LongType,
    DoubleType,
    StringType
}

 /*
  *  A thrift type that can hold the runtime form value data can have
  */
enum BurstQueryApiDataForm {
    Scalar, Vector, Map
}

 /*
  *  A thrift type that can hold the runtime data associated with any of the above primitive types
  */
union BurstQueryApiDatum {

    // scalar values
   1: bool booleanData
   2: byte byteData
   3: i16 shortData
   4: i32 integerData
   5: i64 longData
   6: double doubleData
   7: string stringData

    // vector values
   8: list<bool> booleanVectorData
   9: list<byte> byteVectorData
   10: list<i16> shortVectorData
   11: list<i32> integerVectorData
   12: list<i64> longVectorData
   13: list<double> doubleVectorData
   14: list<string> stringVectorData

    // map values
   15: map<bool, bool> booleanBooleanMapData
   16: map<string, string> stringStringMapData
   // TODO maps

}