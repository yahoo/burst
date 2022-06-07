/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators.hydra.actions

import org.burstsys.eql.{GlobalContext, qualifiedName}
import org.burstsys.eql.actions.DimensionAssign
import org.burstsys.eql.generators.hydra.utils.CodeBlock
import org.burstsys.eql.generators.{ActionSourceGenerator, _}

trait DimensionAssignSourceGenerator extends ActionSourceGenerator {
  self: DimensionAssign =>

  override
  def generateSource()(implicit context: GlobalContext): CodeBlock = {
    val e = expression.generateSource()
    assert(e.length == 1)
    s"${qualifiedName(this.name)} = ${e.head}"
  }

}
