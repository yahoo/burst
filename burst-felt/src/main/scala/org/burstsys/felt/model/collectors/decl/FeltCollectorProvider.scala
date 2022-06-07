/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.decl

import org.burstsys.felt.model.collectors.FeltCollectorRef
import org.burstsys.felt.model.collectors.runtime.{FeltCollector, FeltCollectorBuilder}
import org.burstsys.felt.model.tree.code.cleanClassName

/**
 * each collector has to implement this in order to provide collector functions
 *
 * @tparam D
 * @tparam B
 * @tparam C
 * @tparam R
 * @tparam P
 */
trait FeltCollectorProvider[C <: FeltCollector, B <: FeltCollectorBuilder, R <: FeltCollectorRef,
  D <: FeltCollectorDecl[R, B], P <: FeltCollectorPlan[R, B]] {

  def newBuilder: B

  def builderClassName: String

  def collectorClass: Class[_ <: C]

  final def collectorClassName: String = cleanClassName(collectorClass)

  def collectorPlan(decl: D): P

  /**
   *
   * @param builder
   * @return
   */
  def grabCollector(builder: B): C

  /**
   *
   * @param collector
   */
  def releaseCollector(collector: C): Unit

}
