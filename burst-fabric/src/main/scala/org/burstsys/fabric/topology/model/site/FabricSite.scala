/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.topology.model.site

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import org.burstsys.fabric.metadata.model.FabricSiteKey
import org.burstsys.fabric.topology.model.node.FabricNode
import org.burstsys.vitals.properties.{VitalsPropertyMap, readPropertyMapFromKryo, writePropertyMapToKryo}

/**
 * == Sites are a physical grouping of nodes ==
 * The Fabric representative for the Burst '''Site''' concept. A Site is a fairly low level concept
 * which can be thought of as a consistent, localized physically '''close''' set of nodes e.g. data-center, security-zone,
 * network backplane. It is up to the Burst developer/deployer to make a decision about exactly how to define this.
 * == Semantics ==
 * <ol>
 * <li>nodes and cells belong to '''exactly one''' site</li>
 * <li>nodes and cells '''are not''' intended to be moved between sites easily</li>
 * <li>all nodes (and cells) within a site '''are likely''' to have top performance network connectivity</li>
 * <li>all nodes (and cells) within a site '''are likely'''  to share security context/permissions</li>
 * <li>multiple sites '''are likely''' to provide disaster failover and/or hot-hot load sharing</li>
 * <li>sites are persisted to the catalog</li>
 * <li>it is considered a good idea to share catalog persistence across sites (replicated shared DB servers)</li>
 * </ol>
 *
 * @see [[org.burstsys.fabric.topology.model.cell.FabricCell]]
 *      [[FabricNode]]
 */
trait FabricSite extends Any with Equals {

  /**
   * the primary key for this site
   *
   * @return
   */
  def siteKey: FabricSiteKey

  /**
   * The properties for this site.
   * This is where standard property configuration is done for the site
   *
   * @return
   */
  def siteProperties: VitalsPropertyMap


  ///////////////////////////////////////////////////////////////////////////
  // Identity/Equality
  ///////////////////////////////////////////////////////////////////////////

  final override
  def hashCode(): Int = siteKey.hashCode

  final override
  def equals(obj: scala.Any): Boolean = obj match {
    case that: FabricSite => this.siteKey == that.siteKey
  }

  final override
  def canEqual(that: Any): Boolean = that.isInstanceOf[FabricSite]

  final override
  def toString: String = s"site($siteKey)"

}

object FabricSite {
  def apply(siteKey: FabricSiteKey, siteProperties: VitalsPropertyMap = Map.empty): FabricSite =
    FabricSiteContext(siteKey, siteProperties)
}

private[this] final case
class FabricSiteContext(var siteKey: FabricSiteKey,
                        var siteProperties: VitalsPropertyMap = Map.empty
                       ) extends FabricSite with KryoSerializable {

  ////////////////////////////////////////////////////////////////////////////////
  // SERDE
  ////////////////////////////////////////////////////////////////////////////////

  override
  def write(kryo: Kryo, output: Output): Unit = {
    output writeLong siteKey
    writePropertyMapToKryo(output, siteProperties)
  }

  override
  def read(kryo: Kryo, input: Input): Unit = {
    siteKey = input.readLong
    siteProperties = readPropertyMapFromKryo(input)
  }

}
