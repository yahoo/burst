/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio;

abstract public class JBrioSchematic {
    // public BrioSchema getSchema();

    // this is type key for this structure
    abstract public byte getStructureKey();

    // this is version of this structure
    abstract public int getVersionKey();

    // this is the total number of  fields
    abstract public byte getFieldCount();

    // this is the subtotal of fixed size fields
    abstract public byte fixedFieldCount();

    // this is the subtotal of variable size fields
    abstract public byte variableFieldCount();

    // get the key for a name field
    abstract public byte fieldKey(String fieldName);

    // this is the map of field keys to field names
    abstract public String getField(byte key);

    /**
     * this is where you can look up if a field id represents a vector field
     * remember that field keys are generally one based and this a zero based collection
     */
    abstract public boolean isVector(byte fieldKey);

    /**
     * this is where you can look up if a field id represents a map field
     * remember that field keys are generally one based and this a zero based collection
     */
    abstract public boolean isMap(byte fieldKey);

    /**
     * this is where you can look up if a field id represents a value field
     * remember that field keys are generally one based and this a zero based collection
     */
    abstract public boolean isValue(byte fieldKey);

    /**
     * this is where you can look up the type key for the value part of a field
     * remember that field keys are generally one based and this a zero based collection
     */
    abstract public byte valueTypeKey(byte fieldKey);

    /**
     * get the schematic structure for a reference type
     */
    abstract public JBrioSchematic schematic(byte typeKey);

    /**
     * this is where you can look up the type key for the key part of a map field
     * remember that field keys are generally one based and this a zero based collection
     */
    abstract public byte mapTypeKey(byte fieldKey);

    // this is the offset from the container location where you can find the nulls bitmap block
    abstract public int nullsMapStart();

    // this is the size of the nulls bitmap block
    abstract public int nullsMapSize();

    // this is the offset from the container location where you can find the fixed field data block
    abstract public int fixedFieldsStart();

    // this is the size of the fixed fields block
    abstract public int  fixedFieldsSize();

    // this is the lookup for for finding the offset from the container location where you can find a fixed field
    abstract public int fixedFieldOffsets(byte fieldKey);

    // this is the offset from the container location where you can find the variable field offset section
    abstract public int variableFieldOffsetsStart();

    // this is the size of the variable fields offset lookup array
    abstract public int variableFieldOffsetsSize();

    // this is the offset from the container location where you can find the variable field data section
    abstract public int variableFieldsDataStart();

    // convert from field key to index into the variable field's offset array
    abstract public byte variableFieldOffsetKeys(byte fieldKey);

}