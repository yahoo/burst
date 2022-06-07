/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.model.execution.group

import org.burstsys.agent.api.BurstQueryApiDatum._
import org.burstsys.agent.api.{BurstQueryApiDataForm, BurstQueryApiDatum, BurstQueryApiParameter, BurstQueryDataType}
import org.burstsys.agent.model.execution.group.datum._
import org.burstsys.brio.types.BrioTypes
import org.burstsys.brio.types.BrioTypes._
import org.burstsys.fabric.execution.model.execute.parameters.{FabricMapForm, FabricParameterValue, FabricScalarForm, FabricVectorForm}
import org.burstsys.vitals.errors.VitalsException

import scala.language.implicitConversions

package object parameter {

  type AgentParameter = BurstQueryApiParameter.Proxy

  type AgentThriftParameter = BurstQueryApiParameter

  implicit def fabricToAgentParameter(parameter: FabricParameterValue): BurstQueryApiParameter = {
    val keyType: Option[BurstQueryDataType] = if (parameter.keyType == BrioUnitTypeKey) None else Some(datatypeFor(parameter.keyType))
    BurstQueryApiParameter(
      name = parameter.name, isNull = parameter.isNull, data = parameterToDatum(parameter), form = parameter.form,
      valueType = datatypeFor(parameter.valueType), keyType = keyType
    )
  }

  implicit def thriftToFabricParameter(parameter: BurstQueryApiParameter): FabricParameterValue = {
    parameter.form match {
      case BurstQueryApiDataForm.Scalar =>
        parameter.valueType match {
          case BurstQueryDataType.BooleanType =>
            if (parameter.isNull) FabricParameterValue.nullScalar[Boolean](parameter.name)
            else
              FabricParameterValue.scalar[Boolean](parameter.name, parameter.data.asInstanceOf[BooleanData].booleanData)
          case BurstQueryDataType.ByteType =>
            if (parameter.isNull) FabricParameterValue.nullScalar[Byte](parameter.name)
            else
              FabricParameterValue.scalar[Byte](parameter.name, parameter.data.asInstanceOf[ByteData].byteData)
          case BurstQueryDataType.ShortType =>
            if (parameter.isNull) FabricParameterValue.nullScalar[Short](parameter.name)
            else
              FabricParameterValue.scalar[Short](parameter.name, parameter.data.asInstanceOf[ShortData].shortData)
          case BurstQueryDataType.IntegerType =>
            if (parameter.isNull) FabricParameterValue.nullScalar[Int](parameter.name)
            else
              FabricParameterValue.scalar[Int](parameter.name, parameter.data.asInstanceOf[IntegerData].integerData)
          case BurstQueryDataType.LongType =>
            if (parameter.isNull) FabricParameterValue.nullScalar[Long](parameter.name)
            else
              FabricParameterValue.scalar[Long](parameter.name, parameter.data.asInstanceOf[LongData].longData)
          case BurstQueryDataType.DoubleType =>
            if (parameter.isNull) FabricParameterValue.nullScalar[Double](parameter.name)
            else
              FabricParameterValue.scalar[Double](parameter.name, parameter.data.asInstanceOf[DoubleData].doubleData)
          case BurstQueryDataType.StringType =>
            if (parameter.isNull) FabricParameterValue.nullScalar[String](parameter.name)
            else
              FabricParameterValue.scalar[String](parameter.name, parameter.data.asInstanceOf[StringData].stringData)
          case _ => ???
        }
      case BurstQueryApiDataForm.Vector =>
        parameter.valueType match {
          case BurstQueryDataType.BooleanType =>
            if (parameter.isNull) FabricParameterValue.nullVector[Boolean](parameter.name)
            else
              FabricParameterValue.vector[Boolean](parameter.name, parameter.data.asInstanceOf[BooleanVectorData].booleanVectorData.toArray)
          case BurstQueryDataType.ByteType =>
            if (parameter.isNull) FabricParameterValue.nullVector[Byte](parameter.name)
            else
              FabricParameterValue.vector[Byte](parameter.name, parameter.data.asInstanceOf[ByteVectorData].byteVectorData.toArray)
          case BurstQueryDataType.ShortType =>
            if (parameter.isNull) FabricParameterValue.nullVector[Short](parameter.name)
            else
              FabricParameterValue.vector[Short](parameter.name, parameter.data.asInstanceOf[ShortVectorData].shortVectorData.toArray)
          case BurstQueryDataType.IntegerType =>
            if (parameter.isNull) FabricParameterValue.nullVector[Int](parameter.name)
            else
              FabricParameterValue.vector[Int](parameter.name, parameter.data.asInstanceOf[IntegerVectorData].integerVectorData.toArray)
          case BurstQueryDataType.LongType =>
            if (parameter.isNull) FabricParameterValue.nullVector[Long](parameter.name)
            else
              FabricParameterValue.vector[Long](parameter.name, parameter.data.asInstanceOf[LongVectorData].longVectorData.toArray)
          case BurstQueryDataType.DoubleType =>
            if (parameter.isNull) FabricParameterValue.nullVector[Double](parameter.name)
            else
              FabricParameterValue.vector[Double](parameter.name, parameter.data.asInstanceOf[DoubleVectorData].doubleVectorData.toArray)
          case BurstQueryDataType.StringType =>
            if (parameter.isNull) FabricParameterValue.nullVector[String](parameter.name)
            else
              FabricParameterValue.vector[String](parameter.name, parameter.data.asInstanceOf[StringVectorData].stringVectorData.toArray)
          case _ => ???
        }
      case BurstQueryApiDataForm.Map =>
        val kt = parameter.keyType.getOrElse(throw VitalsException(s"key type missing"))
        (kt, parameter.valueType) match {
          case (BurstQueryDataType.StringType, BurstQueryDataType.StringType) =>
            if (parameter.isNull) FabricParameterValue.nullMap[String, String](parameter.name)
            else
              FabricParameterValue.map[String, String](parameter.name, parameter.data.asInstanceOf[StringStringMapData].stringStringMapData.toMap)
          // TODO flesh out rest
          case _ => ???
        }
      case _ => ???
    }
  }


  def parameterToDatum(parameter: FabricParameterValue): BurstQueryApiDatum = {
    parameter.form match {
      case FabricScalarForm =>
        parameter.valueType match {
          case BrioTypes.BrioBooleanKey => BurstQueryApiDatum.BooleanData(parameter.asScalar[Boolean])
          case BrioTypes.BrioByteKey => BurstQueryApiDatum.ByteData(parameter.asScalar[Byte])
          case BrioTypes.BrioShortKey => BurstQueryApiDatum.ShortData(parameter.asScalar[Short])
          case BrioTypes.BrioIntegerKey => BurstQueryApiDatum.IntegerData(parameter.asScalar[Int])
          case BrioTypes.BrioLongKey => BurstQueryApiDatum.LongData(parameter.asScalar[Long])
          case BrioTypes.BrioDoubleKey => BurstQueryApiDatum.DoubleData(parameter.asScalar[Double])
          case BrioTypes.BrioStringKey => BurstQueryApiDatum.StringData(parameter.asScalar[String])
        }
      case FabricVectorForm =>
        parameter.valueType match {
          case BrioTypes.BrioBooleanKey => BurstQueryApiDatum.BooleanVectorData(parameter.asVector[Boolean])
          case BrioTypes.BrioByteKey => BurstQueryApiDatum.ByteVectorData(parameter.asVector[Byte])
          case BrioTypes.BrioShortKey => BurstQueryApiDatum.ShortVectorData(parameter.asVector[Short])
          case BrioTypes.BrioIntegerKey => BurstQueryApiDatum.IntegerVectorData(parameter.asVector[Int])
          case BrioTypes.BrioLongKey => BurstQueryApiDatum.LongVectorData(parameter.asVector[Long])
          case BrioTypes.BrioDoubleKey => BurstQueryApiDatum.DoubleVectorData(parameter.asVector[Double])
          case BrioTypes.BrioStringKey => BurstQueryApiDatum.StringVectorData(parameter.asVector[String])
        }
      case FabricMapForm =>
        (parameter.keyType, parameter.valueType) match {
          case (BrioTypes.BrioStringKey, BrioTypes.BrioStringKey) => BurstQueryApiDatum.StringStringMapData(parameter.asMap[String, String])
          // TODO flesh out rest
          case _ => ???
        }
      case _ => ???
    }
  }
}
