/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.gen.thrift.api.client.query;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.12.0)", date = "2022-03-10")
public class BTResult implements org.apache.thrift.TBase<BTResult, BTResult._Fields>, java.io.Serializable, Cloneable, Comparable<BTResult> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("BTResult");

  private static final org.apache.thrift.protocol.TField GUID_FIELD_DESC = new org.apache.thrift.protocol.TField("guid", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField MESSAGE_FIELD_DESC = new org.apache.thrift.protocol.TField("message", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField GENERATION_FIELD_DESC = new org.apache.thrift.protocol.TField("generation", org.apache.thrift.protocol.TType.STRUCT, (short)3);
  private static final org.apache.thrift.protocol.TField GENERATION_METRICS_FIELD_DESC = new org.apache.thrift.protocol.TField("generationMetrics", org.apache.thrift.protocol.TType.STRUCT, (short)4);
  private static final org.apache.thrift.protocol.TField EXECUTION_METRICS_FIELD_DESC = new org.apache.thrift.protocol.TField("executionMetrics", org.apache.thrift.protocol.TType.STRUCT, (short)5);
  private static final org.apache.thrift.protocol.TField RESULT_SETS_FIELD_DESC = new org.apache.thrift.protocol.TField("resultSets", org.apache.thrift.protocol.TType.MAP, (short)6);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new BTResultStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new BTResultTupleSchemeFactory();

  /**
   * A gloably unique identifer for the query
   */
  public @org.apache.thrift.annotation.Nullable java.lang.String guid; // required
  /**
   * A summary message describing the query results
   */
  public @org.apache.thrift.annotation.Nullable java.lang.String message; // required
  /**
   * The coordinates of the queried dataset
   */
  public @org.apache.thrift.annotation.Nullable BTViewGeneration generation; // required
  /**
   * Metrics about the queried dataset
   */
  public @org.apache.thrift.annotation.Nullable BTGenerationMetrics generationMetrics; // required
  /**
   * Metrics about the execution of the query
   */
  public @org.apache.thrift.annotation.Nullable BTExecutionMetrics executionMetrics; // required
  /**
   * A map of query name to result set
   */
  public @org.apache.thrift.annotation.Nullable java.util.Map<java.lang.String,BTResultSet> resultSets; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    /**
     * A gloably unique identifer for the query
     */
    GUID((short)1, "guid"),
    /**
     * A summary message describing the query results
     */
    MESSAGE((short)2, "message"),
    /**
     * The coordinates of the queried dataset
     */
    GENERATION((short)3, "generation"),
    /**
     * Metrics about the queried dataset
     */
    GENERATION_METRICS((short)4, "generationMetrics"),
    /**
     * Metrics about the execution of the query
     */
    EXECUTION_METRICS((short)5, "executionMetrics"),
    /**
     * A map of query name to result set
     */
    RESULT_SETS((short)6, "resultSets");

    private static final java.util.Map<java.lang.String, _Fields> byName = new java.util.HashMap<java.lang.String, _Fields>();

    static {
      for (_Fields field : java.util.EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    @org.apache.thrift.annotation.Nullable
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // GUID
          return GUID;
        case 2: // MESSAGE
          return MESSAGE;
        case 3: // GENERATION
          return GENERATION;
        case 4: // GENERATION_METRICS
          return GENERATION_METRICS;
        case 5: // EXECUTION_METRICS
          return EXECUTION_METRICS;
        case 6: // RESULT_SETS
          return RESULT_SETS;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new java.lang.IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    @org.apache.thrift.annotation.Nullable
    public static _Fields findByName(java.lang.String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final java.lang.String _fieldName;

    _Fields(short thriftId, java.lang.String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public java.lang.String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.GUID, new org.apache.thrift.meta_data.FieldMetaData("guid", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.MESSAGE, new org.apache.thrift.meta_data.FieldMetaData("message", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.GENERATION, new org.apache.thrift.meta_data.FieldMetaData("generation", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, BTViewGeneration.class)));
    tmpMap.put(_Fields.GENERATION_METRICS, new org.apache.thrift.meta_data.FieldMetaData("generationMetrics", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, BTGenerationMetrics.class)));
    tmpMap.put(_Fields.EXECUTION_METRICS, new org.apache.thrift.meta_data.FieldMetaData("executionMetrics", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, BTExecutionMetrics.class)));
    tmpMap.put(_Fields.RESULT_SETS, new org.apache.thrift.meta_data.FieldMetaData("resultSets", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), 
            new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, BTResultSet.class))));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(BTResult.class, metaDataMap);
  }

  public BTResult() {
  }

  public BTResult(
    java.lang.String guid,
    java.lang.String message,
    BTViewGeneration generation,
    BTGenerationMetrics generationMetrics,
    BTExecutionMetrics executionMetrics,
    java.util.Map<java.lang.String,BTResultSet> resultSets)
  {
    this();
    this.guid = guid;
    this.message = message;
    this.generation = generation;
    this.generationMetrics = generationMetrics;
    this.executionMetrics = executionMetrics;
    this.resultSets = resultSets;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public BTResult(BTResult other) {
    if (other.isSetGuid()) {
      this.guid = other.guid;
    }
    if (other.isSetMessage()) {
      this.message = other.message;
    }
    if (other.isSetGeneration()) {
      this.generation = new BTViewGeneration(other.generation);
    }
    if (other.isSetGenerationMetrics()) {
      this.generationMetrics = new BTGenerationMetrics(other.generationMetrics);
    }
    if (other.isSetExecutionMetrics()) {
      this.executionMetrics = new BTExecutionMetrics(other.executionMetrics);
    }
    if (other.isSetResultSets()) {
      java.util.Map<java.lang.String,BTResultSet> __this__resultSets = new java.util.HashMap<java.lang.String,BTResultSet>(other.resultSets.size());
      for (java.util.Map.Entry<java.lang.String, BTResultSet> other_element : other.resultSets.entrySet()) {

        java.lang.String other_element_key = other_element.getKey();
        BTResultSet other_element_value = other_element.getValue();

        java.lang.String __this__resultSets_copy_key = other_element_key;

        BTResultSet __this__resultSets_copy_value = new BTResultSet(other_element_value);

        __this__resultSets.put(__this__resultSets_copy_key, __this__resultSets_copy_value);
      }
      this.resultSets = __this__resultSets;
    }
  }

  public BTResult deepCopy() {
    return new BTResult(this);
  }

  @Override
  public void clear() {
    this.guid = null;
    this.message = null;
    this.generation = null;
    this.generationMetrics = null;
    this.executionMetrics = null;
    this.resultSets = null;
  }

  /**
   * A gloably unique identifer for the query
   */
  @org.apache.thrift.annotation.Nullable
  public java.lang.String getGuid() {
    return this.guid;
  }

  /**
   * A gloably unique identifer for the query
   */
  public BTResult setGuid(@org.apache.thrift.annotation.Nullable java.lang.String guid) {
    this.guid = guid;
    return this;
  }

  public void unsetGuid() {
    this.guid = null;
  }

  /** Returns true if field guid is set (has been assigned a value) and false otherwise */
  public boolean isSetGuid() {
    return this.guid != null;
  }

  public void setGuidIsSet(boolean value) {
    if (!value) {
      this.guid = null;
    }
  }

  /**
   * A summary message describing the query results
   */
  @org.apache.thrift.annotation.Nullable
  public java.lang.String getMessage() {
    return this.message;
  }

  /**
   * A summary message describing the query results
   */
  public BTResult setMessage(@org.apache.thrift.annotation.Nullable java.lang.String message) {
    this.message = message;
    return this;
  }

  public void unsetMessage() {
    this.message = null;
  }

  /** Returns true if field message is set (has been assigned a value) and false otherwise */
  public boolean isSetMessage() {
    return this.message != null;
  }

  public void setMessageIsSet(boolean value) {
    if (!value) {
      this.message = null;
    }
  }

  /**
   * The coordinates of the queried dataset
   */
  @org.apache.thrift.annotation.Nullable
  public BTViewGeneration getGeneration() {
    return this.generation;
  }

  /**
   * The coordinates of the queried dataset
   */
  public BTResult setGeneration(@org.apache.thrift.annotation.Nullable BTViewGeneration generation) {
    this.generation = generation;
    return this;
  }

  public void unsetGeneration() {
    this.generation = null;
  }

  /** Returns true if field generation is set (has been assigned a value) and false otherwise */
  public boolean isSetGeneration() {
    return this.generation != null;
  }

  public void setGenerationIsSet(boolean value) {
    if (!value) {
      this.generation = null;
    }
  }

  /**
   * Metrics about the queried dataset
   */
  @org.apache.thrift.annotation.Nullable
  public BTGenerationMetrics getGenerationMetrics() {
    return this.generationMetrics;
  }

  /**
   * Metrics about the queried dataset
   */
  public BTResult setGenerationMetrics(@org.apache.thrift.annotation.Nullable BTGenerationMetrics generationMetrics) {
    this.generationMetrics = generationMetrics;
    return this;
  }

  public void unsetGenerationMetrics() {
    this.generationMetrics = null;
  }

  /** Returns true if field generationMetrics is set (has been assigned a value) and false otherwise */
  public boolean isSetGenerationMetrics() {
    return this.generationMetrics != null;
  }

  public void setGenerationMetricsIsSet(boolean value) {
    if (!value) {
      this.generationMetrics = null;
    }
  }

  /**
   * Metrics about the execution of the query
   */
  @org.apache.thrift.annotation.Nullable
  public BTExecutionMetrics getExecutionMetrics() {
    return this.executionMetrics;
  }

  /**
   * Metrics about the execution of the query
   */
  public BTResult setExecutionMetrics(@org.apache.thrift.annotation.Nullable BTExecutionMetrics executionMetrics) {
    this.executionMetrics = executionMetrics;
    return this;
  }

  public void unsetExecutionMetrics() {
    this.executionMetrics = null;
  }

  /** Returns true if field executionMetrics is set (has been assigned a value) and false otherwise */
  public boolean isSetExecutionMetrics() {
    return this.executionMetrics != null;
  }

  public void setExecutionMetricsIsSet(boolean value) {
    if (!value) {
      this.executionMetrics = null;
    }
  }

  public int getResultSetsSize() {
    return (this.resultSets == null) ? 0 : this.resultSets.size();
  }

  public void putToResultSets(java.lang.String key, BTResultSet val) {
    if (this.resultSets == null) {
      this.resultSets = new java.util.HashMap<java.lang.String,BTResultSet>();
    }
    this.resultSets.put(key, val);
  }

  /**
   * A map of query name to result set
   */
  @org.apache.thrift.annotation.Nullable
  public java.util.Map<java.lang.String,BTResultSet> getResultSets() {
    return this.resultSets;
  }

  /**
   * A map of query name to result set
   */
  public BTResult setResultSets(@org.apache.thrift.annotation.Nullable java.util.Map<java.lang.String,BTResultSet> resultSets) {
    this.resultSets = resultSets;
    return this;
  }

  public void unsetResultSets() {
    this.resultSets = null;
  }

  /** Returns true if field resultSets is set (has been assigned a value) and false otherwise */
  public boolean isSetResultSets() {
    return this.resultSets != null;
  }

  public void setResultSetsIsSet(boolean value) {
    if (!value) {
      this.resultSets = null;
    }
  }

  public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
    switch (field) {
    case GUID:
      if (value == null) {
        unsetGuid();
      } else {
        setGuid((java.lang.String)value);
      }
      break;

    case MESSAGE:
      if (value == null) {
        unsetMessage();
      } else {
        setMessage((java.lang.String)value);
      }
      break;

    case GENERATION:
      if (value == null) {
        unsetGeneration();
      } else {
        setGeneration((BTViewGeneration)value);
      }
      break;

    case GENERATION_METRICS:
      if (value == null) {
        unsetGenerationMetrics();
      } else {
        setGenerationMetrics((BTGenerationMetrics)value);
      }
      break;

    case EXECUTION_METRICS:
      if (value == null) {
        unsetExecutionMetrics();
      } else {
        setExecutionMetrics((BTExecutionMetrics)value);
      }
      break;

    case RESULT_SETS:
      if (value == null) {
        unsetResultSets();
      } else {
        setResultSets((java.util.Map<java.lang.String,BTResultSet>)value);
      }
      break;

    }
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case GUID:
      return getGuid();

    case MESSAGE:
      return getMessage();

    case GENERATION:
      return getGeneration();

    case GENERATION_METRICS:
      return getGenerationMetrics();

    case EXECUTION_METRICS:
      return getExecutionMetrics();

    case RESULT_SETS:
      return getResultSets();

    }
    throw new java.lang.IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new java.lang.IllegalArgumentException();
    }

    switch (field) {
    case GUID:
      return isSetGuid();
    case MESSAGE:
      return isSetMessage();
    case GENERATION:
      return isSetGeneration();
    case GENERATION_METRICS:
      return isSetGenerationMetrics();
    case EXECUTION_METRICS:
      return isSetExecutionMetrics();
    case RESULT_SETS:
      return isSetResultSets();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that == null)
      return false;
    if (that instanceof BTResult)
      return this.equals((BTResult)that);
    return false;
  }

  public boolean equals(BTResult that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_guid = true && this.isSetGuid();
    boolean that_present_guid = true && that.isSetGuid();
    if (this_present_guid || that_present_guid) {
      if (!(this_present_guid && that_present_guid))
        return false;
      if (!this.guid.equals(that.guid))
        return false;
    }

    boolean this_present_message = true && this.isSetMessage();
    boolean that_present_message = true && that.isSetMessage();
    if (this_present_message || that_present_message) {
      if (!(this_present_message && that_present_message))
        return false;
      if (!this.message.equals(that.message))
        return false;
    }

    boolean this_present_generation = true && this.isSetGeneration();
    boolean that_present_generation = true && that.isSetGeneration();
    if (this_present_generation || that_present_generation) {
      if (!(this_present_generation && that_present_generation))
        return false;
      if (!this.generation.equals(that.generation))
        return false;
    }

    boolean this_present_generationMetrics = true && this.isSetGenerationMetrics();
    boolean that_present_generationMetrics = true && that.isSetGenerationMetrics();
    if (this_present_generationMetrics || that_present_generationMetrics) {
      if (!(this_present_generationMetrics && that_present_generationMetrics))
        return false;
      if (!this.generationMetrics.equals(that.generationMetrics))
        return false;
    }

    boolean this_present_executionMetrics = true && this.isSetExecutionMetrics();
    boolean that_present_executionMetrics = true && that.isSetExecutionMetrics();
    if (this_present_executionMetrics || that_present_executionMetrics) {
      if (!(this_present_executionMetrics && that_present_executionMetrics))
        return false;
      if (!this.executionMetrics.equals(that.executionMetrics))
        return false;
    }

    boolean this_present_resultSets = true && this.isSetResultSets();
    boolean that_present_resultSets = true && that.isSetResultSets();
    if (this_present_resultSets || that_present_resultSets) {
      if (!(this_present_resultSets && that_present_resultSets))
        return false;
      if (!this.resultSets.equals(that.resultSets))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + ((isSetGuid()) ? 131071 : 524287);
    if (isSetGuid())
      hashCode = hashCode * 8191 + guid.hashCode();

    hashCode = hashCode * 8191 + ((isSetMessage()) ? 131071 : 524287);
    if (isSetMessage())
      hashCode = hashCode * 8191 + message.hashCode();

    hashCode = hashCode * 8191 + ((isSetGeneration()) ? 131071 : 524287);
    if (isSetGeneration())
      hashCode = hashCode * 8191 + generation.hashCode();

    hashCode = hashCode * 8191 + ((isSetGenerationMetrics()) ? 131071 : 524287);
    if (isSetGenerationMetrics())
      hashCode = hashCode * 8191 + generationMetrics.hashCode();

    hashCode = hashCode * 8191 + ((isSetExecutionMetrics()) ? 131071 : 524287);
    if (isSetExecutionMetrics())
      hashCode = hashCode * 8191 + executionMetrics.hashCode();

    hashCode = hashCode * 8191 + ((isSetResultSets()) ? 131071 : 524287);
    if (isSetResultSets())
      hashCode = hashCode * 8191 + resultSets.hashCode();

    return hashCode;
  }

  @Override
  public int compareTo(BTResult other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = java.lang.Boolean.valueOf(isSetGuid()).compareTo(other.isSetGuid());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetGuid()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.guid, other.guid);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetMessage()).compareTo(other.isSetMessage());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetMessage()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.message, other.message);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetGeneration()).compareTo(other.isSetGeneration());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetGeneration()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.generation, other.generation);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetGenerationMetrics()).compareTo(other.isSetGenerationMetrics());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetGenerationMetrics()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.generationMetrics, other.generationMetrics);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetExecutionMetrics()).compareTo(other.isSetExecutionMetrics());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetExecutionMetrics()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.executionMetrics, other.executionMetrics);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetResultSets()).compareTo(other.isSetResultSets());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetResultSets()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.resultSets, other.resultSets);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  @org.apache.thrift.annotation.Nullable
  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    scheme(iprot).read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    scheme(oprot).write(oprot, this);
  }

  @Override
  public java.lang.String toString() {
    java.lang.StringBuilder sb = new java.lang.StringBuilder("BTResult(");
    boolean first = true;

    sb.append("guid:");
    if (this.guid == null) {
      sb.append("null");
    } else {
      sb.append(this.guid);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("message:");
    if (this.message == null) {
      sb.append("null");
    } else {
      sb.append(this.message);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("generation:");
    if (this.generation == null) {
      sb.append("null");
    } else {
      sb.append(this.generation);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("generationMetrics:");
    if (this.generationMetrics == null) {
      sb.append("null");
    } else {
      sb.append(this.generationMetrics);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("executionMetrics:");
    if (this.executionMetrics == null) {
      sb.append("null");
    } else {
      sb.append(this.executionMetrics);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("resultSets:");
    if (this.resultSets == null) {
      sb.append("null");
    } else {
      sb.append(this.resultSets);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (guid == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'guid' was not present! Struct: " + toString());
    }
    if (message == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'message' was not present! Struct: " + toString());
    }
    if (generation == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'generation' was not present! Struct: " + toString());
    }
    if (generationMetrics == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'generationMetrics' was not present! Struct: " + toString());
    }
    if (executionMetrics == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'executionMetrics' was not present! Struct: " + toString());
    }
    if (resultSets == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'resultSets' was not present! Struct: " + toString());
    }
    // check for sub-struct validity
    if (generation != null) {
      generation.validate();
    }
    if (generationMetrics != null) {
      generationMetrics.validate();
    }
    if (executionMetrics != null) {
      executionMetrics.validate();
    }
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
    try {
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class BTResultStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public BTResultStandardScheme getScheme() {
      return new BTResultStandardScheme();
    }
  }

  private static class BTResultStandardScheme extends org.apache.thrift.scheme.StandardScheme<BTResult> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, BTResult struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // GUID
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.guid = iprot.readString();
              struct.setGuidIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // MESSAGE
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.message = iprot.readString();
              struct.setMessageIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // GENERATION
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.generation = new BTViewGeneration();
              struct.generation.read(iprot);
              struct.setGenerationIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // GENERATION_METRICS
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.generationMetrics = new BTGenerationMetrics();
              struct.generationMetrics.read(iprot);
              struct.setGenerationMetricsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 5: // EXECUTION_METRICS
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.executionMetrics = new BTExecutionMetrics();
              struct.executionMetrics.read(iprot);
              struct.setExecutionMetricsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 6: // RESULT_SETS
            if (schemeField.type == org.apache.thrift.protocol.TType.MAP) {
              {
                org.apache.thrift.protocol.TMap _map42 = iprot.readMapBegin();
                struct.resultSets = new java.util.HashMap<java.lang.String,BTResultSet>(2*_map42.size);
                @org.apache.thrift.annotation.Nullable java.lang.String _key43;
                @org.apache.thrift.annotation.Nullable BTResultSet _val44;
                for (int _i45 = 0; _i45 < _map42.size; ++_i45)
                {
                  _key43 = iprot.readString();
                  _val44 = new BTResultSet();
                  _val44.read(iprot);
                  struct.resultSets.put(_key43, _val44);
                }
                iprot.readMapEnd();
              }
              struct.setResultSetsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, BTResult struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.guid != null) {
        oprot.writeFieldBegin(GUID_FIELD_DESC);
        oprot.writeString(struct.guid);
        oprot.writeFieldEnd();
      }
      if (struct.message != null) {
        oprot.writeFieldBegin(MESSAGE_FIELD_DESC);
        oprot.writeString(struct.message);
        oprot.writeFieldEnd();
      }
      if (struct.generation != null) {
        oprot.writeFieldBegin(GENERATION_FIELD_DESC);
        struct.generation.write(oprot);
        oprot.writeFieldEnd();
      }
      if (struct.generationMetrics != null) {
        oprot.writeFieldBegin(GENERATION_METRICS_FIELD_DESC);
        struct.generationMetrics.write(oprot);
        oprot.writeFieldEnd();
      }
      if (struct.executionMetrics != null) {
        oprot.writeFieldBegin(EXECUTION_METRICS_FIELD_DESC);
        struct.executionMetrics.write(oprot);
        oprot.writeFieldEnd();
      }
      if (struct.resultSets != null) {
        oprot.writeFieldBegin(RESULT_SETS_FIELD_DESC);
        {
          oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRUCT, struct.resultSets.size()));
          for (java.util.Map.Entry<java.lang.String, BTResultSet> _iter46 : struct.resultSets.entrySet())
          {
            oprot.writeString(_iter46.getKey());
            _iter46.getValue().write(oprot);
          }
          oprot.writeMapEnd();
        }
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class BTResultTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public BTResultTupleScheme getScheme() {
      return new BTResultTupleScheme();
    }
  }

  private static class BTResultTupleScheme extends org.apache.thrift.scheme.TupleScheme<BTResult> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, BTResult struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      oprot.writeString(struct.guid);
      oprot.writeString(struct.message);
      struct.generation.write(oprot);
      struct.generationMetrics.write(oprot);
      struct.executionMetrics.write(oprot);
      {
        oprot.writeI32(struct.resultSets.size());
        for (java.util.Map.Entry<java.lang.String, BTResultSet> _iter47 : struct.resultSets.entrySet())
        {
          oprot.writeString(_iter47.getKey());
          _iter47.getValue().write(oprot);
        }
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, BTResult struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      struct.guid = iprot.readString();
      struct.setGuidIsSet(true);
      struct.message = iprot.readString();
      struct.setMessageIsSet(true);
      struct.generation = new BTViewGeneration();
      struct.generation.read(iprot);
      struct.setGenerationIsSet(true);
      struct.generationMetrics = new BTGenerationMetrics();
      struct.generationMetrics.read(iprot);
      struct.setGenerationMetricsIsSet(true);
      struct.executionMetrics = new BTExecutionMetrics();
      struct.executionMetrics.read(iprot);
      struct.setExecutionMetricsIsSet(true);
      {
        org.apache.thrift.protocol.TMap _map48 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
        struct.resultSets = new java.util.HashMap<java.lang.String,BTResultSet>(2*_map48.size);
        @org.apache.thrift.annotation.Nullable java.lang.String _key49;
        @org.apache.thrift.annotation.Nullable BTResultSet _val50;
        for (int _i51 = 0; _i51 < _map48.size; ++_i51)
        {
          _key49 = iprot.readString();
          _val50 = new BTResultSet();
          _val50.read(iprot);
          struct.resultSets.put(_key49, _val50);
        }
      }
      struct.setResultSetsIsSet(true);
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}
