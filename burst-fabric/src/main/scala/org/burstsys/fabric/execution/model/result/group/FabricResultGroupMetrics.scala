/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.execution.model.result.group

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import org.burstsys.fabric.data.model.generation.key.{FabricGenerationKey, FabricGenerationKeyContext}
import org.burstsys.fabric.data.model.generation.metrics.{FabricGenerationMetrics, FabricGenerationMetricsContext}
import org.burstsys.fabric.execution.model.execute.group.{FabricGroupKey, FabricGroupKeyContext}
import org.burstsys.fabric.execution.model.gather.FabricGather
import org.burstsys.fabric.execution.model.metrics.{FabricExecutionMetrics, FabricExecutionMetricsContext}
import org.burstsys.fabric.execution.model.result._
import org.burstsys.fabric.execution.model.result.status._
import org.burstsys.vitals.errors.{VitalsException, _}
import org.burstsys.vitals.json.VitalsJsonObject
import org.burstsys.vitals.json.VitalsJsonRepresentable

import scala.language.implicitConversions

/**
 * the metrics associated with a result group
 */
trait FabricResultGroupMetrics extends VitalsJsonRepresentable[FabricResultGroupMetrics] with FabricResult {

  /**
   * the unique key for a group
   *
   * @return
   */
  def groupKey: FabricGroupKey

  /**
   * the generation identity for this result group
   *
   * @return
   */
  def generationKey: FabricGenerationKey

  /**
   * the metrics associated with the data generation
   *
   * @return
   */
  def generationMetrics: FabricGenerationMetrics

  /**
   * the metrics associated with the execution
   *
   * @return
   */
  def executionMetrics: FabricExecutionMetrics

  /**
   * were there any failures?
   *
   * @return
   */
  final
  def hadFailures: Boolean = executionMetrics.hadFailures

  final override
  def toString: String =
    s"""|
        |RESULT_GROUP_METRICS
        |   $groupKey
        |   $generationKey
        |   resultStatus=$resultStatus
        |   resultMessage=$resultMessage
        |$generationMetrics
        |$executionMetrics""".stripMargin
}

/**
 * Constructors
 */
object FabricResultGroupMetrics {

  def apply(
             groupKey: FabricGroupKey = FabricGroupKey(),
             generationKey: FabricGenerationKey = FabricGenerationKey(),
             resultStatus: FabricResultStatus = FabricSuccessResultStatus,
             resultMessage: String = "ok",
             generationMetrics: FabricGenerationMetrics = FabricGenerationMetrics(),
             executionMetrics: FabricExecutionMetrics = FabricExecutionMetrics()
           ): FabricResultGroupMetrics =
    FabricResultGroupMetricsContext(
      groupKey = groupKey: FabricGroupKey,
      generationKey = generationKey,
      resultStatus = resultStatus: FabricResultStatus,
      resultMessage = resultMessage,
      generationMetrics = generationMetrics: FabricGenerationMetrics,
      executionMetrics = executionMetrics: FabricExecutionMetrics
    )

  def apply(gather: FabricGather): FabricResultGroupMetrics =
    FabricResultGroupMetricsContext(
      groupKey = gather.groupKey,
      generationKey = gather.gatherMetrics.generationKey,
      resultStatus = gather.resultStatus,
      resultMessage = gather.resultMessage,
      generationMetrics = gather.gatherMetrics.generationMetrics,
      executionMetrics = gather.gatherMetrics.executionMetrics
    )

}

private final case
class FabricResultGroupMetricsContext(
                                       var groupKey: FabricGroupKey,
                                       var generationKey: FabricGenerationKey,
                                       var resultStatus: FabricResultStatus,
                                       var resultMessage: String,
                                       var generationMetrics: FabricGenerationMetrics,
                                       var executionMetrics: FabricExecutionMetrics
                                     ) extends FabricResultGroupMetrics with KryoSerializable with VitalsJsonObject {


  ///////////////////////////////////////////////////////////////////
  // JSON
  ///////////////////////////////////////////////////////////////////

  override def toJson: FabricResultGroupMetrics = FabricResultGroupMetricsContext(
    groupKey.toJson, generationKey.toJson, resultStatus, resultMessage, generationMetrics.toJson, executionMetrics.toJson
  )

  ///////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ///////////////////////////////////////////////////////////////////

  override
  def read(kryo: Kryo, input: Input): Unit = {
    try {
      groupKey = kryo.readClassAndObject(input).asInstanceOf[FabricGroupKeyContext]
      generationKey = kryo.readClassAndObject(input).asInstanceOf[FabricGenerationKeyContext]
      resultStatus = kryo.readClassAndObject(input).asInstanceOf[FabricResultStatus]
      resultMessage = input.readString
      generationMetrics = kryo.readClassAndObject(input).asInstanceOf[FabricGenerationMetricsContext]
      executionMetrics = kryo.readClassAndObject(input).asInstanceOf[FabricExecutionMetricsContext]
    } catch safely {
      case t: Throwable =>
        throw VitalsException(t)
    }
  }

  override
  def write(kryo: Kryo, output: Output): Unit = {
    try {
      kryo.writeClassAndObject(output, groupKey)
      kryo.writeClassAndObject(output, generationKey)
      kryo.writeClassAndObject(output, resultStatus)
      output writeString resultMessage
      kryo.writeClassAndObject(output, generationMetrics)
      kryo.writeClassAndObject(output, executionMetrics)
    } catch safely {
      case t: Throwable =>
        throw VitalsException(t)
    }
  }

}
