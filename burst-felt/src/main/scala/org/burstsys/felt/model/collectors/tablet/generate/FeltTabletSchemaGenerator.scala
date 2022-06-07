/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.tablet.generate

import org.burstsys.felt.model.collectors.tablet.FeltTabletBuilder
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I}

trait FeltTabletSchemaGenerator {

  self: FeltTabletBuilder =>

  final
  def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|$I${classOf[FeltTabletBuilder].getName}()""".stripMargin
  }

}
