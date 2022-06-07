/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube.state

import org.burstsys.fabric.execution.model.result.row.FabricDataKeyAnyVal
import org.burstsys.zap.cube
import org.burstsys.zap.cube._

/**
 * Two similar 'find' routines, the first one navigates to a key in the cube creating one if it is missing,
 * the second just returns false if it is not found. Both designed to go lickety split
 */
trait ZapCubeNavigator extends Any with ZapCube {

  def lastRow_=(s: Int): Unit

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline override
  def rowCount_=(count: Int): Unit = lastRow = count - 1

  /**
   * The current number of rows in the cube.
   *
   * @return
   */
  @inline override
  def rowCount: Int = lastRow + 1

  /**
   * navigate to a row that matches the dimensional key or create a row with a matching key and insert at end of
   * appropriate cube bucket list
   *
   * @param key
   * @return the row found or created
   */
  @inline private[zap]
  def navigate(builder: ZapCubeBuilder, thisCube: ZapCubeContext, key: FabricDataKeyAnyVal): ZapCubeRow = {
    val hashcode = thisCube.hash(builder, thisCube, key)

    thisCube.bucket(builder, thisCube, hashcode) match {

      // no rows in bucket yet - we create new matching one, add to bucket and return
      case ZapCubeEmptyBucket =>
        val newRow = thisCube.createNewRow(builder, thisCube, key)
        if (thisCube.rowLimited) return ZapCubeRow()
        thisCube.setBucket(builder, thisCube, hashcode, newRow.rowStartOffset)
        newRow

      // we have at least one row in bucket list - scan through and see if we have a match...
      case startPtr =>
        thisCube.navigateToRowInBucket(builder, thisCube, key, ZapCubeRow(startPtr))
    }
  }

  /**
   *
   * @param key
   * @param headRow
   * @return
   */
  @inline private[zap]
  def navigateToRowInBucket(builder: ZapCubeBuilder, thisCube: ZapCubeContext,
                            key: FabricDataKeyAnyVal, headRow: ZapCubeRow): ZapCubeRow = {
    var currentRow = headRow
    var more = true
    while (more) {
      if (currentRow.matchesDimensionKey(builder, thisCube, key))
        return currentRow
      more = currentRow.hasLinkRow(builder, thisCube)
      if (more)
        currentRow = currentRow.linkRow(builder, thisCube)
    }
    // no match in bucket list, create a new matching one and link at end of currentRow
    val newRow = thisCube.createNewRow(builder, thisCube, key)
    if (!thisCube.rowLimited)
      currentRow.setLinkRow(builder, thisCube, newRow)
    newRow
  }


}
