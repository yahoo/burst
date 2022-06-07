/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.execution.model.result.set

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.vitals.json.VitalsJsonObject
import org.burstsys.vitals.json.VitalsJsonRepresentable

/**
 * a set of 'rows' in a result (one of n in a [[FabricResultGroup]])
 */
trait FabricResultSetMetrics extends VitalsJsonRepresentable[FabricResultSetMetrics] {

  /**
   * was this a successful result?
   *
   * @return
   */
  def succeeded: Boolean

  /**
   * how many rows were returned
   *
   * @return
   */
  def rowCount: Long

  /**
   * was the row count 'limited'?
   *
   * @return
   */
  def limited: Boolean

  /**
   * did the dictionary overflow?
   *
   * @return
   */
  def overflowed: Boolean

  /**
   * TODO
   *
   * @return
   */
  def properties: Map[String, String]

}

object FabricResultSetMetrics {

  def apply(
             succeeded: Boolean = false,
             rowCount: Long = 0,
             limited: Boolean = false,
             overflowed: Boolean = false,
             properties: Map[String, String] = Map.empty
           ): FabricResultSetMetrics =
    FabricResultSetMetricsContext(
      succeeded: Boolean,
      rowCount: Long,
      limited: Boolean,
      overflowed: Boolean,
      properties: Map[String, String]
    )

}

/**
 * reference implementation - also for JSON
 *
 * @param succeeded
 * @param rowCount
 * @param limited
 * @param overflowed
 * @param properties
 */
private final case
class FabricResultSetMetricsContext(
                                     succeeded: Boolean,
                                     rowCount: Long,
                                     limited: Boolean,
                                     overflowed: Boolean,
                                     properties: Map[String, String]
                                   ) extends FabricResultSetMetrics with VitalsJsonObject
