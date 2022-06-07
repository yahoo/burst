/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.sweep.splice

import org.burstsys.brio.types.BrioPath.BrioPathName
import org.burstsys.felt.model.FeltException

import scala.collection.mutable

/**
 * add the capability to collect splices
 */
trait FeltSpliceStore extends AnyRef {

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // internal state
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  lazy val _splices = new mutable.HashMap[String, FeltSplice]

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def allSplices: Array[FeltSplice] = _splices.values.toArray

  final
  def dynamicSplices: Array[FeltSplice] = allSplices.filter(_.placement.isDynamic)

  final protected
  def +=(splice: FeltSplice): Unit = {
    if (_splices.contains(splice.spliceTag))
      throw FeltException(splice.location, s"duplicate splice tag '${splice.spliceTag}'")
    _splices += splice.spliceTag -> splice
  }

  final protected
  def ++=(splices: Array[FeltSplice]): Unit = splices.foreach(this += _)

  final
  def splicesFor(pathName: BrioPathName, placement: FeltPlacement): Array[FeltSplice] =
    allSplices.filter(s => s.pathName == pathName && s.placement == placement)

}
