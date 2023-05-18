/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.metadata.model.view

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import org.burstsys.brio.types.BrioTypes.BrioSchemaName
import org.burstsys.fabric.wave.data.model.generation.FabricGenerationIdentity
import org.burstsys.fabric.wave.metadata.model.{FabricDomainKey, FabricGenerationClock, FabricViewKey}
import org.burstsys.vitals.json.VitalsJsonRepresentable
import org.burstsys.vitals.properties._

/**
 * The Fabric representative for the Burst View Entity
 */
trait FabricView extends VitalsJsonRepresentable[FabricView] with FabricGenerationIdentity {

  /**
   * The BRIO schema name for this View
   */
  def schemaName: BrioSchemaName

  /**
   * The Store properties for this View
   */
  def storeProperties: VitalsPropertyMap

  /**
   * The Motif import filter for this View
   */
  def viewMotif: String

  /**
   * The View properties for this View
   *
   * @return
   */
  def viewProperties: VitalsPropertyMap

  def init(domainKey: FabricDomainKey, viewKey: FabricViewKey, generationClock: FabricGenerationClock): FabricView

  def init(gm: FabricGenerationIdentity): FabricView

  final override
  def toString: String =
    s"FabView(domainKey=$domainKey viewKey=$viewKey generationClock=$generationClock schemaName='$schemaName' viewMotif='${
      viewMotif.replace("\n", " ").replaceAll("\\s+", " ")
    }')"

}

/**
 *
 */
object FabricView {

  def apply(domainKey: FabricDomainKey = 0, viewKey: FabricDomainKey = 0, generationClock: FabricGenerationClock = 0,
            schemaName: String = "NoSchema", viewMotif: String = "NoMotif", viewProperties: VitalsPropertyMap = Map.empty,
            storeProperties: VitalsPropertyMap = Map.empty): FabricView =
    FabricViewContext().init(domainKey = domainKey, viewKey = viewKey, generationClock = generationClock, schemaName = schemaName,
      viewProperties = viewProperties, viewMotif = viewMotif, storeProperties = storeProperties)

}


private[fabric] final case
class FabricViewContext() extends KryoSerializable with FabricView {


  ///////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  ///////////////////////////////////////////////////////////////////

  private
  var _domainKey: FabricDomainKey = _

  private
  var _viewKey: FabricDomainKey = _

  private
  var _generationClock: FabricGenerationClock = _

  private
  var _schemaName: String = "NoSchema"

  private
  var _storeProperties: VitalsPropertyMap = Map.empty

  private
  var _viewMotif: String = "NoMotif"

  private
  var _viewProperties: VitalsPropertyMap = Map.empty

  ///////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////

  override def schemaName: BrioSchemaName = _schemaName

  override def storeProperties: VitalsPropertyMap = _storeProperties

  override def viewMotif: String = _viewMotif

  override def viewProperties: VitalsPropertyMap = _viewProperties

  override def domainKey: FabricDomainKey = _domainKey

  override def viewKey: FabricViewKey = _viewKey

  override def generationClock: FabricGenerationClock = _generationClock

  ///////////////////////////////////////////////////////////////////
  // LIFECYCLE
  ///////////////////////////////////////////////////////////////////

  def init(domainKey: FabricDomainKey, viewKey: FabricDomainKey, generationClock: FabricGenerationClock,
           schemaName: String, viewMotif: String, viewProperties: VitalsPropertyMap, storeProperties: VitalsPropertyMap): FabricView = {
    _domainKey = domainKey
    _viewKey = viewKey
    _schemaName = schemaName
    _viewMotif = viewMotif
    _generationClock = generationClock
    _viewProperties = viewProperties
    _storeProperties = storeProperties
    this
  }

  override def init(domainKey: FabricDomainKey, viewKey: FabricViewKey, generationClock: FabricGenerationClock): FabricView = {
    _domainKey = domainKey
    _viewKey = viewKey
    _generationClock = generationClock
    this
  }

  override
  def init(gm: FabricGenerationIdentity): FabricView = {
    _domainKey = gm.domainKey
    _viewKey = gm.viewKey
    _generationClock = gm.generationClock
    _schemaName = "NoSchema"
    _storeProperties = Map.empty
    _viewMotif = "NoMotif"
    _viewProperties = Map.empty
    this
  }

  ///////////////////////////////////////////////////////////////////
  // JSON
  ///////////////////////////////////////////////////////////////////

  override def toJson: FabricView = JsonFabricView(schemaName, storeProperties, viewMotif, viewProperties, domainKey, viewKey, generationClock)

  ///////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ///////////////////////////////////////////////////////////////////

  override
  def write(kryo: Kryo, output: Output): Unit = {
    output writeLong _domainKey
    output writeLong _viewKey
    output writeLong _generationClock
    output writeString _schemaName
    writePropertyMapToKryo(output, _storeProperties)
    output writeString _viewMotif
    writePropertyMapToKryo(output, _viewProperties)
  }

  override
  def read(kryo: Kryo, input: Input): Unit = {
    _domainKey = input.readLong
    _viewKey = input.readLong
    _generationClock = input.readLong
    _schemaName = input.readString
    _storeProperties = readPropertyMapFromKryo(input)
    _viewMotif = input.readString
    _viewProperties = readPropertyMapFromKryo(input)
  }

}
