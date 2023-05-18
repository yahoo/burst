/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.model.gather.metrics

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import org.burstsys.fabric.wave.data.model.generation.key.{FabricGenerationKey, FabricGenerationKeyContext}
import org.burstsys.fabric.wave.data.model.generation.metrics.{FabricGenerationMetrics, FabricGenerationMetricsContext}
import org.burstsys.fabric.wave.execution.model.metrics.{FabricExecutionMetrics, FabricExecutionMetricsContext, FabricMetrics}
import org.burstsys.fabric.wave.execution.model.result.FabricResult
import org.burstsys.fabric.wave.execution.model.result.status.{FabricResultStatus, FabricUnknownResultStatus}
import org.burstsys.vitals.errors.{VitalsException, _}

/**
 * metrics associated with a fabric gather
 *
 */
trait FabricGatherMetrics extends FabricMetrics[FabricGatherMetrics] with FabricResult {

  /**
   * the identity of the data generation associated with this gather
   *
   * @return
   */
  def generationKey: FabricGenerationKey

  /**
   * the metrics for the data generation associated with this gather
   *
   * @return
   */
  def generationMetrics: FabricGenerationMetrics

  /**
   * the metrics for the execution associated with this gather
   *
   * @return
   */
  def executionMetrics: FabricExecutionMetrics

  final override
  def toString: String =
    s"""|FabGatherMetrics($generationKey resultStatus=$resultStatus resultMessage=$resultMessage)
        |GENERATION_METRICS:
        |  ${generationMetrics.metricsString}
        |$executionMetrics""".stripMargin

}

object FabricGatherMetrics {

  def apply(): FabricGatherMetrics = FabricGatherMetricsContext()

}

final case
class FabricGatherMetricsContext() extends FabricGatherMetrics with KryoSerializable {

  ///////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  ///////////////////////////////////////////////////////////////////

  private
  var _generationKey: FabricGenerationKey = FabricGenerationKey()

  private
  var _resultStatus: FabricResultStatus = FabricUnknownResultStatus

  private
  var _resultMessage: String = "unknown"

  private
  var _generationMetrics: FabricGenerationMetrics = FabricGenerationMetrics()

  private
  var _executionMetrics: FabricExecutionMetrics = FabricExecutionMetrics()

  ///////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////

  override def generationKey: FabricGenerationKey = _generationKey

  override def resultStatus: FabricResultStatus = _resultStatus

  override def resultMessage: String = _resultMessage

  override def generationMetrics: FabricGenerationMetrics = _generationMetrics

  override def executionMetrics: FabricExecutionMetrics = _executionMetrics

  ///////////////////////////////////////////////////////////////////
  // JSON
  ///////////////////////////////////////////////////////////////////

  override
  def toJson: FabricGatherMetrics =
    JsonFabricGatherMetrics(generationKey.toJson, generationMetrics.toJson, executionMetrics.toJson, resultStatus, resultMessage)

  ///////////////////////////////////////////////////////////////////
  // LIFECYCLE
  ///////////////////////////////////////////////////////////////////

  override
  def initMetrics(key: FabricGenerationKey): Unit = {
    _generationKey.init(key)
    _resultStatus = FabricUnknownResultStatus
    _resultMessage = "unknown"
    _generationMetrics.initMetrics(key)
    _executionMetrics.initMetrics(key)
  }

  ///////////////////////////////////////////////////////////////////
  // MERGES
  ///////////////////////////////////////////////////////////////////

  override def mergeItemMetricsOnWorker(metrics: FabricGatherMetrics): Unit = {
    _generationMetrics.mergeItemMetricsOnWorker(metrics.generationMetrics)
    _executionMetrics.mergeItemMetricsOnWorker(metrics.executionMetrics)
  }

  override def finalizeRegionMetricsOnWorker(): Unit = {
    _generationMetrics.finalizeRegionMetricsOnWorker()
    _executionMetrics.finalizeRegionMetricsOnWorker()
  }

  override def finalizeSliceMetricsOnWorker(): Unit = {
    _generationMetrics.finalizeSliceMetricsOnWorker()
    _executionMetrics.finalizeSliceMetricsOnWorker()
  }

  override def finalizeWaveMetricsOnSupervisor(sliceMetrics: Array[FabricGatherMetrics]): Unit = {
    _generationMetrics.finalizeWaveMetricsOnSupervisor(sliceMetrics.map(_.generationMetrics))
    _executionMetrics.finalizeWaveMetricsOnSupervisor(sliceMetrics.map(_.executionMetrics))
  }

  ///////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ///////////////////////////////////////////////////////////////////

  override
  def write(kryo: Kryo, output: Output): Unit = {
    try {
      kryo.writeClassAndObject(output, _generationKey)
      kryo.writeClassAndObject(output, _resultStatus)
      output writeString _resultMessage
      kryo.writeClassAndObject(output, _generationMetrics)
      kryo.writeClassAndObject(output, _executionMetrics)
    } catch safely {
      case t: Throwable => throw VitalsException(t)
    }
  }

  override
  def read(kryo: Kryo, input: Input): Unit = {
    try {
      _generationKey = kryo.readClassAndObject(input).asInstanceOf[FabricGenerationKeyContext]
      _resultStatus = kryo.readClassAndObject(input).asInstanceOf[FabricResultStatus]
      _resultMessage = input.readString
      _generationMetrics = kryo.readClassAndObject(input).asInstanceOf[FabricGenerationMetricsContext]
      _executionMetrics = kryo.readClassAndObject(input).asInstanceOf[FabricExecutionMetricsContext]
    } catch safely {
      case t: Throwable => throw VitalsException(t)
    }
  }

}
