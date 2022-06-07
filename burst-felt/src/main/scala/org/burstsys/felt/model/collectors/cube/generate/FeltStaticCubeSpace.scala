/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.generate

import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.brio.types.BrioPath.BrioPathName
import org.burstsys.felt.model.schema.FeltSchemaNode
import org.burstsys.felt.model.sweep.symbols.sweepRuntimeSym
import org.burstsys.felt.model.tree.FeltGlobal
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}

trait FeltStaticCubeSpace {
  def isRoot: Boolean

  def currentInstanceUsed(pathName: BrioPathName): Boolean

  def currentRelationUsed(pathName: BrioPathName): Boolean

  def rootCube: FeltCode

  def currentRelCube: FeltCode

  def currentRelCube(pathName: BrioPathName): FeltCode

  def currentInstCube: FeltCode

  def currentInstCube(pathName: BrioPathName): FeltCode

  def parentInstCube: FeltCode

  def parentInstTmpCube: FeltCode

  def cubeBuilderVar: FeltCode

  def cubeDictionaryVar: FeltCode

}

object FeltStaticCubeSpace {

  def apply(global: FeltGlobal, cubeName: String, node: FeltSchemaNode): FeltStaticCubeSpace =
    FeltStaticCubeSpaceContext(global, cubeName, node)

  def apply(global: FeltGlobal, cubeName: String)(implicit cursor: FeltCodeCursor): FeltStaticCubeSpace = {
    FeltStaticCubeSpaceContext(global, cubeName, global.feltSchema.nodeForPathName(cursor.pathName))
  }

}

/**
 * all the appropriate Felt Code symbols for cube operations
 *
 * @param global   used to track variable usage
 * @param cubeName (same as frame name)
 * @param cursor
 */
private final case
class FeltStaticCubeSpaceContext(global: FeltGlobal, cubeName: String, node: BrioNode) extends FeltStaticCubeSpace {

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private
  val tracker = global.artifactTracker

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def isRoot: Boolean = node.isRoot

  override
  def currentInstanceUsed(pathName: BrioPathName): Boolean = tracker.isActive(cubeInstanceVariable(cubeName, pathName))

  override
  def currentRelationUsed(pathName: BrioPathName): Boolean = tracker.isActive(cubeRelationVariable(cubeName, pathName))

  override
  def rootCube: FeltCode = s"$sweepRuntimeSym.${cubeRoot(cubeName)}"

  override
  def currentRelCube: FeltCode = currentRelationCube(cubeName, node.pathName)

  override
  def currentRelCube(pathName: BrioPathName): FeltCode = currentRelationCube(cubeName, pathName)

  override
  def currentInstCube: FeltCode = currentInstanceCube(cubeName, node.pathName)

  override
  def currentInstCube(pathName: BrioPathName): FeltCode = currentInstanceCube(cubeName, pathName)

  override
  def parentInstCube: FeltCode = parentInstanceCube(cubeName, node.parent.pathName)

  override
  def parentInstTmpCube: FeltCode = parentInstanceTmpCube(cubeName, node.parent.pathName)

  override
  def cubeBuilderVar: FeltCode = s"${cubeBuilderVariable(cubeName)}"

  override
  def cubeDictionaryVar: FeltCode = s"$sweepRuntimeSym.${cubeDictionary(cubeName)}"

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // INTERNALS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private def currentInstanceCube(cubeName: String, pathName: BrioPathName): FeltCode = {
    val artifact = tracker += cubeInstanceVariable(cubeName, pathName)
    s"$sweepRuntimeSym.$artifact"
  }

  private def currentInstanceCubeUsed(cubeName: String, pathName: BrioPathName): Boolean =
    tracker.isActive(cubeInstanceVariable(cubeName, pathName))

  private def parentInstanceCube(cubeName: String, parentPathName: BrioPathName): FeltCode = {
    val artifact = tracker += cubeInstanceVariable(cubeName, parentPathName)
    s"$sweepRuntimeSym.$artifact"
  }

  private def parentInstanceTmpCube(cubeName: String, parentPathName: BrioPathName): FeltCode = {
    s"${cubeInstanceVariable(cubeName, parentPathName)}_tmp"
  }

  private def currentRelationCube(cubeName: String, pathName: BrioPathName): FeltCode = {
    val artifact = tracker += cubeRelationVariable(cubeName, pathName)
    s"$sweepRuntimeSym.$artifact"
  }

  private def currentRelationCubeUsed(cubeName: String, pathName: BrioPathName): Boolean =
    tracker.isActive(cubeRelationVariable(cubeName, pathName))


}
