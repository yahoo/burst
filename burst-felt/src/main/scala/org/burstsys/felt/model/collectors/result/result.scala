/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors

import org.burstsys.brio.types.BrioTypes.BrioTypeKey
import org.burstsys.brio.types.BrioTypes.BrioTypeName
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.fabric.execution.model.result.set.FabricResultSet
import org.burstsys.fabric.execution.model.result.set.FabricResultSetIndex
import org.burstsys.fabric.execution.model.result.set.FabricResultSetMetrics
import org.burstsys.fabric.execution.model.result.set.FabricResultSetName

package object result {

  final case class JsonFabricResultSet(resultIndex: FabricResultSetIndex,
                                       resultName: FabricResultSetName,
                                       metrics: FabricResultSetMetrics,
                                       columnNames: Array[String],
                                       columnTypeNames: Array[BrioTypeName],
                                       columnTypeKeys: Array[BrioTypeKey],
                                       dimensionCount: Int,
                                       aggregationCount: Int,
                                       rowCount: Int,
                                       rowSet: Array[FabricResultRow]
                                      ) extends FabricResultSet
}
