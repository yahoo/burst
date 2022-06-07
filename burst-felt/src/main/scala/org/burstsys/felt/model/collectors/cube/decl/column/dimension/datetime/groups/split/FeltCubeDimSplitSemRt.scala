/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime.groups.split

import org.burstsys.felt.model.collectors.cube.decl.column.dimension.FeltCubeDimSemRt
import org.burstsys.ginsu.functions.GinsuFunctions

abstract class FeltCubeDimSplitSemRt extends AnyRef
  with FeltCubeDimSemRt with GinsuFunctions {

  override protected val dimensionHandlesStrings: Boolean = false

  semanticType = SPLIT_DIMENSION_SEMANTIC

}
