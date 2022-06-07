/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.lattice.traveler

import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.brio.model.schema.types._
import org.burstsys.brio.types.BrioTypes.BrioVersionKey
import org.burstsys.felt.model.lattice.FeltLatTravGenContext
import org.burstsys.felt.model.schema.traveler.FeltTraveler
import org.burstsys.felt.model.sweep.splice._
import org.burstsys.felt.model.sweep.symbols.{schemaRuntimeSym, schemaSweepSym}
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, FeltNoCode, I}
import org.burstsys.vitals.strings.VitalsGeneratingArray

/**
 * generate code to implement a [[FeltTraveler]]
 * for a specific brio schema that traverses a specific reference scalar node/relation subrelations
 */
trait FeltLatRefScalRelTravGen extends Any {

  self: FeltLatTravGenContext =>

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   *
   * @param treeNode
   * @param version
   * @return
   */
  final
  def mergeStatRefScalChildren(treeNode: BrioNode, version: BrioVersionKey)(implicit cursor: FeltCodeCursor): FeltCode = {
    val children = treeNode.childrenWithoutForm(BrioValueScalarRelation)
    if (children.isEmpty) return FeltNoCode
    (children map {
      childNode =>
        val pathKey = childNode.pathKey
        val pathName = childNode.pathName
        val relationCode = childNode.relation.relationForm match {
          case BrioValueVectorRelation => processValVecRel(treeNode, version, childNode)
          case BrioValueMapRelation => processValMapRel(treeNode, version, childNode)
          case BrioReferenceScalarRelation => processRefScalChildRel(treeNode, version, childNode)
          case BrioReferenceVectorRelation => processRefVecRel(treeNode, version, childNode)
          case _ => ???
        }
        s"""|
            |$relationCode
            |$I$schemaSweepSym.referenceScalarSplice($schemaRuntimeSym, $pathKey, ${FeltChildMergePlace.key}) // $pathName $FeltChildMergePlace""".stripMargin
    }).noNulls.mkString
  }

  /**
   *
   * @param treeNode
   * @param version
   * @return
   */
  final
  def joinRefScalChildren(treeNode: BrioNode, version: BrioVersionKey)(implicit cursor: FeltCodeCursor): FeltCode = {
    val children = treeNode.childrenWithoutForm(BrioValueScalarRelation)
    if (children.isEmpty) return FeltNoCode
    (children map {
      childNode =>
        val pathKey = childNode.pathKey
        val pathName = childNode.pathName
        s"""|
            |$I$schemaSweepSym.referenceScalarSplice($schemaRuntimeSym, $pathKey, ${FeltChildJoinPlace.key})  // $pathName $FeltChildJoinPlace""".stripMargin
    }).noNulls.mkString
  }

}
