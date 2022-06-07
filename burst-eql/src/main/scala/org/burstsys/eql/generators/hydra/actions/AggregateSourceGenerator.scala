/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators.hydra.actions

import org.burstsys.eql.{GlobalContext, qualifiedFrameName, qualifiedName}
import org.burstsys.eql.actions.Aggregate
import org.burstsys.eql.generators._
import org.burstsys.eql.generators.hydra.utils.CodeBlock
import org.burstsys.motif.motif.tree.values.AggregationOperatorType

trait AggregateSourceGenerator extends ActionSourceGenerator {
  self: Aggregate =>

  override
  def generateSource()(implicit context: GlobalContext): CodeBlock = {
    function match {
      case AggregationOperatorType.COUNT | AggregationOperatorType.TOP | AggregationOperatorType.UNIQUE =>
        s"${qualifiedName(name)} = 1"
      case _ =>
        val e = value.generateSource()
        assert(e.length == 1)
        s"${qualifiedName(this.name)} = ${e.head}"
    }

  }

}
