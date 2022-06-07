/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.execution.model.result

package object set {

  type FabricResultSetIndex = Int

  type FabricResultSetName = String

  type FabricResultSets = Map[FabricResultSetIndex, FabricResultSet]

}
