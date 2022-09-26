/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.data.model.generation

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import org.burstsys.fabric.data.model.generation.metrics.{FabricGenerationMetrics, FabricGenerationMetricsContext}
import org.burstsys.fabric.data.model.slice.metadata.{FabricSliceMetadata, FabricSliceMetadataContext}
import org.burstsys.fabric.data.model.slice.state._
import org.burstsys.fabric.metadata.model.datasource.{FabricDatasource, FabricDatasourceContext}
import org.burstsys.vitals.errors.{VitalsException, _}
import org.burstsys.vitals.json.VitalsJsonRepresentable

import scala.collection.mutable

/**
 * All information related to the ''generation'' of a [[org.burstsys.fabric.metadata.model.view.FabricView]]
 * A generation is a single snapshot of the data associated with that view at that specific moment in time.
 * It exists only in the worker caches and is carefully identified to assist in cache management.
 */
trait FabricGeneration extends VitalsJsonRepresentable[FabricGeneration] {

  /**
   * The metadata that uniquely identifies the distributed data objects associated with this generation
   */
  def datasource: FabricDatasource

  /**
   * The state of this slice
   *
   * @return
   */
  def state: FabricDataState

  /**
   * The metadata for the slices of the datasource associated with this distributed data objects
   */
  def slices: Array[FabricSliceMetadata]

  /**
   * The metrics associated with this distributed data objects
   */
  def generationMetrics: FabricGenerationMetrics

  /**
   * add any missing slices. We implement
   * set semantics i.e. duplicates are ignored
   *
   * @param slices the slices to add
   */
  def addSlices(slices: Array[FabricSliceMetadata]): Unit

  /**
   * merge the metrics from all of the slices in the generation.
   * this is useful when building a generation from slices returned by a gather
   * Do this only after all slices have been added
   */
  def finalizeMetrics(): Unit

  final override
  def toString: String =
    s"""|FabGeneration(${datasource}, sliceCount=${slices.length})
        |$generationMetrics
        |${slices.mkString("\n")}""".stripMargin

}

object FabricGeneration {

  def apply(datasource: FabricDatasource, metrics: FabricGenerationMetrics): FabricGeneration =
    FabricGenerationContext().init(datasource, metrics)

  def apply(datasource: FabricDatasource, slices: Array[FabricSliceMetadata] = Array.empty): FabricGeneration =
    FabricGenerationContext().init(datasource, slices)

}

private[fabric] final case
class FabricGenerationContext() extends KryoSerializable with FabricGeneration {

  ///////////////////////////////////////////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  ///////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  var _datasource: FabricDatasource = _

  private[this]
  var _state: FabricDataState = FabricDataCold

  private[this]
  var _slices: mutable.HashSet[FabricSliceMetadata] = new mutable.HashSet[FabricSliceMetadata]

  private[this]
  var _generationMetrics: FabricGenerationMetrics = _

  ///////////////////////////////////////////////////////////////////////////////////////////////////////
  // JSON
  ///////////////////////////////////////////////////////////////////////////////////////////////////////

  override def toJson: FabricGeneration =
    JsonFabricGeneration(datasource, state, slices.map(_.toJsonLite), generationMetrics.toJson)

  override def toJsonLite: FabricGeneration =
    JsonFabricGeneration(datasource = null, state, slices = Array.empty, generationMetrics.toJson)

  ///////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////////////////////////////////////////

  override def datasource: FabricDatasource = _datasource

  override def state: FabricDataState = _state

  override def slices: Array[FabricSliceMetadata] = _slices.toArray

  override def generationMetrics: FabricGenerationMetrics = _generationMetrics

  override
  def addSlices(slices: Array[FabricSliceMetadata]): Unit = {
    this._slices ++= slices
  }

  override
  def finalizeMetrics(): Unit = {
    _generationMetrics = FabricGenerationMetrics()
    _generationMetrics.initMetrics(datasource)
    _generationMetrics.finalizeWaveMetricsOnSupervisor(slices.map(_.generationMetrics))
    _state = _generationMetrics.state
  }

  ///////////////////////////////////////////////////////////////////
  // INTERNAL
  ///////////////////////////////////////////////////////////////////

  def init(datasource: FabricDatasource, metrics: FabricGenerationMetrics): FabricGeneration = {
    _datasource = datasource
    _generationMetrics = metrics
    this
  }

  def init(datasource: FabricDatasource, slices: Array[FabricSliceMetadata]): FabricGeneration = {
    _datasource = datasource
    addSlices(slices)
    finalizeMetrics()
    this
  }

  ///////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ///////////////////////////////////////////////////////////////////

  override
  def read(kryo: Kryo, input: Input): Unit = {
    try {
      _datasource = kryo.readClassAndObject(input).asInstanceOf[FabricDatasourceContext]
      _state = kryo.readClassAndObject(input).asInstanceOf[FabricDataState]
      _generationMetrics = kryo.readClassAndObject(input).asInstanceOf[FabricGenerationMetricsContext]
      _slices = new mutable.HashSet[FabricSliceMetadata]
      val sliceCount = input.readInt
      var i = 0
      while (i < sliceCount) {
        _slices += kryo.readClassAndObject(input).asInstanceOf[FabricSliceMetadataContext]
        i += 1
      }
    } catch safely {
      case t: Throwable =>
        throw VitalsException(t)
    }
  }

  override
  def write(kryo: Kryo, output: Output): Unit = {
    try {
      kryo.writeClassAndObject(output, _datasource)
      kryo.writeClassAndObject(output, _state)
      kryo.writeClassAndObject(output, _generationMetrics)
      output writeInt _slices.size
      _slices foreach {
        p => kryo.writeClassAndObject(output, p)
      }
    } catch safely {
      case t: Throwable =>
        throw VitalsException(t)
    }
  }

}
