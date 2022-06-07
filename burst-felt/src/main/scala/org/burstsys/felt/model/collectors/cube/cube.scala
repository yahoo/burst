/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors

import org.burstsys.felt.model.collectors.cube.decl.{FeltCubeDecl, FeltCubeRef}
import org.burstsys.felt.model.collectors.decl.FeltCollectorProvider
import org.burstsys.felt.model.tree.{FeltNode, FeltTreeRules}

package object cube {

  /**
   * all cubes and sub-cubes have a unique id
   */
  type FeltCubeId = Int

  /**
   * keep track of places where a cube id has not been specified
   */
  final
  val FeltNoCubeDefined: FeltCubeId = -1

  /**
   *
   */
  type FeltCubeName = String

  trait FeltCubeProvider
    extends FeltCollectorProvider[FeltCubeCollector, FeltCubeBuilder, FeltCubeRef, FeltCubeDecl, FeltCubePlan]

  final implicit
  class FeltCubeRules(node: FeltNode) extends FeltTreeRules {
    def allCubeDecls: Array[FeltCubeDecl] = node.allNodesOfType[FeltCubeDecl]
  }

}
