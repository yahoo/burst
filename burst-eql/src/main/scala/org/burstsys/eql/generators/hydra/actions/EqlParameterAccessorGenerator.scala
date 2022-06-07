/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators.hydra.actions

import org.burstsys.eql.GlobalContext
import org.burstsys.eql.generators.ActionPhase.ActionPhase
import org.burstsys.eql.generators.hydra.utils.CodeBlock
import org.burstsys.eql.generators.{ActionSourceGenerator, _}
import org.burstsys.eql.planning.EqlParameterAccessor

trait EqlParameterAccessorGenerator extends ActionSourceGenerator {
  self: EqlParameterAccessor =>

  override
  def generateSource()(implicit context: GlobalContext): CodeBlock = {
    val e = reference.value.generateSource()
    assert(e.length <= 1)
    e
  }

  override def phase(): ActionPhase = ActionPhase.Pre
}
