/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route

import org.burstsys.brio.types.BrioCourse._
import org.burstsys.felt.binding.FeltBinding
import org.burstsys.felt.model.collectors.route.decl.graph.FeltRouteTransition
import org.burstsys.felt.model.collectors.route.plane.FeltRoutePlaneContext
import org.burstsys.felt.model.collectors.runtime.{FeltCollectorBuilder, FeltCollectorBuilderContext, FeltCollectorPlane}
import org.burstsys.tesla.TeslaTypes.TeslaMemoryOffset

trait FeltRouteBuilder extends FeltCollectorBuilder {

  /**
   * TODO
   *
   * @return
   */
  def maxPartialPaths: Int

  /**
   *
   * @return
   */
  def maxCompletePaths: Int

  /**
   * TODO
   *
   * @return
   */
  def maxStepsPerGraph: Int

  /**
   * TODO
   *
   * @return
   */
  def maxPathTime: Long

  /**
   * TODO
   *
   * @return
   */
  def minCourse: Int

  /**
   * TODO
   *
   * @return
   */
  def maxCourse: Int

  /**
   * TODO
   *
   * @return
   */
  def entranceSteps: Array[FeltRouteStepKey]

  /**
   * TODO
   *
   * @return
   */
  def exitSteps: Array[FeltRouteStepKey]

  /**
   * TODO
   *
   * @return
   */
  def beginSteps: Array[FeltRouteStepKey]

  /**
   * TODO
   *
   * @return
   */
  def tacitSteps: Array[FeltRouteStepKey]

  /**
   * TODO
   *
   * @return
   */
  def emitCodes: Array[BrioCourseId]

  /**
   * TODO
   *
   * @return
   */
  def endSteps: Array[FeltRouteStepKey]

  /**
   * TODO
   *
   * @return
   */
  def completeSteps: Array[FeltRouteStepKey]

  /**
   * TODO
   *
   * @return
   */
  def transitions: Array[FeltRouteTransition]

  /**
   * TODO
   *
   * @return
   */
  def isEntranceStep(step: FeltRouteStepKey): Boolean

  /**
   * TODO
   *
   * @return
   */
  def isExitStep(step: FeltRouteStepKey): Boolean

  def isCompleteStep(step: FeltRouteStepKey): Boolean

  def init(
            frameId: Int,
            frameName: String,
            binding: FeltBinding,
            maxPartialPaths: Int,
            maxCompletePaths: Int,
            maxStepsPerPath: Int,
            maxPathTime: Long,
            minCourse: Int,
            maxCourse: Int,
            entranceSteps: Array[FeltRouteStepKey],
            exitSteps: Array[FeltRouteStepKey],
            beginSteps: Array[FeltRouteStepKey],
            tacitSteps: Array[FeltRouteStepKey],
            emitCodes: Array[Int],
            endSteps: Array[FeltRouteStepKey],
            completeSteps: Array[FeltRouteStepKey],
            transitions: Array[FeltRouteTransition]
          ): Unit

}

abstract
class FeltRouteBuilderContext extends FeltCollectorBuilderContext with FeltRouteBuilder {

  final override
  def collectorPlaneClass[C <: FeltCollectorPlane[_, _]]: Class[C] =
    classOf[FeltRoutePlaneContext].asInstanceOf[Class[C]]

}
