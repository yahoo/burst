/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.model.generation.key

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import org.burstsys.fabric.wave.data.model.generation.FabricGenerationIdentity
import org.burstsys.fabric.wave.data.model.slice.metadata.FabricSliceMetadata
import org.burstsys.fabric.wave.metadata.model.{FabricDomainKey, FabricGenerationClock, FabricViewKey}
import org.burstsys.vitals.json.VitalsJsonRepresentable

import scala.language.implicitConversions

/**
 * an embeddable [[FabricGenerationIdentity]] type
 */
trait FabricGenerationKey extends VitalsJsonRepresentable[FabricGenerationKey] with FabricGenerationIdentity {

  /**
   * the primary key for the domain
   */
  def domainKey: FabricDomainKey

  /**
   * the primary key for the view
   */
  def viewKey: FabricViewKey

  /**
   * the generation clock
   */
  def generationClock: FabricGenerationClock

  /**
   * assume value from another generation metric
   *
   * @param gm
   * @return
   */
  def init(gm: FabricGenerationIdentity): FabricGenerationKey

  /**
   *
   * @param domainKey
   * @param viewKey
   * @param generationClock
   * @return
   */
  def init(domainKey: FabricDomainKey, viewKey: FabricViewKey, generationClock: FabricGenerationClock): FabricGenerationKey

  /**
   * determine if a slice metadata is represented by this particular generation key.
   *
   * Generation keys represent unspecified fields with -1
   *
   * @param slice
   * @return
   */
  def identifies(slice: FabricSliceMetadata): Boolean = {
    (domainKey == -1 || domainKey == slice.datasource.domain.domainKey) &&
      (viewKey == -1 || viewKey == slice.datasource.view.viewKey) &&
      (generationClock == -1 || generationClock == slice.datasource.view.generationClock)
  }

  final override
  def toString: String = s"domainKey=$domainKey, viewKey=$viewKey, generationClock=$generationClock"

}

object FabricGenerationKey {

  def apply(
             domainKey: FabricDomainKey = -1L,
             viewKey: FabricViewKey = -1L,
             generationClock: FabricGenerationClock = -1L
           ): FabricGenerationKey =
    FabricGenerationKeyContext().init(
      domainKey = domainKey: FabricDomainKey,
      viewKey = viewKey: FabricViewKey,
      generationClock = generationClock: FabricGenerationClock
    )

  def apply(copy: FabricGenerationKey): FabricGenerationKey =
    FabricGenerationKeyContext().init(
      domainKey = copy.domainKey,
      viewKey = copy.viewKey,
      generationClock = copy.generationClock
    )

}

private[fabric] final case
class FabricGenerationKeyContext() extends FabricGenerationKey with KryoSerializable {
  ///////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  ///////////////////////////////////////////////////////////////////

  private[this]
  var _domainKey: FabricDomainKey = _

  private[this]
  var _viewKey: FabricViewKey = _

  private[this]
  var _generationClock: FabricGenerationClock = _

  ///////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////

  override def domainKey: FabricDomainKey = _domainKey

  override def viewKey: FabricViewKey = _viewKey

  override def generationClock: FabricGenerationClock = _generationClock

  ///////////////////////////////////////////////////////////////////
  // JSON
  ///////////////////////////////////////////////////////////////////

  override def toJson: FabricGenerationKey =
    JsonFabricGenerationKey(domainKey, viewKey, generationClock)

  ///////////////////////////////////////////////////////////////////
  // LIFECYCLE
  ///////////////////////////////////////////////////////////////////


  override
  def init(gm: FabricGenerationIdentity): FabricGenerationKey = {
    this._domainKey = gm.domainKey
    this._viewKey = gm.viewKey
    this._generationClock = gm.generationClock
    this
  }

  override
  def init(domainKey: FabricDomainKey, viewKey: FabricViewKey, generationClock: FabricGenerationClock): FabricGenerationKey = {
    this._domainKey = domainKey
    this._viewKey = viewKey
    this._generationClock = generationClock
    this
  }

  ///////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ///////////////////////////////////////////////////////////////////

  override
  def read(kryo: Kryo, input: Input): Unit = {
    _domainKey = input.readLong
    _viewKey = input.readLong
    _generationClock = input.readLong
  }

  override
  def write(kryo: Kryo, output: Output): Unit = {
    output writeLong _domainKey
    output writeLong _viewKey
    output writeLong _generationClock
  }

}
