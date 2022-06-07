/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model

import org.burstsys.felt.binding.FeltBinding

/**
 * =sweep=
 */
package object sweep {

  trait FeltSweepComponent extends Any {
    def feltBinding: FeltBinding
  }

}
