/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.gen.thrift.api.client.view;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.12.0)", date = "2022-03-10")
public class BTViewResponse implements org.apache.thrift.TBase<BTViewResponse, BTViewResponse._Fields>, java.io.Serializable, Cloneable, Comparable<BTViewResponse> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("BTViewResponse");

  private static final org.apache.thrift.protocol.TField OUTCOME_FIELD_DESC = new org.apache.thrift.protocol.TField("outcome", org.apache.thrift.protocol.TType.STRUCT, (short)1);
  private static final org.apache.thrift.protocol.TField META_FIELD_DESC = new org.apache.thrift.protocol.TField("meta", org.apache.thrift.protocol.TType.MAP, (short)2);
  private static final org.apache.thrift.protocol.TField VIEW_FIELD_DESC = new org.apache.thrift.protocol.TField("view", org.apache.thrift.protocol.TType.STRUCT, (short)3);
  private static final org.apache.thrift.protocol.TField VIEWS_FIELD_DESC = new org.apache.thrift.protocol.TField("views", org.apache.thrift.protocol.TType.LIST, (short)4);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new BTViewResponseStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new BTViewResponseTupleSchemeFactory();

  /**
   * The outcome of the request
   */
  public @org.apache.thrift.annotation.Nullable org.burstsys.gen.thrift.api.client.BTRequestOutcome outcome; // required
  /**
   * Miscellaneous metadata about the request
   */
  public @org.apache.thrift.annotation.Nullable java.util.Map<java.lang.String,java.lang.String> meta; // required
  /**
   * The view returned by the request
   */
  public @org.apache.thrift.annotation.Nullable BTView view; // optional
  /**
   * The list of views returned by the request
   */
  public @org.apache.thrift.annotation.Nullable java.util.List<BTView> views; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    /**
     * The outcome of the request
     */
    OUTCOME((short)1, "outcome"),
    /**
     * Miscellaneous metadata about the request
     */
    META((short)2, "meta"),
    /**
     * The view returned by the request
     */
    VIEW((short)3, "view"),
    /**
     * The list of views returned by the request
     */
    VIEWS((short)4, "views");

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
        case 1: // OUTCOME
          return OUTCOME;
        case 2: // META
          return META;
        case 3: // VIEW
          return VIEW;
        case 4: // VIEWS
          return VIEWS;
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
  private static final _Fields optionals[] = {_Fields.VIEW,_Fields.VIEWS};
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.OUTCOME, new org.apache.thrift.meta_data.FieldMetaData("outcome", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.burstsys.gen.thrift.api.client.BTRequestOutcome.class)));
    tmpMap.put(_Fields.META, new org.apache.thrift.meta_data.FieldMetaData("meta", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
    tmpMap.put(_Fields.VIEW, new org.apache.thrift.meta_data.FieldMetaData("view", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, BTView.class)));
    tmpMap.put(_Fields.VIEWS, new org.apache.thrift.meta_data.FieldMetaData("views", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, BTView.class))));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(BTViewResponse.class, metaDataMap);
  }

  public BTViewResponse() {
  }

  public BTViewResponse(
    org.burstsys.gen.thrift.api.client.BTRequestOutcome outcome,
    java.util.Map<java.lang.String,java.lang.String> meta)
  {
    this();
    this.outcome = outcome;
    this.meta = meta;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public BTViewResponse(BTViewResponse other) {
    if (other.isSetOutcome()) {
      this.outcome = new org.burstsys.gen.thrift.api.client.BTRequestOutcome(other.outcome);
    }
    if (other.isSetMeta()) {
      java.util.Map<java.lang.String,java.lang.String> __this__meta = new java.util.HashMap<java.lang.String,java.lang.String>(other.meta);
      this.meta = __this__meta;
    }
    if (other.isSetView()) {
      this.view = new BTView(other.view);
    }
    if (other.isSetViews()) {
      java.util.List<BTView> __this__views = new java.util.ArrayList<BTView>(other.views.size());
      for (BTView other_element : other.views) {
        __this__views.add(new BTView(other_element));
      }
      this.views = __this__views;
    }
  }

  public BTViewResponse deepCopy() {
    return new BTViewResponse(this);
  }

  @Override
  public void clear() {
    this.outcome = null;
    this.meta = null;
    this.view = null;
    this.views = null;
  }

  /**
   * The outcome of the request
   */
  @org.apache.thrift.annotation.Nullable
  public org.burstsys.gen.thrift.api.client.BTRequestOutcome getOutcome() {
    return this.outcome;
  }

  /**
   * The outcome of the request
   */
  public BTViewResponse setOutcome(@org.apache.thrift.annotation.Nullable org.burstsys.gen.thrift.api.client.BTRequestOutcome outcome) {
    this.outcome = outcome;
    return this;
  }

  public void unsetOutcome() {
    this.outcome = null;
  }

  /** Returns true if field outcome is set (has been assigned a value) and false otherwise */
  public boolean isSetOutcome() {
    return this.outcome != null;
  }

  public void setOutcomeIsSet(boolean value) {
    if (!value) {
      this.outcome = null;
    }
  }

  public int getMetaSize() {
    return (this.meta == null) ? 0 : this.meta.size();
  }

  public void putToMeta(java.lang.String key, java.lang.String val) {
    if (this.meta == null) {
      this.meta = new java.util.HashMap<java.lang.String,java.lang.String>();
    }
    this.meta.put(key, val);
  }

  /**
   * Miscellaneous metadata about the request
   */
  @org.apache.thrift.annotation.Nullable
  public java.util.Map<java.lang.String,java.lang.String> getMeta() {
    return this.meta;
  }

  /**
   * Miscellaneous metadata about the request
   */
  public BTViewResponse setMeta(@org.apache.thrift.annotation.Nullable java.util.Map<java.lang.String,java.lang.String> meta) {
    this.meta = meta;
    return this;
  }

  public void unsetMeta() {
    this.meta = null;
  }

  /** Returns true if field meta is set (has been assigned a value) and false otherwise */
  public boolean isSetMeta() {
    return this.meta != null;
  }

  public void setMetaIsSet(boolean value) {
    if (!value) {
      this.meta = null;
    }
  }

  /**
   * The view returned by the request
   */
  @org.apache.thrift.annotation.Nullable
  public BTView getView() {
    return this.view;
  }

  /**
   * The view returned by the request
   */
  public BTViewResponse setView(@org.apache.thrift.annotation.Nullable BTView view) {
    this.view = view;
    return this;
  }

  public void unsetView() {
    this.view = null;
  }

  /** Returns true if field view is set (has been assigned a value) and false otherwise */
  public boolean isSetView() {
    return this.view != null;
  }

  public void setViewIsSet(boolean value) {
    if (!value) {
      this.view = null;
    }
  }

  public int getViewsSize() {
    return (this.views == null) ? 0 : this.views.size();
  }

  @org.apache.thrift.annotation.Nullable
  public java.util.Iterator<BTView> getViewsIterator() {
    return (this.views == null) ? null : this.views.iterator();
  }

  public void addToViews(BTView elem) {
    if (this.views == null) {
      this.views = new java.util.ArrayList<BTView>();
    }
    this.views.add(elem);
  }

  /**
   * The list of views returned by the request
   */
  @org.apache.thrift.annotation.Nullable
  public java.util.List<BTView> getViews() {
    return this.views;
  }

  /**
   * The list of views returned by the request
   */
  public BTViewResponse setViews(@org.apache.thrift.annotation.Nullable java.util.List<BTView> views) {
    this.views = views;
    return this;
  }

  public void unsetViews() {
    this.views = null;
  }

  /** Returns true if field views is set (has been assigned a value) and false otherwise */
  public boolean isSetViews() {
    return this.views != null;
  }

  public void setViewsIsSet(boolean value) {
    if (!value) {
      this.views = null;
    }
  }

  public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
    switch (field) {
    case OUTCOME:
      if (value == null) {
        unsetOutcome();
      } else {
        setOutcome((org.burstsys.gen.thrift.api.client.BTRequestOutcome)value);
      }
      break;

    case META:
      if (value == null) {
        unsetMeta();
      } else {
        setMeta((java.util.Map<java.lang.String,java.lang.String>)value);
      }
      break;

    case VIEW:
      if (value == null) {
        unsetView();
      } else {
        setView((BTView)value);
      }
      break;

    case VIEWS:
      if (value == null) {
        unsetViews();
      } else {
        setViews((java.util.List<BTView>)value);
      }
      break;

    }
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case OUTCOME:
      return getOutcome();

    case META:
      return getMeta();

    case VIEW:
      return getView();

    case VIEWS:
      return getViews();

    }
    throw new java.lang.IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new java.lang.IllegalArgumentException();
    }

    switch (field) {
    case OUTCOME:
      return isSetOutcome();
    case META:
      return isSetMeta();
    case VIEW:
      return isSetView();
    case VIEWS:
      return isSetViews();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that == null)
      return false;
    if (that instanceof BTViewResponse)
      return this.equals((BTViewResponse)that);
    return false;
  }

  public boolean equals(BTViewResponse that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_outcome = true && this.isSetOutcome();
    boolean that_present_outcome = true && that.isSetOutcome();
    if (this_present_outcome || that_present_outcome) {
      if (!(this_present_outcome && that_present_outcome))
        return false;
      if (!this.outcome.equals(that.outcome))
        return false;
    }

    boolean this_present_meta = true && this.isSetMeta();
    boolean that_present_meta = true && that.isSetMeta();
    if (this_present_meta || that_present_meta) {
      if (!(this_present_meta && that_present_meta))
        return false;
      if (!this.meta.equals(that.meta))
        return false;
    }

    boolean this_present_view = true && this.isSetView();
    boolean that_present_view = true && that.isSetView();
    if (this_present_view || that_present_view) {
      if (!(this_present_view && that_present_view))
        return false;
      if (!this.view.equals(that.view))
        return false;
    }

    boolean this_present_views = true && this.isSetViews();
    boolean that_present_views = true && that.isSetViews();
    if (this_present_views || that_present_views) {
      if (!(this_present_views && that_present_views))
        return false;
      if (!this.views.equals(that.views))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + ((isSetOutcome()) ? 131071 : 524287);
    if (isSetOutcome())
      hashCode = hashCode * 8191 + outcome.hashCode();

    hashCode = hashCode * 8191 + ((isSetMeta()) ? 131071 : 524287);
    if (isSetMeta())
      hashCode = hashCode * 8191 + meta.hashCode();

    hashCode = hashCode * 8191 + ((isSetView()) ? 131071 : 524287);
    if (isSetView())
      hashCode = hashCode * 8191 + view.hashCode();

    hashCode = hashCode * 8191 + ((isSetViews()) ? 131071 : 524287);
    if (isSetViews())
      hashCode = hashCode * 8191 + views.hashCode();

    return hashCode;
  }

  @Override
  public int compareTo(BTViewResponse other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = java.lang.Boolean.valueOf(isSetOutcome()).compareTo(other.isSetOutcome());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetOutcome()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.outcome, other.outcome);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetMeta()).compareTo(other.isSetMeta());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetMeta()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.meta, other.meta);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetView()).compareTo(other.isSetView());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetView()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.view, other.view);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetViews()).compareTo(other.isSetViews());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetViews()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.views, other.views);
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
    java.lang.StringBuilder sb = new java.lang.StringBuilder("BTViewResponse(");
    boolean first = true;

    sb.append("outcome:");
    if (this.outcome == null) {
      sb.append("null");
    } else {
      sb.append(this.outcome);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("meta:");
    if (this.meta == null) {
      sb.append("null");
    } else {
      sb.append(this.meta);
    }
    first = false;
    if (isSetView()) {
      if (!first) sb.append(", ");
      sb.append("view:");
      if (this.view == null) {
        sb.append("null");
      } else {
        sb.append(this.view);
      }
      first = false;
    }
    if (isSetViews()) {
      if (!first) sb.append(", ");
      sb.append("views:");
      if (this.views == null) {
        sb.append("null");
      } else {
        sb.append(this.views);
      }
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (outcome == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'outcome' was not present! Struct: " + toString());
    }
    if (meta == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'meta' was not present! Struct: " + toString());
    }
    // check for sub-struct validity
    if (outcome != null) {
      outcome.validate();
    }
    if (view != null) {
      view.validate();
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

  private static class BTViewResponseStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public BTViewResponseStandardScheme getScheme() {
      return new BTViewResponseStandardScheme();
    }
  }

  private static class BTViewResponseStandardScheme extends org.apache.thrift.scheme.StandardScheme<BTViewResponse> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, BTViewResponse struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // OUTCOME
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.outcome = new org.burstsys.gen.thrift.api.client.BTRequestOutcome();
              struct.outcome.read(iprot);
              struct.setOutcomeIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // META
            if (schemeField.type == org.apache.thrift.protocol.TType.MAP) {
              {
                org.apache.thrift.protocol.TMap _map30 = iprot.readMapBegin();
                struct.meta = new java.util.HashMap<java.lang.String,java.lang.String>(2*_map30.size);
                @org.apache.thrift.annotation.Nullable java.lang.String _key31;
                @org.apache.thrift.annotation.Nullable java.lang.String _val32;
                for (int _i33 = 0; _i33 < _map30.size; ++_i33)
                {
                  _key31 = iprot.readString();
                  _val32 = iprot.readString();
                  struct.meta.put(_key31, _val32);
                }
                iprot.readMapEnd();
              }
              struct.setMetaIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // VIEW
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.view = new BTView();
              struct.view.read(iprot);
              struct.setViewIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // VIEWS
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list34 = iprot.readListBegin();
                struct.views = new java.util.ArrayList<BTView>(_list34.size);
                @org.apache.thrift.annotation.Nullable BTView _elem35;
                for (int _i36 = 0; _i36 < _list34.size; ++_i36)
                {
                  _elem35 = new BTView();
                  _elem35.read(iprot);
                  struct.views.add(_elem35);
                }
                iprot.readListEnd();
              }
              struct.setViewsIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, BTViewResponse struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.outcome != null) {
        oprot.writeFieldBegin(OUTCOME_FIELD_DESC);
        struct.outcome.write(oprot);
        oprot.writeFieldEnd();
      }
      if (struct.meta != null) {
        oprot.writeFieldBegin(META_FIELD_DESC);
        {
          oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRING, struct.meta.size()));
          for (java.util.Map.Entry<java.lang.String, java.lang.String> _iter37 : struct.meta.entrySet())
          {
            oprot.writeString(_iter37.getKey());
            oprot.writeString(_iter37.getValue());
          }
          oprot.writeMapEnd();
        }
        oprot.writeFieldEnd();
      }
      if (struct.view != null) {
        if (struct.isSetView()) {
          oprot.writeFieldBegin(VIEW_FIELD_DESC);
          struct.view.write(oprot);
          oprot.writeFieldEnd();
        }
      }
      if (struct.views != null) {
        if (struct.isSetViews()) {
          oprot.writeFieldBegin(VIEWS_FIELD_DESC);
          {
            oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.views.size()));
            for (BTView _iter38 : struct.views)
            {
              _iter38.write(oprot);
            }
            oprot.writeListEnd();
          }
          oprot.writeFieldEnd();
        }
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class BTViewResponseTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public BTViewResponseTupleScheme getScheme() {
      return new BTViewResponseTupleScheme();
    }
  }

  private static class BTViewResponseTupleScheme extends org.apache.thrift.scheme.TupleScheme<BTViewResponse> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, BTViewResponse struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      struct.outcome.write(oprot);
      {
        oprot.writeI32(struct.meta.size());
        for (java.util.Map.Entry<java.lang.String, java.lang.String> _iter39 : struct.meta.entrySet())
        {
          oprot.writeString(_iter39.getKey());
          oprot.writeString(_iter39.getValue());
        }
      }
      java.util.BitSet optionals = new java.util.BitSet();
      if (struct.isSetView()) {
        optionals.set(0);
      }
      if (struct.isSetViews()) {
        optionals.set(1);
      }
      oprot.writeBitSet(optionals, 2);
      if (struct.isSetView()) {
        struct.view.write(oprot);
      }
      if (struct.isSetViews()) {
        {
          oprot.writeI32(struct.views.size());
          for (BTView _iter40 : struct.views)
          {
            _iter40.write(oprot);
          }
        }
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, BTViewResponse struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      struct.outcome = new org.burstsys.gen.thrift.api.client.BTRequestOutcome();
      struct.outcome.read(iprot);
      struct.setOutcomeIsSet(true);
      {
        org.apache.thrift.protocol.TMap _map41 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRING, iprot.readI32());
        struct.meta = new java.util.HashMap<java.lang.String,java.lang.String>(2*_map41.size);
        @org.apache.thrift.annotation.Nullable java.lang.String _key42;
        @org.apache.thrift.annotation.Nullable java.lang.String _val43;
        for (int _i44 = 0; _i44 < _map41.size; ++_i44)
        {
          _key42 = iprot.readString();
          _val43 = iprot.readString();
          struct.meta.put(_key42, _val43);
        }
      }
      struct.setMetaIsSet(true);
      java.util.BitSet incoming = iprot.readBitSet(2);
      if (incoming.get(0)) {
        struct.view = new BTView();
        struct.view.read(iprot);
        struct.setViewIsSet(true);
      }
      if (incoming.get(1)) {
        {
          org.apache.thrift.protocol.TList _list45 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
          struct.views = new java.util.ArrayList<BTView>(_list45.size);
          @org.apache.thrift.annotation.Nullable BTView _elem46;
          for (int _i47 = 0; _i47 < _list45.size; ++_i47)
          {
            _elem46 = new BTView();
            _elem46.read(iprot);
            struct.views.add(_elem46);
          }
        }
        struct.setViewsIsSet(true);
      }
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}

