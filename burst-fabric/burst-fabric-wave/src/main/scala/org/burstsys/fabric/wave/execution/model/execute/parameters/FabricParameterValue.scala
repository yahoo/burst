/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.model.execute.parameters

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.brio.types.BrioTypes
import org.burstsys.brio.types.BrioTypes.{BrioDataType, BrioTypeKey, brioDataTypeFromClassTag}
import org.burstsys.vitals.errors.VitalsException

import scala.reflect.ClassTag

/**
  * a parameter instance
  */
trait FabricParameterValue extends Any with FabricParameter {

  /**
    * is this parameter set to null?
    *
    * @return
    */
  def isNull: Boolean

  /**
    * return parameter value as scalar
    *
    * @tparam VT
    * @return
    */
  def asScalar[VT <: BrioDataType : ClassTag]: VT

  /**
    * return parameter value as vector
    *
    * @tparam VT
    * @return
    */
  def asVector[VT <: BrioDataType : ClassTag]: Array[VT]

  /**
    * return parameter value as map
    *
    * @tparam KT
    * @tparam VT
    * @return
    */
  def asMap[KT <: BrioDataType : ClassTag, VT <: BrioDataType : ClassTag]: Map[KT, VT]

}

object FabricParameterValue {

  def scalar[VT <: BrioDataType : ClassTag](name: String, data: VT): FabricParameterValue = {
    val vType = brioDataTypeFromClassTag[VT]
    new FabricParameterValueContext(
      vName = name, data = data, isNull = false, vForm = FabricScalarForm, vValueType = vType, vKeyType = BrioTypes.BrioUnitTypeKey
    )
  }

  def nullScalar[VT <: BrioDataType : ClassTag](name: String): FabricParameterValue = {
    val vType = brioDataTypeFromClassTag[VT]
    new FabricParameterValueContext(
      vName = name, data = null, isNull = true, vForm = FabricScalarForm, vValueType = vType, vKeyType = BrioTypes.BrioUnitTypeKey
    )
  }

  def vector[VT <: BrioDataType : ClassTag](name: String, data: Array[VT]): FabricParameterValue = {
    val vType = brioDataTypeFromClassTag[VT]
    new FabricParameterValueContext(
      vName = name, data = data, isNull = false, vForm = FabricVectorForm, vValueType = vType, vKeyType = BrioTypes.BrioUnitTypeKey
    )
  }

  def nullVector[VT <: BrioDataType : ClassTag](name: String): FabricParameterValue = {
    val vType = brioDataTypeFromClassTag[VT]
    new FabricParameterValueContext(
      vName = name, data = null, isNull = true, vForm = FabricVectorForm, vValueType = vType, vKeyType = BrioTypes.BrioUnitTypeKey
    )
  }

  def map[KT <: BrioDataType : ClassTag, VT <: BrioDataType : ClassTag](name: String, data: Map[KT, VT]): FabricParameterValue = {
    val kType = brioDataTypeFromClassTag[KT]
    val vType = brioDataTypeFromClassTag[VT]
    new FabricParameterValueContext(
      vName = name, data = data, isNull = false, vForm = FabricMapForm, vValueType = vType, vKeyType = kType
    )
  }

  def nullMap[KT <: BrioDataType : ClassTag, VT <: BrioDataType : ClassTag](name: String): FabricParameterValue = {
    val kType = brioDataTypeFromClassTag[KT]
    val vType = brioDataTypeFromClassTag[VT]
    new FabricParameterValueContext(
      vName = name, data = null, isNull = true, vForm = FabricMapForm, vValueType = vType, vKeyType = kType
    )
  }

}

class FabricParameterValueContext(vName: String, var data: Any, var isNull: Boolean, vForm: FabricParameterForm, vValueType: BrioTypeKey, vKeyType: BrioTypeKey)
  extends FabricParameterContext(vName, vForm, vValueType, vKeyType) with FabricParameterValue with FabricParameterSerde {

  override def toString: String = {
    val d = if(isNull) "NULL" else data.toString
    s"Value(data=$d, ${super.toString})"
  }

  def this() = this(null, null, false, null, 0, 0)

  ///////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////

  final override
  def asScalar[VT <: BrioDataType : ClassTag]: VT = {
    val vType = brioDataTypeFromClassTag[VT]
    if (form != FabricScalarForm || vType != this.valueType) throw VitalsException(s"can't access as scalar of this type")
    data.asInstanceOf[VT]
  }

  final override
  def asVector[VT <: BrioDataType : ClassTag]: Array[VT] = {
    val vType = brioDataTypeFromClassTag[VT]
    if (form != FabricVectorForm || vType != this.valueType) throw VitalsException(s"can't access as vector of this type")
    data.asInstanceOf[Array[VT]]
  }

  final override
  def asMap[KT <: BrioDataType : ClassTag, VT <: BrioDataType : ClassTag]: Map[KT, VT] = {
    val kType = brioDataTypeFromClassTag[KT]
    val vType = brioDataTypeFromClassTag[VT]
    if (form != FabricMapForm || kType != this.keyType || vType != this.valueType) throw VitalsException(s"can't access as map of this type")
    data.asInstanceOf[Map[KT, VT]]
  }

  ///////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ///////////////////////////////////////////////////////////////////

  override
  def write(kryo: Kryo, output: Output): Unit = {
    super.write(kryo, output)
    output.writeBoolean(isNull)
    if (!isNull)
      form match {
        case FabricScalarForm => writeScalar(output)
        case FabricVectorForm => writeVector(output)
        case FabricMapForm => writeMap(output)
        case _ => ???
      }
  }

  override
  def read(kryo: Kryo, input: Input): Unit = {
    super.read(kryo, input)
    isNull = input.readBoolean
    if (!isNull)
      form match {
        case FabricScalarForm => readScalar(input)
        case FabricVectorForm => readVector(input)
        case FabricMapForm => readMap(input)
        case _ => ???
      }
  }


}
