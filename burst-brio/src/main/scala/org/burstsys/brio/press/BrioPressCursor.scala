/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.press

import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.brio.model.schema.types.BrioRelation
import org.burstsys.brio.types.BrioPath.{BrioPathKey, BrioPathName}
import org.burstsys.brio.types.BrioTypes.{BrioRelationName, BrioRelationOrdinal}

/**
  * Provided to source during traversal in order to show where in schema driven traversal
  * events are.
  */
trait BrioPressCursor extends Any {

  /**
   * @return the name for the field on the traversed object that is begin pressed
    */
  def relationName: BrioRelationName

  /**
   * @return the key for the field on the traversed object that is being pressed
    */
  def relationOrdinal: BrioRelationOrdinal

  /**
    * @return TODO
    */
  def pathName: BrioPathName

  /**
    * @return TODO
    */
  def pathKey: BrioPathKey

  /**
    * TODO
    *
    * @return
    */
  def isKey: Boolean

  /**
    * TODO
    *
    * @return
    */
  def isOrdinal: Boolean

  /**
    * TODO
    *
    * @return
    */
  def isSorted: Boolean

}

object BrioPressCursor {

  def apply(
             pathName: BrioPathName,
             pathKey: BrioPathKey,
             relationName: BrioRelationName,
             relationOrdinal: BrioRelationOrdinal
           ): BrioPressCursor =
    BrioPressCursorContext(
      pathName: BrioPathName,
      pathKey: BrioPathKey,
      relationName: BrioRelationName,
      relationOrdinal: BrioRelationOrdinal
    )

}

private final case
class BrioPressCursorContext(var pathName: BrioPathName, var pathKey: BrioPathKey,
                             var relationName: BrioRelationName, var relationOrdinal: BrioRelationOrdinal)
  extends BrioPressCursor {

  var isKey: Boolean = false

  var isOrdinal: Boolean = false

  var isSorted: Boolean = false

  def initialize(node: BrioNode): this.type = {
    pathName = node.pathName
    pathKey = node.pathKey
    relationName = node.relation.relationName
    relationOrdinal = node.relation.relationOrdinal
    isKey = node.relation.isKey
    isOrdinal = node.relation.isOrdinal
    this
  }

  override
  def toString: BrioPathName =
    s"Cursor(pathName='$pathName', pathKey=$pathKey, relationName='$relationName', relationOrdinal=$relationOrdinal)"

}

