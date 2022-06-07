/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.aggregation.primitive

import org.burstsys.brio.types.BrioTypes.brioDataTypeNameFromKey
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.FeltCubeAggDecl
import org.burstsys.felt.model.tree.source.S

/**
 * A Felt compatible ''primitive'' aggregate AST node
 */
trait FeltCubeAggPrimitiveDecl extends FeltCubeAggDecl {


  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String =
    s"$S'$columnName':${semanticType.name}[${brioDataTypeNameFromKey(valueType).toLowerCase}]"

}
