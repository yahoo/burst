/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.topology.model.cell

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import org.burstsys.fabric.metadata.model.FabricCellKey
import org.burstsys.fabric.topology.model.node.FabricNode
import org.burstsys.vitals.properties.{VitalsPropertyMap, readPropertyMapFromKryo, writePropertyMapToKryo}

/**
 * == Cells are a logical functional grouping of nodes ==
 * The Fabric representative for the Burst '''Cell''' concept. A Cell is a set of nodes within the same site
 * that cooperate on individual units of work.
 * == Semantics ==
 * <ol>
 * <li>cells belong to '''exactly one site''' and are not easily moved</li>
 * <li>nodes belong to '''at most one''' cell</li>
 * <li>nodes can be '''moved between cells'''</li>
 * <li>cells '''contain''' one or more workers and one or more supervisor</li>
 * <li>generally cell supervisors are behind a single virtual IP (failover/load balancing)</li>
 * <li>cells are persisted to the catalog</li>
 * </ol>
 *
 * @see [[org.burstsys.fabric.topology.model.site.FabricSite]]
 *      [[FabricNode]]
 */
trait FabricCell extends Any with Equals {

  /**
   * the primary key for this cell
   *
   * @return
   */
  def cellKey: FabricCellKey

  /**
   * The properties for this Cell.
   * This is where standard property configuration is done for the cell process
   *
   * @return
   */
  def cellProperties: VitalsPropertyMap


  ///////////////////////////////////////////////////////////////////////////
  // Identity/Equality
  ///////////////////////////////////////////////////////////////////////////

  final override
  def hashCode(): Int = cellKey.hashCode

  final override
  def equals(obj: scala.Any): Boolean = obj match {
    case that: FabricCell => this.cellKey == that.cellKey
  }

  final override
  def canEqual(that: Any): Boolean = that.isInstanceOf[FabricCell]

  final override
  def toString: String = s"cell($cellKey)"

}

object FabricCell {
  def apply(cellKey: FabricCellKey, cellProperties: VitalsPropertyMap = Map.empty): FabricCell =
    FabricCellContext(cellKey, cellProperties)

}

private[this] final case
class FabricCellContext(var cellKey: FabricCellKey,
                        var cellProperties: VitalsPropertyMap = Map.empty
                       ) extends FabricCell with KryoSerializable {

  ////////////////////////////////////////////////////////////////////////////////
  // SERDE
  ////////////////////////////////////////////////////////////////////////////////

  override
  def write(kryo: Kryo, output: Output): Unit = {
    output writeLong cellKey
    writePropertyMapToKryo(output, cellProperties)
  }

  override
  def read(kryo: Kryo, input: Input): Unit = {
    cellKey = input.readLong
    cellProperties = readPropertyMapFromKryo(input)
  }
}
