/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.model.result.group

import org.burstsys.fabric.wave.execution.FabricResourceHolder
import org.burstsys.fabric.wave.execution.model.execute
import org.burstsys.fabric.wave.execution.model.execute.group.FabricGroupKey
import org.burstsys.fabric.wave.execution.model.result._
import org.burstsys.fabric.wave.execution.model.result.set._
import org.burstsys.fabric.wave.execution.model.result.status._
import org.burstsys.vitals.json.VitalsJsonObject
import org.burstsys.vitals.json.VitalsJsonRepresentable

/**
 * zero or more result sets
 *
 */
trait FabricResultGroup extends VitalsJsonRepresentable[FabricResultGroup]
  with FabricResult with FabricResourceHolder {

  /**
   * the key (identity) associated with this group
   *
   * @return
   */
  def groupKey: FabricGroupKey

  /**
   * all the metrics associated with this result group
   *
   * @return
   */
  def groupMetrics: FabricResultGroupMetrics

  /**
   * a count of all the rows returned across all the queries in the
   * result group.
   *
   * @return
   */
  def rowCount: Int

  /**
   * map result set ordinal index to the name
   *
   * @return
   */
  def resultSets: Map[FabricResultSetIndex, FabricResultSet]

  /**
   * map result set names to the ordinal index
   *
   * @return
   */
  def resultSetNames: Map[FabricResultSetName, FabricResultSetIndex] = resultSets.map(rs => rs._2.resultName -> rs._1)

  /**
   * process internal data structures to prepare this group for access
   *
   * @return
   */
  def extractResults: FabricResultGroup = {
    this
  }

  /**
   * were there any failures?
   *
   * @return
   */
  final
  def hadFailures: Boolean = groupMetrics.executionMetrics.hadFailures

}

object FabricResultGroup {

  def apply(
             groupKey: FabricGroupKey = FabricGroupKey(),
             resultStatus: FabricResultStatus = FabricSuccessResultStatus,
             resultMessage: String = "ok",
             groupMetrics: FabricResultGroupMetrics = FabricResultGroupMetrics(),
             resultSets: FabricResultSets = Map.empty,
             rowCount: Int = 0
           ): FabricResultGroup =
    FabricResultGroupContext(
      groupKey: FabricGroupKey,
      resultStatus: FabricResultStatus,
      resultMessage: String,
      groupMetrics: FabricResultGroupMetrics,
      resultSets: FabricResultSets,
      rowCount: Int
    )

}

private final case
class FabricResultGroupContext(
                                groupKey: FabricGroupKey,
                                resultStatus: FabricResultStatus,
                                resultMessage: String,
                                groupMetrics: FabricResultGroupMetrics,
                                resultSets: FabricResultSets,
                                rowCount: Int
                              ) extends FabricResultGroup with VitalsJsonObject {
  override def toJson: FabricResultGroup =
    FabricResultGroupContext(groupKey, resultStatus, resultMessage, groupMetrics.toJson, resultSets.view.mapValues(_.toJson).toMap, rowCount)
}
