/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension

import org.burstsys.brio.types.BrioTypes.{BrioLongKey, BrioTypeKey}

package object datetime {

  /**
   * all date time related dimensions
   */
  abstract
  class FeltCubeDimDateTimeSem extends FeltCubeDimColSem {
    final override val bType: BrioTypeKey = BrioLongKey
  }

}
