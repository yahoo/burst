/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.metadata.model.datasource

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import org.burstsys.fabric.data.model.generation.metrics
import org.burstsys.fabric.data.model.generation.metrics.FabricGenerationMetrics
import org.burstsys.fabric.metadata.model.domain.{FabricDomain, FabricDomainContext}
import org.burstsys.fabric.metadata.model.view.{FabricView, FabricViewContext}
import org.burstsys.fabric.metadata.model.{FabricDomainKey, FabricGenerationClock, FabricViewKey}
import org.burstsys.fabric.metadata.{ViewLastLoadStaleMsProperty, ViewNextLoadStaleMsDefault, ViewNextLoadStaleMsProperty, ViewNextSampleRateDefault, ViewNextSampleRateProperty, model}
import org.burstsys.vitals.json.VitalsJsonRepresentable
import org.burstsys.vitals.properties._

/**
 * The Fabric metadata associated with a Burst distributed data object
 */
trait FabricDatasource extends VitalsJsonRepresentable[FabricDatasource] with Equals {

  /**
   * The Domain entity of this Burst distributed data object
   */
  def domain: FabricDomain

  /**
   * The View entity of this Burst distributed data object
   */
  def view: FabricView

  /**
   * Update the datasource properties to sync them up with metrics from the last load
   *
   * @param generationMetrics
   * @return
   */
  def postWaveMetricsUpdate(generationMetrics: FabricGenerationMetrics): VitalsPropertyMap

  ///////////////////////////////////////////////////////////////////////////
  // Identity/Equality
  ///////////////////////////////////////////////////////////////////////////

  final override
  def hashCode(): Int = {
    var result: Long = 17
    result = 31 * result + domain.hashCode
    result = 31 * result + view.hashCode
    result.toInt
  }

  final override
  def equals(obj: scala.Any): Boolean = obj match {
    case that: FabricDatasource => this.domain == that.domain && this.view == that.view
  }

  final override
  def canEqual(that: Any): Boolean = that.isInstanceOf[FabricDatasource]

  override def toString: String = s"FabDatasource(domain=$domain view=$view)"

}

/**
 * Construct a datasource
 */
object FabricDatasource {

  def apply(): FabricDatasource = FabricDatasourceContext()

  def apply(domain: FabricDomain, view: FabricView): FabricDatasource = FabricDatasourceContext().init(domain, view)

  def apply(domainKey: FabricDomainKey, viewKey: FabricViewKey, generationClock: FabricGenerationClock): FabricDatasource =
    FabricDatasourceContext().init(domainKey = domainKey, viewKey = viewKey, generationClock = generationClock)

}

final case
class FabricDatasourceContext() extends KryoSerializable with FabricDatasource {

  ///////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  ///////////////////////////////////////////////////////////////////

  private
  var _domain: FabricDomain = _

  private
  var _view: FabricView = _

  ///////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////

  override def domain: FabricDomain = _domain

  override def view: FabricView = _view

  override
  def postWaveMetricsUpdate(generationMetrics: FabricGenerationMetrics): VitalsPropertyMap = {

    val loadStaleMs = _view.viewProperties.extend.getValueOrDefault(ViewNextLoadStaleMsProperty, ViewNextLoadStaleMsDefault)
    generationMetrics.calcEarliestLoadAt(loadStaleMs = loadStaleMs)

    val viewProperties = _view.viewProperties ++
      metrics.generationMetricsToProperties(generationMetrics) +
      (ViewLastLoadStaleMsProperty -> loadStaleMs.toString)

    viewProperties
  }

  ///////////////////////////////////////////////////////////////////
  // LIFECYCLE
  ///////////////////////////////////////////////////////////////////

  def init(domain: FabricDomain, view: FabricView): FabricDatasource = {
    _domain = domain
    _view = view
    this
  }

  def init(domainKey: FabricDomainKey, viewKey: FabricViewKey, generationClock: FabricGenerationClock): FabricDatasource = {
    _domain = model.domain.FabricDomain(domainKey = domainKey)
    _view = model.view.FabricView(domainKey = domainKey, viewKey = viewKey, generationClock = generationClock)
    this
  }

  ///////////////////////////////////////////////////////////////////
  // JSON
  ///////////////////////////////////////////////////////////////////

  override def toJson: FabricDatasource = JsonFabricDatasource(domain.toJson, view.toJson)

  ///////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ///////////////////////////////////////////////////////////////////

  override
  def read(kryo: Kryo, input: Input): Unit = {
    _domain = kryo.readClassAndObject(input).asInstanceOf[FabricDomainContext]
    _view = kryo.readClassAndObject(input).asInstanceOf[FabricViewContext]
  }

  override
  def write(kryo: Kryo, output: Output): Unit = {
    kryo.writeClassAndObject(output, _domain)
    kryo.writeClassAndObject(output, _view)
  }

}

