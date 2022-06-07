/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube

import org.burstsys.felt.model.collectors.cube.decl.{FeltCubeDecl, FeltCubeRef}
import org.burstsys.felt.model.collectors.decl.FeltCollectorPlan

trait FeltCubePlan extends FeltCollectorPlan[FeltCubeRef, FeltCubeBuilder] {

  def decl: FeltCubeDecl

  def binding: FeltCubeProvider

  override def initialize: FeltCubePlan

}
