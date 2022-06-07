/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.brio.reference

import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.brio.types.BrioPath.{BrioPathKey, BrioPathName}
import org.burstsys.felt.model.brio.decl.FeltBrioDecl
import org.burstsys.felt.model.poly.FeltPolyRefFuncGen
import org.burstsys.felt.model.reference.FeltReference

/**
 * super trait for standard and extended brio references
 */
trait FeltBrioRef extends FeltReference with FeltPolyRefFuncGen {

  def parentPathName: BrioPathName

  def parentPathKey: BrioPathKey

  def pathName: BrioPathName

  def pathKey: BrioPathKey

  def refDecl: FeltBrioDecl

  final def brioNode: BrioNode = refDecl.brioNode

  final override val isMutable: Boolean = false

}
