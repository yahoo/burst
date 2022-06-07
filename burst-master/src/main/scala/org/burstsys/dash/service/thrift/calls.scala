/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.service.thrift

import org.burstsys.fabric.execution.model.execute.parameters.FabricCall
import org.burstsys.fabric.execution.model.execute.parameters.FabricParameterValue
import org.burstsys.gen.thrift.api.client.BTDataFormat
import org.burstsys.gen.thrift.api.client.BTDataType
import org.burstsys.gen.thrift.api.client.BTDataType.BoolType
import org.burstsys.gen.thrift.api.client.BTDataType.ByteType
import org.burstsys.gen.thrift.api.client.BTDataType.DoubleType
import org.burstsys.gen.thrift.api.client.BTDataType.IntType
import org.burstsys.gen.thrift.api.client.BTDataType.LongType
import org.burstsys.gen.thrift.api.client.BTDataType.ShortType
import org.burstsys.gen.thrift.api.client.query.BTParameter

import java.util
import scala.collection.JavaConverters._

object calls {

  def fromThrift(parameters: util.List[BTParameter]): Option[FabricCall] = {
    if (parameters.isEmpty)
      return None
    Some(FabricCall(parameters.asScala.map(paramFromThrift).toArray))
  }

  private def paramFromThrift(parameter: BTParameter): FabricParameterValue = {
    val name = parameter.name
    parameter.format match {
      case BTDataFormat.Scalar =>
        parameter.primaryType match {
          case BoolType =>
            if (parameter.isNull) FabricParameterValue.nullScalar[Boolean](name)
            else FabricParameterValue.scalar(name, parameter.datum.getBoolVal)
          case ByteType =>
            if (parameter.isNull) FabricParameterValue.nullScalar[Byte](name)
            else FabricParameterValue.scalar(name, parameter.datum.getByteVal)
          case ShortType =>
            if (parameter.isNull) FabricParameterValue.nullScalar[Short](name)
            else FabricParameterValue.scalar(name, parameter.datum.getShortVal)
          case IntType =>
            if (parameter.isNull) FabricParameterValue.nullScalar[Int](name)
            else FabricParameterValue.scalar(name, parameter.datum.getIntVal)
          case LongType =>
            if (parameter.isNull) FabricParameterValue.nullScalar[Long](name)
            else FabricParameterValue.scalar(name, parameter.datum.getLongVal)
          case DoubleType =>
            if (parameter.isNull) FabricParameterValue.nullScalar[Double](name)
            else FabricParameterValue.scalar(name, parameter.datum.getDoubleVal)
          case BTDataType.StringType =>
            if (parameter.isNull) FabricParameterValue.nullScalar[String](name)
            else FabricParameterValue.scalar(name, parameter.datum.getStringVal)
        }
      case BTDataFormat.Vector =>
        parameter.primaryType match {
          case BoolType =>
            if (parameter.isNull) FabricParameterValue.nullVector[Boolean](name)
            else FabricParameterValue.vector(name, parameter.datum.getBoolVector.asScala.toArray)
          case ByteType =>
            if (parameter.isNull) FabricParameterValue.nullVector[Byte](name)
            else FabricParameterValue.vector(name, parameter.datum.getByteVector.asScala.toArray)
          case ShortType =>
            if (parameter.isNull) FabricParameterValue.nullVector[Short](name)
            else FabricParameterValue.vector(name, parameter.datum.getShortVector.asScala.toArray)
          case IntType =>
            if (parameter.isNull) FabricParameterValue.nullVector[Int](name)
            else FabricParameterValue.vector(name, parameter.datum.getIntVector.asScala.toArray)
          case LongType =>
            if (parameter.isNull) FabricParameterValue.nullVector[Long](name)
            else FabricParameterValue.vector(name, parameter.datum.getLongVector.asScala.toArray)
          case DoubleType =>
            if (parameter.isNull) FabricParameterValue.nullVector[Double](name)
            else FabricParameterValue.vector(name, parameter.datum.getDoubleVector.asScala.toArray)
          case BTDataType.StringType =>
            if (parameter.isNull) FabricParameterValue.nullVector[String](name)
            else FabricParameterValue.vector(name, parameter.datum.getStringVector.asScala.toArray)
        }
      case BTDataFormat.Map =>
        parameter.primaryType match {
          case BoolType | ByteType | ShortType | IntType | LongType | DoubleType =>
            // map keys must be string for now
            ???
          case BTDataType.StringType =>
            parameter.secondaryType match {
              case BoolType =>
                if (parameter.isNull) FabricParameterValue.nullMap[String, Boolean](name)
                else FabricParameterValue.map[String, Boolean](name, parameter.datum.getStringBoolMap.asScala.map(e => e._1 -> (e._2: Boolean)).toMap)
              case IntType =>
                if (parameter.isNull) FabricParameterValue.nullMap[String, Int](name)
                else FabricParameterValue.map[String, Int](name, parameter.datum.getStringIntMap.asScala.map(e => e._1 -> (e._2: Int)).toMap)
              case LongType =>
                if (parameter.isNull) FabricParameterValue.nullMap[String, Long](name)
                else FabricParameterValue.map[String, Long](name, parameter.datum.getStringLongMap.asScala.map(e => e._1 -> (e._2: Long)).toMap)
              case BTDataType.StringType =>
                if (parameter.isNull) FabricParameterValue.nullMap[String, String](name)
                else FabricParameterValue.map[String, String](name, asMap(parameter.datum.getStringStringMap))
              case ByteType | ShortType | DoubleType =>
                // these map values are currently unsupported
                ???
            }
        }
    }
  }
}
