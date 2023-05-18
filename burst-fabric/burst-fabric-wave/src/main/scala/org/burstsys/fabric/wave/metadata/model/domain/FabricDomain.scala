/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.metadata.model.domain

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import org.burstsys.fabric.wave.metadata.model.FabricDomainKey
import org.burstsys.vitals.json.VitalsJsonRepresentable
import org.burstsys.vitals.properties._

/**
 * The Fabric representative for the Burst Domain Entity
 */
trait FabricDomain extends VitalsJsonRepresentable[FabricDomain] with Equals {

  /**
   * the primary key for the domain
   */
  def domainKey: FabricDomainKey

  /**
   * the properties associated with this domain
   */
  def domainProperties: VitalsPropertyMap

  final override
  def toString: String = s"FabDomain(domainKey=$domainKey)"

  ///////////////////////////////////////////////////////////////////////////
  // Identity/Equality
  ///////////////////////////////////////////////////////////////////////////

  final override
  def hashCode(): Int = domainKey.hashCode

  final override
  def equals(obj: scala.Any): Boolean = obj match {
    case that: FabricDomain => this.domainKey == that.domainKey
    case _ => false
  }

  final override
  def canEqual(that: Any): Boolean = that.isInstanceOf[FabricDomain]

}

/**
 *
 */
object FabricDomain {

  def apply(): FabricDomain = FabricDomainContext()

  def apply(domainKey: FabricDomainKey, domainProperties: VitalsPropertyMap = Map.empty): FabricDomain =
    FabricDomainContext().init(domainKey, domainProperties)

}


private[fabric] final case
class FabricDomainContext() extends KryoSerializable with FabricDomain {

  ///////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  ///////////////////////////////////////////////////////////////////

  private
  var _domainKey: FabricDomainKey = -1L

  private
  var _domainProperties: VitalsPropertyMap = Map.empty

  ///////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////

  def init(domainKey: FabricDomainKey = -1L, domainProperties: VitalsPropertyMap = Map.empty): FabricDomain = {
    _domainKey = domainKey
    _domainProperties = domainProperties
    this
  }

  override def domainKey: FabricDomainKey = _domainKey

  override def domainProperties: VitalsPropertyMap = _domainProperties

  ///////////////////////////////////////////////////////////////////
  // JSON
  ///////////////////////////////////////////////////////////////////

  override def toJson: FabricDomain = JsonFabricDomain(_domainKey, _domainProperties)

  ///////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ///////////////////////////////////////////////////////////////////


  override
  def write(kryo: Kryo, output: Output): Unit = {
    output writeLong domainKey
    writePropertyMapToKryo(output, domainProperties)
  }

  override
  def read(kryo: Kryo, input: Input): Unit = {
    _domainKey = input.readLong
    _domainProperties = readPropertyMapFromKryo(input)
  }

}
