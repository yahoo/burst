/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column

import org.burstsys.felt.model.collectors.cube.decl.{FeltCubeColSem, FeltSemType}

package object dimension {

  /**
   * ENUM type for identifying dimensional semantics
   *
   */
  abstract
  class FeltDimSemType(nm: String) extends FeltSemType(nm) {
    final val id: String = name;
  }

  /**
   * TODO
   *
   */
  abstract
  class FeltCubeDimColSem extends FeltCubeColSem {

    def semanticRt: FeltCubeDimSemRt

  }

}
