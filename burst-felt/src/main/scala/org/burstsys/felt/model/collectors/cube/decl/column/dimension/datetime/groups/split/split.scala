/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime.groups

import org.burstsys.felt.model.collectors.cube.decl.column.dimension.{FeltCubeDimColSem, FeltDimSemType}

package object split {

  private[felt] final val SplitName = "split"

  object SPLIT_DIMENSION_SEMANTIC extends FeltDimSemType(SplitName)

  abstract
  class FeltCubeDimSplitSem extends FeltCubeDimColSem


}
