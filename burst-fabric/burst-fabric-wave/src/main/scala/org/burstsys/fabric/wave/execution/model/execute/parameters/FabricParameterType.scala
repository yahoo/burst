/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.model.execute.parameters

import org.burstsys.brio.types.BrioTypes
import org.burstsys.brio.types.BrioTypes._

import scala.reflect.ClassTag

/**
  * a parameter type
  */
trait FabricParameterType extends Any with FabricParameter {

  /**
    * the name of this parameter
    *
    * @return
    */
  def name: String

  /**
    * the ''form'' of this parameter
    *
    * @return
    */
  def form: FabricParameterForm

  /**
    * the value brio type
    *
    * @return
    */
  def valueType: BrioTypeKey

  /**
    * the key brio type
    *
    * @return
    */
  def keyType: BrioTypeKey

}

object FabricParameterType {

  def scalar[VT <: BrioDataType : ClassTag](name: String): FabricParameterType = {
    val vType = BrioTypes.brioDataTypeFromClassTag[VT]
    new FabricParameterTypeContext(tName = name, tForm = FabricScalarForm, tValueType = vType, tKeyType = BrioUnitTypeKey)
  }

  def vector[VT <: BrioDataType : ClassTag](name: String): FabricParameterType = {
    val vType = BrioTypes.brioDataTypeFromClassTag[VT]
    new FabricParameterTypeContext(tName = name, tForm = FabricVectorForm, tValueType = vType, tKeyType = BrioUnitTypeKey)
  }

  def map[KT <: BrioDataType : ClassTag, VT <: BrioDataType : ClassTag](name: String): FabricParameterType = {
    val kType = BrioTypes.brioDataTypeFromClassTag[KT]
    val vType = BrioTypes.brioDataTypeFromClassTag[VT]
    new FabricParameterTypeContext(tName = name, tForm = FabricMapForm, tValueType = vType, tKeyType = kType)
  }

}

final
class FabricParameterTypeContext(
                                  tName: String, tForm: FabricParameterForm,
                                  tValueType: BrioTypeKey, tKeyType: BrioTypeKey
                                ) extends FabricParameterContext(tName, tForm, tValueType, tKeyType) with FabricParameterType {

  override def toString: String = s"Type(${super.toString})"

  def this() = this(null, null, 0, 0)
}
