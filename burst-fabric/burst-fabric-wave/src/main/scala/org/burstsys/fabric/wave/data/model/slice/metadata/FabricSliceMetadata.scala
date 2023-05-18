/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.model.slice.metadata

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import org.burstsys.fabric.wave.data.model.generation.metrics.{FabricGenerationMetrics, FabricGenerationMetricsContext}
import org.burstsys.fabric.wave.data.model.slice.FabricSliceKey
import org.burstsys.fabric.wave.data.model.slice.state._
import org.burstsys.fabric.wave.data.model.snap.FabricSnap
import org.burstsys.fabric.wave.metadata.model.datasource.{FabricDatasource, FabricDatasourceContext}
import org.burstsys.vitals.errors
import org.burstsys.vitals.errors.{VitalsException, _}
import org.burstsys.vitals.json.VitalsJsonObject
import org.burstsys.vitals.json.VitalsJsonRepresentable
import org.burstsys.vitals.net.{VitalsHostName, getPublicHostName}

/**
 * The '''metadata''' of a 'data partition' in the Burst dataset world. These are distributed/hosted
 * across worker nodes.
 */
trait FabricSliceMetadata extends VitalsJsonRepresentable[FabricSliceMetadata] with Equals {

  /**
   * the datasource (domain/view) for this generation
   */
  def datasource: FabricDatasource

  /**
   * the unique identifier for this slice within a generation
   */
  def sliceKey: FabricSliceKey

  /**
   * the name of the host this slice resides upon
   */
  def hostname: VitalsHostName

  /**
   * the state this slice is in
   */
  def state: FabricDataState

  /**
   * set the state this slice is in
   */
  def state_=(s: FabricDataState): Unit

  /**
   * print out a total of all errors and exception traces
   *
   * @return
   */
  def failure: String

  /**
   * add a failure exception stack to list for this slice
   *
   * @param t
   */
  def failure(t: Throwable): Unit

  /**
   * add a failure message to list of error messages for this slice
   *
   * @param msg
   */
  def failure(msg: String): Unit

  /**
   * the metrics associated with generation of this slice
   */
  def generationMetrics: FabricGenerationMetrics

  def reset(): Unit

  ///////////////////////////////////////////////////////////////////////////
  // Identity/Equality
  ///////////////////////////////////////////////////////////////////////////

  final override
  def hashCode(): Int = {
    var result: Long = 17L
    result = 31 * result + datasource.hashCode
    result = 31 * result + sliceKey.hashCode
    result.toInt
  }

  final override
  def equals(obj: scala.Any): Boolean = obj match {
    case that: FabricSliceMetadata => this.datasource == that.datasource &&
      this.sliceKey == that.sliceKey
  }

  final override
  def canEqual(that: Any): Boolean = that.isInstanceOf[FabricSliceMetadata]

  final override
  def toString: String =
    s"""|FabSliceMetadata(datasource=$datasource, sliceKey=$sliceKey, state=$state, hostname=$hostname, failure='$failure')
        |$generationMetrics""".stripMargin

}

object FabricSliceMetadata {

  def apply(
             datasource: FabricDatasource,
             sliceKey: FabricSliceKey,
             hostname: VitalsHostName
           ): FabricSliceMetadata =
    FabricSliceMetadataContext(
      _datasource = datasource,
      _sliceKey = sliceKey,
      _hostname = hostname,
      _state = FabricDataCold,
      _generationMetrics = FabricGenerationMetrics(datasource)
    )

  def apply(snap: FabricSnap): FabricSliceMetadata =
    FabricSliceMetadataContext(
      _datasource = snap.slice.datasource,
      _sliceKey = snap.slice.sliceKey,
      _hostname = getPublicHostName,
      _state = FabricDataCold,
      _generationMetrics = FabricGenerationMetrics(snap.slice.datasource)
    )

}

private[fabric] final case
class FabricSliceMetadataContext(
                                  private var _datasource: FabricDatasource,
                                  private var _sliceKey: FabricSliceKey,
                                  private var _hostname: VitalsHostName,
                                  private var _state: FabricDataState,
                                  private var _generationMetrics: FabricGenerationMetrics,
                                  private var _failure: String = ""
                                ) extends KryoSerializable with FabricSliceMetadata with VitalsJsonObject {

  def this() = this(null, 0, null, null, null, null)

  override def toJson: FabricSliceMetadata =
    JsonFabricSliceMetadata(datasource.toJson, sliceKey, hostname, state, failure, generationMetrics.toJson)

  override def toJsonLite: FabricSliceMetadata =
    JsonFabricSliceMetadata(datasource = null, sliceKey, hostname, state, failure, generationMetrics.toJson)

  override def state: FabricDataState = _state

  override def state_=(s: FabricDataState): Unit = {
    _state = s
    _generationMetrics.state = s
  }

  override def reset(): Unit = {
    _state = FabricDataCold
    _failure = ""
    _generationMetrics.initMetrics(datasource)
  }

  override def failure(t: Throwable): Unit = {
    _state = FabricDataFailed
    if (_failure.nonEmpty) {
      _failure += "\n---\n"
    }
    _failure += s"${errors.printStack(t)}"
  }

  override def failure(msg: String): Unit = {
    _state = FabricDataFailed
    if (_failure.nonEmpty) {
      _failure += "\n---\n"
    }
    _failure += s"$msg"
  }

  override def datasource: FabricDatasource = _datasource

  override def sliceKey: FabricSliceKey = _sliceKey

  override def hostname: VitalsHostName = _hostname

  override def failure: String = _failure

  override def generationMetrics: FabricGenerationMetrics = _generationMetrics

  ///////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ///////////////////////////////////////////////////////////////////

  override
  def read(kryo: Kryo, input: Input): Unit = {
    try {
      _datasource = kryo.readClassAndObject(input).asInstanceOf[FabricDatasourceContext]
      _sliceKey = input.readInt
      _hostname = input.readString
      _state = kryo.readClassAndObject(input).asInstanceOf[FabricDataState]
      _generationMetrics = kryo.readClassAndObject(input).asInstanceOf[FabricGenerationMetricsContext]
      _failure = input.readString
    } catch safely {
      case t: Throwable =>
        throw VitalsException(t)
    }
  }

  override
  def write(kryo: Kryo, output: Output): Unit = {
    try {
      kryo.writeClassAndObject(output, _datasource)
      output writeInt _sliceKey
      output writeString _hostname
      kryo.writeClassAndObject(output, _state)
      kryo.writeClassAndObject(output, _generationMetrics)
      output writeString _failure
    } catch safely {
      case t: Throwable =>
        throw VitalsException(t)
    }
  }

}
