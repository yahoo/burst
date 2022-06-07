/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators.hydra.actions

import org.burstsys.eql._
import org.burstsys.eql.actions.DimensionInsert
import org.burstsys.eql.generators.ActionSourceGenerator
import org.burstsys.eql.generators.hydra.utils.CodeBlock

trait DimensionInsertSourceGenerator extends ActionSourceGenerator {
  self: DimensionInsert =>

  override
  def generateSource()(implicit context: GlobalContext): CodeBlock = {
    s"insert($qualifiedFrameName)"
  }

}
