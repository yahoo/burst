/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.sweep

import org.burstsys.felt.model.schema.traveler.runtimeSuffix
import org.burstsys.felt.model.sweep.symbols.{schemaRuntimeSym, sweepRuntimeSym}
import org.burstsys.felt.model.tree.FeltGlobal
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I}

package object runtime {

  trait FeltRuntimeComponent extends Any

  trait FeltRuntimeSymbols {
    def runtimeTravelerClassName(global: FeltGlobal): FeltCode = s"${global.travelerClassName}$runtimeSuffix"

    def sweepRuntimeClassVal(global: FeltGlobal)(implicit cursor: FeltCodeCursor): FeltCode =
      s"""|
          |${I}val $sweepRuntimeSym:${global.treeGuid} = $schemaRuntimeSym.asInstanceOf[${global.treeGuid}]""".stripMargin

  }

}
