/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.visits.generate

import org.burstsys.felt.model.analysis.decl.FeltAnalysisDecl
import org.burstsys.felt.model.brio.reference.FeltBrioStdRef
import org.burstsys.felt.model.schema.traveler.FeltTraveler
import org.burstsys.felt.model.sweep.splice.{FeltExprSplice, FeltSpliceStore}
import org.burstsys.felt.model.visits.decl.{FeltDynamicVisitDecl, FeltStaticVisitDecl, FeltVisitDecl}

import scala.language.implicitConversions

/**
 * Top level cube '''splicing''' algorithm. Here we make sure that all cube splices are given a chance to insert themselves.
 */
final case
class FeltStaticVisitSplicer(analysis: FeltAnalysisDecl) extends FeltSpliceStore {

  def collect: this.type = {
    analysis.frames.flatMap(_.visits.filter(_.isInstanceOf[FeltStaticVisitDecl])).foreach(collectStaticSplices)
    this
  }

  private
  def collectStaticSplices(visit: FeltVisitDecl): Unit = {
    visit match {

      /**
       * static visits are easy... They follow the [[FeltTraveler]]
       * and just require cube merges/joins
       */
      case staticVisit: FeltStaticVisitDecl =>
        staticVisit.actions.foreach {
          action =>
            val brioReference = staticVisit.traverseTarget.referenceGetOrThrow[FeltBrioStdRef]
            val traverseNode = brioReference.refDecl.brioNode
            if (action.expressionBlock.nonEmpty) {
              this += FeltExprSplice(
                visit.global, visit.location,
                spliceTag = visit.frame.frameName,
                pathName = traverseNode.pathName,
                placement = action.actionType.placement,
                expression = action.expressionBlock,
                ordinal = visit.ordinal
              )
            }
        }

      case dynamicVisit: FeltDynamicVisitDecl => // handled elsewhere

      case _ => ???

    }
  }

}
