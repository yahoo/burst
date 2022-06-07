/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.visits

import org.burstsys.brio.types.BrioPath.BrioPathName
import org.burstsys.felt.model.sweep.splice.FeltSplice
import org.burstsys.felt.model.visits.decl.{FeltDynamicVisitDecl, FeltVisitableRef, FeltVisitorRef}

import scala.collection.mutable.ArrayBuffer

package object generate {

  type FeltVisitBuffer = ArrayBuffer[FeltDynamicVisitDecl]
  type FeltSpliceBuffer = ArrayBuffer[FeltSplice]

  final case class FeltVisitKey(visitedName: String, visitorName: String, path: BrioPathName) {
    def spliceTag: String =
      s"""$visitedName.$visitorName.$path"""
  }

  final case class FeltVisitCapture(key: FeltVisitKey, visitor: FeltVisitorRef, visitable: FeltVisitableRef) {
    val visits: FeltVisitBuffer = new FeltVisitBuffer
  }

}
