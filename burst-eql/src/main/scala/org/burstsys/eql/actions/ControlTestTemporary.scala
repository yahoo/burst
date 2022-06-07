/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.actions

import org.burstsys.eql.generators.DeclarationScope.DeclarationScope
import org.burstsys.eql.generators.{DeclarationScope, Var}
import org.burstsys.eql.generators.hydra.actions.ControlTestSourceGenerator
import org.burstsys.motif.common.DataType
import org.burstsys.motif.paths.schemas.StructurePath


object ControlTestTemporary {
  def apply(name: String, scope: DeclarationScope): ControlTestTemporary = {
    new ControlTestTemporary(name, scope = scope)
  }
}

class ControlTestTemporary(val name: String, val scope: DeclarationScope = DeclarationScope.Frame, var needsSummary: Boolean = false)
  extends Temporary with ControlTestSourceGenerator
{
  override def getLowestVisitPath: StructurePath = throw new NotImplementedError()

  lazy val summaryVar: Var = Var(name + "_summary", scope, DataType.BOOLEAN)
  lazy val tempVar: Var = Var(name, scope, DataType.BOOLEAN)
}
